import { Component, ChangeDetectionStrategy, Input } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-error-banner',
  standalone: true,
  imports: [MatIconModule],
  templateUrl: './error-banner.component.html',
  styleUrls: ['./error-banner.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ErrorBannerComponent {
  @Input({ required: true }) message!: string;
}
