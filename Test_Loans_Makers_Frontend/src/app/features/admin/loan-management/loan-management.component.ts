import { Component, ChangeDetectionStrategy, inject, OnInit, OnDestroy, signal } from '@angular/core';
import { CommonModule, CurrencyPipe, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDividerModule } from '@angular/material/divider';
import { HttpErrorResponse } from '@angular/common/http';
import { LoanService } from '../../../core/services/loan.service';
import { AdminLoanStore } from '../../../core/store/admin-loan.store';
import { AuthStore } from '../../../core/store/auth.store';
import { Loan, LoanStatus } from '../../../core/models/loan.model';
import { RejectDialogComponent } from './reject-dialog.component';
import {
  StatusBadgeComponent,
  UserLayoutComponent,
  PageHeaderComponent,
  InfoItemComponent,
  ErrorBannerComponent,
  EmptyStateComponent,
  PaginationComponent,
} from '../../../shared';

@Component({
  selector: 'app-loan-management',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatTooltipModule,
    MatDividerModule,
    CurrencyPipe,
    DatePipe,
    StatusBadgeComponent,
    UserLayoutComponent,
    PageHeaderComponent,
    InfoItemComponent,
    ErrorBannerComponent,
    EmptyStateComponent,
    PaginationComponent,
  ],
  templateUrl: './loan-management.component.html',
  styleUrls: ['./loan-management.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LoanManagementComponent implements OnInit, OnDestroy {
  private readonly loanService = inject(LoanService);
  readonly store = inject(AdminLoanStore);
  readonly authStore = inject(AuthStore);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);
  private readonly dialog = inject(MatDialog);

  readonly statusFilter = signal<LoanStatus | 'ALL'>('ALL');
  readonly searchTerm = signal('');
  readonly sidebarOpen = signal(true);
  readonly activeView = signal<'list' | 'detail'>('list');
  readonly skeletonItems = Array(4);

  ngOnInit(): void {
    this.loadLoans();
    this.loadSummary();
  }

  ngOnDestroy(): void {
    this.store.clearMessages();
  }

  loadLoans(): void {
    this.store.setLoading();
    const filter = this.statusFilter();
    const status = filter === 'ALL' ? undefined : filter;
    const search = this.searchTerm().trim() || undefined;
    this.loanService.getAdminLoans(this.store.page(), this.store.size(), status, search).subscribe({
      next: (res) => {
        this.store.setLoans(res.content, res.page, res.size, res.totalElements, res.totalPages, res.first, res.last);
      },
      error: (err: HttpErrorResponse) => {
        this.store.setError(err.error?.detail || 'Error al cargar solicitudes');
      },
    });
  }

  loadSummary(): void {
    this.loanService.getAdminSummary().subscribe({
      next: (summary) => this.store.setSummary(summary),
      error: () => { /* Summary load failure is non-critical */ },
    });
  }

  setFilter(status: LoanStatus | 'ALL'): void {
    this.statusFilter.set(status);
    this.searchTerm.set('');
    this.store.setLoading();
    const filterStatus = status === 'ALL' ? undefined : status;
    this.loanService.getAdminLoans(0, this.store.size(), filterStatus).subscribe({
      next: (res) => {
        this.store.setLoans(res.content, res.page, res.size, res.totalElements, res.totalPages, res.first, res.last);
      },
      error: (err: HttpErrorResponse) => {
        this.store.setError(err.error?.detail || 'Error al cargar solicitudes');
      },
    });
  }

  onSearch(term: string): void {
    this.searchTerm.set(term);
    this.statusFilter.set('ALL');
    this.store.setLoading();
    const search = term.trim() || undefined;
    this.loanService.getAdminLoans(0, this.store.size(), undefined, search).subscribe({
      next: (res) => {
        this.store.setLoans(res.content, res.page, res.size, res.totalElements, res.totalPages, res.first, res.last);
      },
      error: (err: HttpErrorResponse) => {
        this.store.setError(err.error?.detail || 'Error al cargar solicitudes');
      },
    });
  }

  goToPage(page: number): void {
    this.store.setLoading();
    const filter = this.statusFilter();
    const status = filter === 'ALL' ? undefined : filter;
    const search = this.searchTerm().trim() || undefined;
    this.loanService.getAdminLoans(page, this.store.size(), status, search).subscribe({
      next: (res) => {
        this.store.setLoans(res.content, res.page, res.size, res.totalElements, res.totalPages, res.first, res.last);
      },
      error: (err: HttpErrorResponse) => {
        this.store.setError(err.error?.detail || 'Error al cargar solicitudes');
      },
    });
  }

  viewDetail(loan: Loan): void {
    this.store.setSelectedLoan(loan);
    this.activeView.set('detail');
  }

  backToList(): void {
    this.store.clearSelectedLoan();
    this.activeView.set('list');
  }

  approveLoan(loan: Loan): void {
    if (this.store.isActionInProgress()) return;
    this.store.setActionLoading(loan.id);
    this.loanService.approveLoan(loan.id).subscribe({
      next: (updated) => {
        this.store.updateLoan(updated);
        this.store.setSuccess('Préstamo aprobado exitosamente');
        this.loadSummary();
        this.snackBar.open('Préstamo aprobado exitosamente', 'Cerrar', {
          duration: 4000, panelClass: ['snackbar-success'],
        });
      },
      error: (err: HttpErrorResponse) => this.handleActionError(err),
    });
  }

  openRejectDialog(loan: Loan): void {
    if (this.store.isActionInProgress()) return;
    const dialogRef = this.dialog.open(RejectDialogComponent, {
      width: '480px',
      disableClose: true,
      data: { loanId: loan.id, amount: loan.amount },
    });
    dialogRef.afterClosed().subscribe((reason: string | undefined) => {
      if (reason) this.rejectLoan(loan, reason);
    });
  }

  private rejectLoan(loan: Loan, reason: string): void {
    this.store.setActionLoading(loan.id);
    this.loanService.rejectLoan(loan.id, reason).subscribe({
      next: (updated) => {
        this.store.updateLoan(updated);
        this.store.setSuccess('Préstamo rechazado');
        this.loadSummary();
        this.snackBar.open('Préstamo rechazado', 'Cerrar', {
          duration: 4000, panelClass: ['snackbar-info'],
        });
      },
      error: (err: HttpErrorResponse) => this.handleActionError(err),
    });
  }

  private handleActionError(err: HttpErrorResponse): void {
    const message = err.error?.detail || 'Error al procesar la solicitud';
    if (err.status === 409) {
      this.store.setError('Conflicto: ' + message);
      this.snackBar.open('Conflicto: la solicitud fue modificada. Recargando...', 'Cerrar', {
        duration: 5000, panelClass: ['snackbar-warning'],
      });
      this.loadLoans();
      this.loadSummary();
    } else {
      this.store.setError(message);
      this.snackBar.open(message, 'Cerrar', { duration: 5000, panelClass: ['snackbar-error'] });
    }
  }

  toggleSidebar(): void {
    this.sidebarOpen.set(!this.sidebarOpen());
  }

  logout(): void {
    this.authStore.logout();
    this.router.navigate(['/login']);
  }
}
