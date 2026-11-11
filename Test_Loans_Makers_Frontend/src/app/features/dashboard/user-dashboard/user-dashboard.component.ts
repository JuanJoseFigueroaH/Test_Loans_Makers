import { Component, ChangeDetectionStrategy, computed, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { HttpErrorResponse } from '@angular/common/http';
import { AuthStore } from '../../../core/store/auth.store';
import { LoanStore } from '../../../core/store/loan.store';
import { LoanService } from '../../../core/services/loan.service';
import { LoanStatus } from '../../../core/models/loan.model';
import {
  UserLayoutComponent,
  KpiGridComponent, KpiItem,
  LoanCardComponent,
  PageHeaderComponent,
  PaginationComponent,
  ErrorBannerComponent,
  EmptyStateComponent,
  SkeletonLoaderComponent,
} from '../../../shared';

@Component({
  selector: 'app-user-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatProgressSpinnerModule,
    UserLayoutComponent,
    KpiGridComponent,
    LoanCardComponent,
    PageHeaderComponent,
    PaginationComponent,
    ErrorBannerComponent,
    EmptyStateComponent,
    SkeletonLoaderComponent,
  ],
  templateUrl: './user-dashboard.component.html',
  styleUrls: ['./user-dashboard.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UserDashboardComponent implements OnInit {
  readonly authStore = inject(AuthStore);
  readonly loanStore = inject(LoanStore);
  private readonly loanService = inject(LoanService);
  private readonly router = inject(Router);

  readonly statusFilter = signal<LoanStatus | 'ALL'>('ALL');
  readonly skeletonItems = Array(3);

  readonly kpiItems = computed<KpiItem[]>(() => [
    { icon: 'list_alt',        value: this.loanStore.totalElements(), label: 'Total',      color: 'primary', filterKey: 'ALL',      active: this.statusFilter() === 'ALL' },
    { icon: 'hourglass_empty', value: this.loanStore.pendingCount(),  label: 'Pendientes', color: 'warning', filterKey: 'PENDING',  active: this.statusFilter() === 'PENDING' },
    { icon: 'check_circle',    value: this.loanStore.approvedCount(), label: 'Aprobados',  color: 'success', filterKey: 'APPROVED', active: this.statusFilter() === 'APPROVED' },
    { icon: 'cancel',          value: this.loanStore.rejectedCount(), label: 'Rechazados', color: 'danger',  filterKey: 'REJECTED', active: this.statusFilter() === 'REJECTED' },
  ]);

  ngOnInit(): void {
    this.loadLoans();
  }

  loadLoans(): void {
    this.loanStore.setLoading();
    const filter = this.statusFilter();
    const status = filter === 'ALL' ? undefined : filter;
    this.loanService.getMyLoans(this.loanStore.page(), this.loanStore.size(), status).subscribe({
      next: (res) => {
        this.loanStore.setLoans(res.content, res.page, res.size, res.totalElements, res.totalPages, res.first, res.last);
      },
      error: (err: HttpErrorResponse) => {
        this.loanStore.setError(err.error?.detail || 'Error al cargar préstamos');
      },
    });
  }

  setFilter(status: LoanStatus | 'ALL'): void {
    this.statusFilter.set(status);
    this.loanStore.setLoading();
    const filterStatus = status === 'ALL' ? undefined : status;
    this.loanService.getMyLoans(0, this.loanStore.size(), filterStatus).subscribe({
      next: (res) => {
        this.loanStore.setLoans(res.content, res.page, res.size, res.totalElements, res.totalPages, res.first, res.last);
      },
      error: (err: HttpErrorResponse) => {
        this.loanStore.setError(err.error?.detail || 'Error al cargar préstamos');
      },
    });
  }

  goToPage(page: number): void {
    this.loanStore.setLoading();
    const filter = this.statusFilter();
    const status = filter === 'ALL' ? undefined : filter;
    this.loanService.getMyLoans(page, this.loanStore.size(), status).subscribe({
      next: (res) => {
        this.loanStore.setLoans(res.content, res.page, res.size, res.totalElements, res.totalPages, res.first, res.last);
      },
      error: (err: HttpErrorResponse) => {
        this.loanStore.setError(err.error?.detail || 'Error al cargar préstamos');
      },
    });
  }

  viewDetail(loanId: string): void {
    this.router.navigate(['/loans', loanId]);
  }

  navigateToCreateLoan(): void {
    this.router.navigate(['/loans/new']);
  }

  logout(): void {
    this.authStore.logout();
    this.router.navigate(['/login']);
  }

}
