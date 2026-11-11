import { Component, ChangeDetectionStrategy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

export interface RejectDialogData {
  loanId: string;
  amount: number;
}

@Component({
  selector: 'app-reject-dialog',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
  ],
  template: `
    <div class="text-center px-6 pt-6 pb-2">
      <div class="w-14 h-14 rounded-full bg-red-50 flex items-center justify-center mx-auto mb-4">
        <mat-icon class="!text-[28px] !w-7 !h-7 text-red-500">warning_amber</mat-icon>
      </div>
      <h2 mat-dialog-title class="!text-lg !font-bold !text-gray-900 !mb-2 !tracking-tight">Rechazar Solicitud</h2>
      <p class="text-gray-500 text-sm leading-normal m-0">Esta acción no se puede deshacer. El solicitante será notificado del rechazo.</p>
    </div>
    <mat-dialog-content class="!pt-4">
      <mat-form-field appearance="outline" class="w-full">
        <mat-label>Motivo del rechazo</mat-label>
        <textarea matInput
                  [(ngModel)]="reason"
                  rows="4"
                  placeholder="Describa el motivo por el cual se rechaza esta solicitud..."
                  required
                  maxlength="500"
                  aria-label="Motivo del rechazo"
                  aria-required="true"
                  id="reject-reason"></textarea>
        <mat-hint align="end">{{ reason.length }} / 500</mat-hint>
        @if (showError) {
          <mat-error>El motivo del rechazo es obligatorio</mat-error>
        }
      </mat-form-field>
    </mat-dialog-content>
    <mat-dialog-actions align="end" class="!px-6 !pb-6 !pt-4 !gap-3">
      <button mat-stroked-button (click)="onCancel()" class="!inline-flex !items-center !gap-2 !rounded-lg !font-semibold">Cancelar</button>
      <button mat-flat-button color="warn" (click)="onConfirm()" [disabled]="!reason.trim()" class="!inline-flex !items-center !gap-2 !rounded-lg !font-semibold">
        <mat-icon>block</mat-icon>
        Confirmar Rechazo
      </button>
    </mat-dialog-actions>
  `,
  styles: [`:host { display: block; }`],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RejectDialogComponent {
  private readonly dialogRef = inject(MatDialogRef<RejectDialogComponent>);
  readonly data: RejectDialogData = inject(MAT_DIALOG_DATA);

  reason = '';
  showError = false;

  onCancel(): void {
    this.dialogRef.close();
  }

  onConfirm(): void {
    if (!this.reason.trim()) {
      this.showError = true;
      return;
    }
    this.dialogRef.close(this.reason.trim());
  }
}
