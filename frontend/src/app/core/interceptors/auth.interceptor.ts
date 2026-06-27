import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, switchMap, throwError } from 'rxjs';
import { AuthService } from '../auth/auth.service';
import { TokenStorageService } from '../services/token-storage.service';

const AUTH_FREE_PATHS = ['/auth/register', '/auth/login', '/auth/refresh'];

/**
 * Functional interceptor (Angular's modern style, replacing the old
 * HttpInterceptor class pattern) that:
 *  1. Attaches the bearer access token to every outgoing request, except
 *     the auth endpoints that issue tokens in the first place.
 *  2. On a 401, attempts exactly one silent refresh-and-retry before giving
 *     up and forcing a re-login -- this avoids infinite refresh loops if
 *     the refresh token itself is invalid/expired.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const tokenStorage = inject(TokenStorageService);
  const authService = inject(AuthService);
  const router = inject(Router);

  const isAuthFree = AUTH_FREE_PATHS.some((path) => req.url.includes(path));
  const accessToken = tokenStorage.getAccessToken();

  const authorizedReq = !isAuthFree && accessToken
    ? req.clone({ setHeaders: { Authorization: `Bearer ${accessToken}` } })
    : req;

  return next(authorizedReq).pipe(
    catchError((error: unknown) => {
      if (error instanceof HttpErrorResponse && error.status === 401 && !isAuthFree) {
        return authService.refreshAccessToken().pipe(
          switchMap(() => {
            const refreshedToken = tokenStorage.getAccessToken();
            const retriedReq = req.clone({
              setHeaders: { Authorization: `Bearer ${refreshedToken}` },
            });
            return next(retriedReq);
          }),
          catchError((refreshError) => {
            tokenStorage.clear();
            router.navigate(['/login']);
            return throwError(() => refreshError);
          }),
        );
      }
      return throwError(() => error);
    }),
  );
};
