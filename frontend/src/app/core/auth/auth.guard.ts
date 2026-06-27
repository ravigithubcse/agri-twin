import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { TokenStorageService } from '../services/token-storage.service';

/**
 * Functional guard (modern Angular style). Checks for the *presence* of a
 * stored access token, not full validity -- if the token is expired, the
 * authInterceptor's refresh-and-retry handles that on the first real API
 * call, and APP_INITIALIZER's restoreSession() handles it at startup.
 * This guard only stops obviously-logged-out users from reaching protected
 * routes before any HTTP call is even made.
 */
export const authGuard: CanActivateFn = () => {
  const tokenStorage = inject(TokenStorageService);
  const router = inject(Router);

  if (tokenStorage.getAccessToken()) {
    return true;
  }

  router.navigate(['/login']);
  return false;
};
