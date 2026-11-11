import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { roleGuard } from './role.guard';
import { AuthStore } from '../store/auth.store';
import { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';

describe('RoleGuard', () => {
  let authStore: InstanceType<typeof AuthStore>;
  let router: Router;

  const mockRoute = {} as ActivatedRouteSnapshot;
  const mockState = {} as RouterStateSnapshot;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        AuthStore,
        {
          provide: Router,
          useValue: {
            createUrlTree: jasmine.createSpy('createUrlTree').and.callFake((path: string[]) => path),
          },
        },
      ],
    });

    authStore = TestBed.inject(AuthStore);
    router = TestBed.inject(Router);
  });

  it('should redirect to login when not authenticated', () => {
    const guard = roleGuard('ROLE_ADMIN');
    TestBed.runInInjectionContext(() => guard(mockRoute, mockState));

    expect(router.createUrlTree).toHaveBeenCalledWith(['/login']);
  });

  it('should allow access when user has required role', () => {
    authStore.setAuthenticated(
      { id: '1', email: 'admin@test.com', role: 'ROLE_ADMIN', enabled: true, createdAt: '' },
      'token'
    );

    const guard = roleGuard('ROLE_ADMIN');
    const result = TestBed.runInInjectionContext(() => guard(mockRoute, mockState));

    expect(result).toBe(true);
  });

  it('should redirect to unauthorized when role does not match', () => {
    authStore.setAuthenticated(
      { id: '1', email: 'user@test.com', role: 'ROLE_USER', enabled: true, createdAt: '' },
      'token'
    );

    const guard = roleGuard('ROLE_ADMIN');
    TestBed.runInInjectionContext(() => guard(mockRoute, mockState));

    expect(router.createUrlTree).toHaveBeenCalledWith(['/unauthorized']);
  });

  it('should allow ROLE_USER to access user routes', () => {
    authStore.setAuthenticated(
      { id: '1', email: 'user@test.com', role: 'ROLE_USER', enabled: true, createdAt: '' },
      'token'
    );

    const guard = roleGuard('ROLE_USER');
    const result = TestBed.runInInjectionContext(() => guard(mockRoute, mockState));

    expect(result).toBe(true);
  });
});
