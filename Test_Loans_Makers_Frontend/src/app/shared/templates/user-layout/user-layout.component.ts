import { Component, ChangeDetectionStrategy, Input, Output, EventEmitter } from '@angular/core';
import { AppHeaderComponent } from '../../organisms/app-header/app-header.component';

@Component({
  selector: 'app-user-layout',
  standalone: true,
  imports: [AppHeaderComponent],
  templateUrl: './user-layout.component.html',
  styleUrls: ['./user-layout.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UserLayoutComponent {
  @Input() userEmail?: string;
  @Output() logoutClick = new EventEmitter<void>();
}
