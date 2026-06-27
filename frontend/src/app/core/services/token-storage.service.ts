import { Injectable } from '@angular/core';

const ACCESS_TOKEN_KEY = 'agritwin_access_token';
const REFRESH_TOKEN_KEY = 'agritwin_refresh_token';

/**
 * Wraps localStorage so the rest of the app never touches the Web Storage
 * API directly. NOTE: localStorage is acceptable here because this is a
 * real deployed Angular app (not a claude.ai artifact sandbox, where
 * browser storage APIs are unavailable) -- this service runs in a normal
 * browser via `ng serve` / a real build.
 */
@Injectable({ providedIn: 'root' })
export class TokenStorageService {
  getAccessToken(): string | null {
    return localStorage.getItem(ACCESS_TOKEN_KEY);
  }

  getRefreshToken(): string | null {
    return localStorage.getItem(REFRESH_TOKEN_KEY);
  }

  setTokens(accessToken: string, refreshToken: string): void {
    localStorage.setItem(ACCESS_TOKEN_KEY, accessToken);
    localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken);
  }

  setAccessToken(accessToken: string): void {
    localStorage.setItem(ACCESS_TOKEN_KEY, accessToken);
  }

  clear(): void {
    localStorage.removeItem(ACCESS_TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
  }
}
