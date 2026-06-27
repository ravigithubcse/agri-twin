import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter, Router } from '@angular/router';
import { authInterceptor } from './auth.interceptor';
import { TokenStorageService } from '../services/token-storage.service';
import { environment } from '../../../environments/environment';

describe('authInterceptor', () => {
  let httpClient: HttpClient;
  let httpMock: HttpTestingController;
  let tokenStorage: TokenStorageService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        provideRouter([]),
      ],
    });

    httpClient = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
    tokenStorage = TestBed.inject(TokenStorageService);
    localStorage.clear();
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('attaches the Authorization header when an access token is present', () => {
    tokenStorage.setTokens('my-access-token', 'my-refresh-token');

    httpClient.get(`${environment.farmTwinServiceUrl}/farm-twins/me`).subscribe();

    const req = httpMock.expectOne(`${environment.farmTwinServiceUrl}/farm-twins/me`);
    expect(req.request.headers.get('Authorization')).toBe('Bearer my-access-token');
    req.flush({});
  });

  it('does not attach Authorization header to the login endpoint', () => {
    tokenStorage.setTokens('my-access-token', 'my-refresh-token');

    httpClient.post(`${environment.userServiceUrl}/auth/login`, {}).subscribe();

    const req = httpMock.expectOne(`${environment.userServiceUrl}/auth/login`);
    expect(req.request.headers.has('Authorization')).toBe(false);
    req.flush({});
  });

  it('attempts a silent refresh-and-retry on a 401, then succeeds with the new token', () => {
    tokenStorage.setTokens('expired-token', 'valid-refresh-token');

    httpClient.get(`${environment.farmTwinServiceUrl}/farm-twins/me`).subscribe({
      next: (response) => expect(response).toEqual({ ok: true }),
    });

    const firstReq = httpMock.expectOne(`${environment.farmTwinServiceUrl}/farm-twins/me`);
    expect(firstReq.request.headers.get('Authorization')).toBe('Bearer expired-token');
    firstReq.flush('Unauthorized', { status: 401, statusText: 'Unauthorized' });

    const refreshReq = httpMock.expectOne(`${environment.userServiceUrl}/auth/refresh`);
    refreshReq.flush({
      accessToken: 'new-access-token',
      refreshToken: 'new-refresh-token',
      accessTokenExpiresInSeconds: 900,
      user: { id: 'u1', phone: '9876543210', fullName: 'Test', role: 'FARMER', tier: 'FREE', stateCode: null, districtCode: null, languagePreference: 'hi', createdAt: '2026-01-01T00:00:00Z' },
    });

    const retriedReq = httpMock.expectOne(`${environment.farmTwinServiceUrl}/farm-twins/me`);
    expect(retriedReq.request.headers.get('Authorization')).toBe('Bearer new-access-token');
    retriedReq.flush({ ok: true });
  });

  it('clears tokens and redirects to /login when refresh itself fails', () => {
    tokenStorage.setTokens('expired-token', 'invalid-refresh-token');
    const router = TestBed.inject(Router);
    const navigateSpy = spyOn(router, 'navigate');

    httpClient.get(`${environment.farmTwinServiceUrl}/farm-twins/me`).subscribe({
      error: () => {},
    });

    const firstReq = httpMock.expectOne(`${environment.farmTwinServiceUrl}/farm-twins/me`);
    firstReq.flush('Unauthorized', { status: 401, statusText: 'Unauthorized' });

    const refreshReq = httpMock.expectOne(`${environment.userServiceUrl}/auth/refresh`);
    refreshReq.flush('Invalid refresh token', { status: 401, statusText: 'Unauthorized' });

    expect(tokenStorage.getAccessToken()).toBeNull();
    expect(navigateSpy).toHaveBeenCalledWith(['/login']);
  });
});
