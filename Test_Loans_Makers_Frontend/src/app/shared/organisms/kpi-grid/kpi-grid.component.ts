import { Component, ChangeDetectionStrategy, Input, Output, EventEmitter } from '@angular/core';
import { KpiCardComponent } from '../../molecules/kpi-card/kpi-card.component';

export interface KpiItem {
  icon: string;
  value: number | string;
  label: string;
  color: 'primary' | 'warning' | 'success' | 'danger';
  filterKey: string;
  active?: boolean;
}

@Component({
  selector: 'app-kpi-grid',
  standalone: true,
  imports: [KpiCardComponent],
  templateUrl: './kpi-grid.component.html',
  styleUrls: ['./kpi-grid.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class KpiGridComponent {
  @Input({ required: true }) items: KpiItem[] = [];
  @Output() kpiClick = new EventEmitter<string>();
}
