import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { authGuard } from './auth.guard';
import { TokenStorageService } from '../services/token-storage.service';

describe('authGuard', () => {
  let tokenStorage: TokenStorageService;
  let router: Router;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideRouter([])],
    });
    tokenStorage = TestBed.inject(TokenStorageService);
    router = TestBed.inject(Router);
    localStorage.clear();
  });

  function runGuard(): boolean {
    return TestBed.runInInjectionContext(() => authGuard({} as never, {} as never)) as boolean;
  }

  it('allows navigation when an access token is present', () => {
    tokenStorage.setTokens('token', 'refresh');
    expect(runGuard()).toBe(true);
  });

  it('blocks navigation and redirects to /login when no access token is present', () => {
    const navigateSpy = spyOn(router, 'navigate');
    expect(runGuard()).toBe(false);
    expect(navigateSpy).toHaveBeenCalledWith(['/login']);
  });
});
