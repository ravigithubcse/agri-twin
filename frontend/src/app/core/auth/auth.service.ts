import { HttpClient } from '@angular/common/http';
import { Injectable, computed, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthResponse, LoginRequest, RegisterRequest, UserResponse } from '../models/user.model';
import { TokenStorageService } from '../services/token-storage.service';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly baseUrl = `${environment.userServiceUrl}/auth`;
  private readonly usersUrl = `${environment.userServiceUrl}/users`;

  /** Signal-based current user state -- null means logged out. */
  private readonly currentUserSignal = signal<UserResponse | null>(null);
  readonly currentUser = this.currentUserSignal.asReadonly();
  readonly isAuthenticated = computed(() => this.currentUserSignal() !== null);

  constructor(
    private readonly http: HttpClient,
    private readonly tokenStorage: TokenStorageService,
    private readonly router: Router,
  ) {}

  register(request: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/register`, request).pipe(
      tap((response) => this.onAuthenticated(response)),
    );
  }

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/login`, request).pipe(
      tap((response) => this.onAuthenticated(response)),
    );
  }

  /** Called once at app startup to restore session from a stored access token. */
  restoreSession(): Observable<UserResponse> {
    return this.http.get<UserResponse>(`${this.usersUrl}/me`).pipe(
      tap((user) => this.currentUserSignal.set(user)),
    );
  }

  refreshAccessToken(): Observable<AuthResponse> {
    const refreshToken = this.tokenStorage.getRefreshToken();
    return this.http.post<AuthResponse>(`${this.baseUrl}/refresh`, { refreshToken }).pipe(
      tap((response) => this.onAuthenticated(response)),
    );
  }

  logout(): void {
    this.http.post(`${this.baseUrl}/logout`, {}).subscribe({
      complete: () => this.onLoggedOut(),
      error: () => this.onLoggedOut(), // still clear local state even if the network call fails
    });
  }

  private onAuthenticated(response: AuthResponse): void {
    this.tokenStorage.setTokens(response.accessToken, response.refreshToken);
    this.currentUserSignal.set(response.user);
  }

  private onLoggedOut(): void {
    this.tokenStorage.clear();
    this.currentUserSignal.set(null);
    this.router.navigate(['/login']);
  }
}
