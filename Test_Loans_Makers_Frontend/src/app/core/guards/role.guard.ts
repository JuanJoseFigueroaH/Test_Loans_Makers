import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthStore } from '../store/auth.store';
import { UserRole } from '../models/user.model';

export function roleGuard(requiredRole: UserRole): CanActivateFn {
  return () => {
    const authStore = inject(AuthStore);
    const router = inject(Router);

    if (!authStore.isAuthenticated()) {
      return router.createUrlTree(['/login']);
    }

    if (authStore.userRole() === requiredRole) {
      return true;
    }

    return router.createUrlTree(['/unauthorized']);
  };
}
