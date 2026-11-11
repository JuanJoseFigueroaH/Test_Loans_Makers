import { Component, ChangeDetectionStrategy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { AuthStore } from '../../../core/store/auth.store';
import { UserLayoutComponent, PageHeaderComponent } from '../../../shared';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    UserLayoutComponent,
    PageHeaderComponent,
  ],
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminDashboardComponent {
  readonly authStore = inject(AuthStore);
  private readonly router = inject(Router);

  logout(): void {
    this.authStore.logout();
    this.router.navigate(['/login']);
  }
}
