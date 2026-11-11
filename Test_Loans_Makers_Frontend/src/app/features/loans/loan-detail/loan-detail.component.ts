import { Component, ChangeDetectionStrategy, inject, OnInit } from '@angular/core';
import { CommonModule, CurrencyPipe, DatePipe } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDividerModule } from '@angular/material/divider';
import { HttpErrorResponse } from '@angular/common/http';
import { LoanStore } from '../../../core/store/loan.store';
import { LoanService } from '../../../core/services/loan.service';
import { Loan } from '../../../core/models/loan.model';
import {
  StatusBadgeComponent,
  InfoItemComponent,
  ErrorBannerComponent,
} from '../../../shared';

@Component({
  selector: 'app-loan-detail',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatDividerModule,
    CurrencyPipe,
    DatePipe,
    StatusBadgeComponent,
    InfoItemComponent,
    ErrorBannerComponent,
  ],
  templateUrl: './loan-detail.component.html',
  styleUrls: ['./loan-detail.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LoanDetailComponent implements OnInit {
  readonly store = inject(LoanStore);
  private readonly loanService = inject(LoanService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadLoan(id);
    }
  }

  private loadLoan(id: string): void {
    this.store.setDetailLoading();
    this.loanService.getLoanById(id).subscribe({
      next: (loan: Loan) => this.store.setSelectedLoan(loan),
      error: (err: HttpErrorResponse) => {
        const message = err.status === 404
          ? 'Solicitud no encontrada'
          : (err.error?.detail || 'Error al cargar la solicitud');
        this.store.setError(message);
      },
    });
  }

  getStatusIcon(status: string): string {
    switch (status) {
      case 'PENDING': return 'hourglass_empty';
      case 'APPROVED': return 'check_circle';
      case 'REJECTED': return 'cancel';
      default: return 'help';
    }
  }

  goBack(): void {
    this.router.navigate(['/dashboard']);
  }
}
