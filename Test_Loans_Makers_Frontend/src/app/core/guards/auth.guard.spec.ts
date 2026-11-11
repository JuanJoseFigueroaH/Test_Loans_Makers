import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { authGuard, guestGuard } from './auth.guard';
import { AuthStore } from '../store/auth.store';
import { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';

describe('AuthGuard', () => {
  let authStore: InstanceType<typeof AuthStore>;
  let router: Router;

  const mockRoute = {} as ActivatedRouteSnapshot;
  const mockState = {} as RouterStateSnapshot;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        AuthStore,
        { provide: Router, useValue: { createUrlTree: jasmine.createSpy('createUrlTree').and.returnValue('/login'), navigate: jasmine.createSpy('navigate') } },
      ],
    });

    authStore = TestBed.inject(AuthStore);
    router = TestBed.inject(Router);
  });

  describe('authGuard', () => {
    it('should allow access when authenticated', () => {
      authStore.setAuthenticated(
        { id: '1', email: 'test@test.com', role: 'ROLE_USER', enabled: true, createdAt: '' },
        'token'
      );

      const result = TestBed.runInInjectionContext(() => authGuard(mockRoute, mockState));

      expect(result).toBe(true);
    });

    it('should redirect to login when not authenticated', () => {
      TestBed.runInInjectionContext(() => authGuard(mockRoute, mockState));

      expect(router.createUrlTree).toHaveBeenCalledWith(['/login']);
    });
  });

  describe('guestGuard', () => {
    it('should allow access when not authenticated', () => {
      const result = TestBed.runInInjectionContext(() => guestGuard(mockRoute, mockState));

      expect(result).toBe(true);
    });

    it('should redirect admin to /admin when authenticated', () => {
      authStore.setAuthenticated(
        { id: '1', email: 'admin@test.com', role: 'ROLE_ADMIN', enabled: true, createdAt: '' },
        'token'
      );

      TestBed.runInInjectionContext(() => guestGuard(mockRoute, mockState));

      expect(router.createUrlTree).toHaveBeenCalledWith(['/admin']);
    });

    it('should redirect user to /dashboard when authenticated', () => {
      authStore.setAuthenticated(
        { id: '1', email: 'user@test.com', role: 'ROLE_USER', enabled: true, createdAt: '' },
        'token'
      );

      TestBed.runInInjectionContext(() => guestGuard(mockRoute, mockState));

      expect(router.createUrlTree).toHaveBeenCalledWith(['/dashboard']);
    });
  });
});
