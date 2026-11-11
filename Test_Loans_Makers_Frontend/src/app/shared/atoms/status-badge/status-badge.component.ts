import { Component, ChangeDetectionStrategy, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-status-badge',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './status-badge.component.html',
  styleUrls: ['./status-badge.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class StatusBadgeComponent {
  @Input({ required: true }) status!: string;
  @Input() size: 'sm' | 'lg' = 'sm';

  get label(): string {
    switch (this.status) {
      case 'PENDING': return 'PENDIENTE';
      case 'APPROVED': return 'APROBADO';
      case 'REJECTED': return 'RECHAZADO';
      default: return this.status;
    }
  }

  get dotClass(): string {
    switch (this.status) {
      case 'PENDING': return 'bg-amber-500';
      case 'APPROVED': return 'bg-emerald-500';
      case 'REJECTED': return 'bg-red-500';
      default: return 'bg-gray-500';
    }
  }

  get badgeClasses(): string {
    const base = 'inline-flex items-center gap-1.5 rounded-full font-semibold uppercase tracking-wide';
    const sizeClass = this.size === 'lg'
      ? 'px-4 py-1.5 text-xs'
      : 'px-3 py-0.5 text-[11px]';

    let colorClass = '';
    switch (this.status) {
      case 'PENDING':
        colorClass = 'bg-amber-100 text-amber-700';
        break;
      case 'APPROVED':
        colorClass = 'bg-emerald-100 text-emerald-700';
        break;
      case 'REJECTED':
        colorClass = 'bg-red-100 text-red-700';
        break;
      default:
        colorClass = 'bg-gray-100 text-gray-700';
    }

    return `${base} ${sizeClass} ${colorClass}`;
  }
}
