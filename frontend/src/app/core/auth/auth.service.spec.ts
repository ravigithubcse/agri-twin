import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter, Router } from '@angular/router';
import { AuthService } from './auth.service';
import { TokenStorageService } from '../services/token-storage.service';
import { AuthResponse, UserResponse } from '../models/user.model';
import { environment } from '../../../environments/environment';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;
  let tokenStorage: TokenStorageService;
  let router: Router;

  const sampleUser: UserResponse = {
    id: 'user-1',
    phone: '9876543210',
    fullName: 'Ramesh Kumar',
    role: 'FARMER',
    tier: 'FREE',
    stateCode: 'MH',
    districtCode: 'PUN',
    languagePreference: 'hi',
    createdAt: '2026-06-01T00:00:00Z',
  };

  const sampleAuthResponse: AuthResponse = {
    accessToken: 'access-token-123',
    refreshToken: 'refresh-token-456',
    accessTokenExpiresInSeconds: 900,
    user: sampleUser,
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    });

    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
    tokenStorage = TestBed.inject(TokenStorageService);
    router = TestBed.inject(Router);
    localStorage.clear();
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('starts logged out', () => {
    expect(service.isAuthenticated()).toBe(false);
    expect(service.currentUser()).toBeNull();
  });

  it('login() stores tokens and sets currentUser on success', () => {
    service.login({ phone: '9876543210', password: 'SecurePass123' }).subscribe();

    const req = httpMock.expectOne(`${environment.userServiceUrl}/auth/login`);
    expect(req.request.method).toBe('POST');
    req.flush(sampleAuthResponse);

    expect(service.isAuthenticated()).toBe(true);
    expect(service.currentUser()).toEqual(sampleUser);
    expect(tokenStorage.getAccessToken()).toBe('access-token-123');
    expect(tokenStorage.getRefreshToken()).toBe('refresh-token-456');
  });

  it('register() stores tokens and sets currentUser on success', () => {
    service.register({
      phone: '9876543210',
      password: 'SecurePass123',
      fullName: 'Ramesh Kumar',
    }).subscribe();

    const req = httpMock.expectOne(`${environment.userServiceUrl}/auth/register`);
    expect(req.request.method).toBe('POST');
    req.flush(sampleAuthResponse);

    expect(service.currentUser()).toEqual(sampleUser);
  });

  it('restoreSession() populates currentUser from /users/me', () => {
    service.restoreSession().subscribe();

    const req = httpMock.expectOne(`${environment.userServiceUrl}/users/me`);
    expect(req.request.method).toBe('GET');
    req.flush(sampleUser);

    expect(service.currentUser()).toEqual(sampleUser);
  });

  it('logout() clears tokens, currentUser, and navigates to /login', () => {
    tokenStorage.setTokens('a', 'b');
    const navigateSpy = spyOn(router, 'navigate');

    service.logout();

    const req = httpMock.expectOne(`${environment.userServiceUrl}/auth/logout`);
    req.flush({});

    expect(tokenStorage.getAccessToken()).toBeNull();
    expect(service.currentUser()).toBeNull();
    expect(navigateSpy).toHaveBeenCalledWith(['/login']);
  });

  it('logout() clears local state even if the server call fails', () => {
    tokenStorage.setTokens('a', 'b');

    service.logout();

    const req = httpMock.expectOne(`${environment.userServiceUrl}/auth/logout`);
    req.error(new ProgressEvent('network error'));

    expect(tokenStorage.getAccessToken()).toBeNull();
    expect(service.currentUser()).toBeNull();
  });
});
