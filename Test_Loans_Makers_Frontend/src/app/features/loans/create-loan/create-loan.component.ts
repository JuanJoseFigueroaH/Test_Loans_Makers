import { Component, ChangeDetectionStrategy, inject, OnDestroy } from '@angular/core';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDividerModule } from '@angular/material/divider';
import { HttpErrorResponse } from '@angular/common/http';
import { LoanService } from '../../../core/services/loan.service';
import { StatusBadgeComponent } from '../../../shared/atoms/status-badge/status-badge.component';
import { LoanStore } from '../../../core/store/loan.store';
import { AuthStore } from '../../../core/store/auth.store';

@Component({
  selector: 'app-create-loan',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatDividerModule,
    CurrencyPipe,
    StatusBadgeComponent,
  ],
  templateUrl: './create-loan.component.html',
  styleUrls: ['./create-loan.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CreateLoanComponent implements OnDestroy {
  private readonly fb = inject(FormBuilder);
  private readonly loanService = inject(LoanService);
  private readonly loanStore = inject(LoanStore);
  private readonly authStore = inject(AuthStore);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);

  readonly loading = this.loanStore.loading;
  readonly error = this.loanStore.error;
  readonly success = this.loanStore.success;
  readonly createdLoan = this.loanStore.createdLoan;

  readonly termOptions = [
    { value: 6, label: '6 meses' },
    { value: 12, label: '12 meses' },
    { value: 24, label: '24 meses' },
    { value: 36, label: '36 meses' },
    { value: 48, label: '48 meses' },
    { value: 60, label: '60 meses' },
  ];

  loanForm: FormGroup = this.fb.group({
    amount: [null, [Validators.required, Validators.min(0.01)]],
    termMonths: [null, [Validators.required, Validators.min(1)]],
  });

  ngOnDestroy(): void {
    this.loanStore.clearState();
  }

  onSubmit(): void {
    if (this.loanForm.invalid) {
      this.loanForm.markAllAsTouched();
      return;
    }

    this.loanStore.setLoading();
    const { amount, termMonths } = this.loanForm.getRawValue();

    this.loanService.createLoan({ amount, termMonths }).subscribe({
      next: (loan) => {
        this.loanStore.setCreated(loan);
        this.snackBar.open('Solicitud de préstamo creada exitosamente', 'Cerrar', {
          duration: 5000,
          panelClass: ['snackbar-success'],
        });
      },
      error: (err: HttpErrorResponse) => {
        const message = err.error?.detail || 'Error al crear la solicitud de préstamo';
        this.loanStore.setError(message);
        this.snackBar.open(message, 'Cerrar', {
          duration: 5000,
          panelClass: ['snackbar-error'],
        });
      },
    });
  }

  getFieldError(field: string): string {
    const control = this.loanForm.get(field);
    if (control?.hasError('required')) {
      return 'Este campo es obligatorio';
    }
    if (control?.hasError('min')) {
      return field === 'amount'
        ? 'El monto debe ser mayor a cero'
        : 'El plazo debe ser mayor a cero';
    }
    return '';
  }

  newRequest(): void {
    this.loanForm.reset();
    this.loanStore.clearState();
  }

  goToDashboard(): void {
    this.router.navigate(['/dashboard']);
  }
}
