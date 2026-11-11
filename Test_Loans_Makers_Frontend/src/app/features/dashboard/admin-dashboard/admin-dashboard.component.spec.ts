import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AdminDashboardComponent } from './admin-dashboard.component';
import { provideRouter, Router } from '@angular/router';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { AuthStore } from '../../../core/store/auth.store';

describe('AdminDashboardComponent', () => {
  let component: AdminDashboardComponent;
  let fixture: ComponentFixture<AdminDashboardComponent>;
  let router: Router;
  let authStore: InstanceType<typeof AuthStore>;

  beforeEach(async () => {
    sessionStorage.clear();
    await TestBed.configureTestingModule({
      imports: [AdminDashboardComponent],
      providers: [
        provideRouter([{ path: 'login', component: AdminDashboardComponent }]),
        provideAnimationsAsync(),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(AdminDashboardComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    authStore = TestBed.inject(AuthStore);
  });

  afterEach(() => {
    sessionStorage.clear();
  });

  it('should create', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should logout and navigate to login', () => {
    spyOn(router, 'navigate');
    authStore.setAuthenticated(
      { id: '1', email: 'admin@test.com', role: 'ROLE_ADMIN', enabled: true, createdAt: '' },
      'token'
    );
    component.logout();
    expect(authStore.isAuthenticated()).toBeFalse();
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });
});
