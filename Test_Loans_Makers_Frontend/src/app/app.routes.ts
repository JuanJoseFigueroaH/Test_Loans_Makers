import { Routes } from '@angular/router';
import { authGuard, guestGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';

export const routes: Routes = [
  {
    path: 'login',
    canActivate: [guestGuard],
    loadComponent: () =>
      import('./features/auth/login/login.component').then(m => m.LoginComponent),
  },
  {
    path: 'dashboard',
    canActivate: [authGuard, roleGuard('ROLE_USER')],
    loadComponent: () =>
      import('./features/dashboard/user-dashboard/user-dashboard.component').then(m => m.UserDashboardComponent),
  },
  {
    path: 'loans/new',
    canActivate: [authGuard, roleGuard('ROLE_USER')],
    loadComponent: () =>
      import('./features/loans/create-loan/create-loan.component').then(m => m.CreateLoanComponent),
  },
  {
    path: 'loans/:id',
    canActivate: [authGuard, roleGuard('ROLE_USER')],
    loadComponent: () =>
      import('./features/loans/loan-detail/loan-detail.component').then(m => m.LoanDetailComponent),
  },
  {
    path: 'admin',
    canActivate: [authGuard, roleGuard('ROLE_ADMIN')],
    loadComponent: () =>
      import('./features/admin/loan-management/loan-management.component').then(m => m.LoanManagementComponent),
  },
  {
    path: '',
    redirectTo: 'login',
    pathMatch: 'full',
  },
  {
    path: '**',
    redirectTo: 'login',
  },
];
