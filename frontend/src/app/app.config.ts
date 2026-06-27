import { APP_INITIALIZER, ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideAnimations } from '@angular/platform-browser/animations';
import { firstValueFrom, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { routes } from './app.routes';
import { authInterceptor } from './core/interceptors/auth.interceptor';
import { AuthService } from './core/auth/auth.service';
import { TokenStorageService } from './core/services/token-storage.service';

/**
 * On app startup, if a previously-stored access token exists, try to
 * restore the session by fetching /users/me. If that fails (token expired,
 * server down, etc.) we swallow the error here -- the user simply lands on
 * a logged-out route, rather than the whole app failing to bootstrap.
 */
function initializeAuth(authService: AuthService, tokenStorage: TokenStorageService) {
  return () => {
    if (!tokenStorage.getAccessToken()) {
      return Promise.resolve();
    }
    return firstValueFrom(
      authService.restoreSession().pipe(catchError(() => of(null))),
    );
  };
}

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideHttpClient(withInterceptors([authInterceptor])),
    provideAnimations(),
    {
      provide: APP_INITIALIZER,
      useFactory: initializeAuth,
      deps: [AuthService, TokenStorageService],
      multi: true,
    },
  ],
};
