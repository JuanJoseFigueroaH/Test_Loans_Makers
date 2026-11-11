import { Component, ChangeDetectionStrategy, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-info-item',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './info-item.component.html',
  styleUrls: ['./info-item.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class InfoItemComponent {
  @Input({ required: true }) label!: string;
  @Input({ required: true }) value!: string;
  @Input() mono = false;
}
