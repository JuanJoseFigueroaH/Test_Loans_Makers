import { Component, ChangeDetectionStrategy, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-kpi-card',
  standalone: true,
  imports: [CommonModule, MatIconModule],
  templateUrl: './kpi-card.component.html',
  styleUrls: ['./kpi-card.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class KpiCardComponent {
  @Input({ required: true }) icon!: string;
  @Input({ required: true }) value!: number | string;
  @Input({ required: true }) label!: string;
  @Input() color: 'primary' | 'warning' | 'success' | 'danger' = 'primary';
  @Input() active = false;
  @Output() cardClick = new EventEmitter<void>();

  get iconBgClass(): string {
    switch (this.color) {
      case 'primary': return 'bg-blue-600';
      case 'warning': return 'bg-amber-500';
      case 'success': return 'bg-emerald-500';
      case 'danger': return 'bg-red-500';
      default: return 'bg-blue-600';
    }
  }
}
