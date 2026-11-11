import { Component, ChangeDetectionStrategy, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule, CurrencyPipe, DatePipe } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { StatusBadgeComponent } from '../../atoms/status-badge/status-badge.component';

@Component({
  selector: 'app-loan-card',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatButtonModule, MatIconModule, CurrencyPipe, DatePipe, StatusBadgeComponent],
  templateUrl: './loan-card.component.html',
  styleUrls: ['./loan-card.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LoanCardComponent {
  @Input({ required: true }) status!: string;
  @Input({ required: true }) amount!: number;
  @Input({ required: true }) termMonths!: number;
  @Input({ required: true }) date!: string;
  @Input() rejectionReason?: string;
  @Input() reviewedAt?: string;
  @Output() cardClick = new EventEmitter<void>();

  get statusLabel(): string {
    switch (this.status) {
      case 'PENDING': return 'Pendiente';
      case 'APPROVED': return 'Aprobado';
      case 'REJECTED': return 'Rechazado';
      default: return this.status;
    }
  }
}
