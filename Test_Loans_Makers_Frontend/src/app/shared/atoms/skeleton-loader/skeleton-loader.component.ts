import { Component, ChangeDetectionStrategy, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-skeleton-loader',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './skeleton-loader.component.html',
  styleUrls: ['./skeleton-loader.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SkeletonLoaderComponent {
  @Input() width: 'short' | 'medium' | 'wide' | 'full' = 'full';
  @Input() height: 'sm' | 'md' | 'lg' = 'sm';

  get sizeClass(): string {
    const w = {
      short: 'w-[30%]',
      medium: 'w-[50%]',
      wide: 'w-[70%]',
      full: 'w-full',
    }[this.width];

    const h = {
      sm: 'h-3.5',
      md: 'h-5',
      lg: 'h-6',
    }[this.height];

    return `${w} ${h}`;
  }
}
