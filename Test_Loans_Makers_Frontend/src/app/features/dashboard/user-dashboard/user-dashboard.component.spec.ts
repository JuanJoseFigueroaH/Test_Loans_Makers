import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { UserDashboardComponent } from './user-dashboard.component';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter, Router } from '@angular/router';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { AuthStore } from '../../../core/store/auth.store';
import { LoanStore } from '../../../core/store/loan.store';

describe('UserDashboardComponent', () => {
  let component: UserDashboardComponent;
  let fixture: ComponentFixture<UserDashboardComponent>;
  let httpTesting: HttpTestingController;
  let router: Router;
  let authStore: InstanceType<typeof AuthStore>;
  let loanStore: InstanceType<typeof LoanStore>;

  const mockPagedResponse = {
    content: [
      { id: '1', userId: 'u1', amount: 5000000, termMonths: 12, status: 'PENDING', createdAt: '2024-01-01', updatedAt: '2024-01-01' },
    ],
    page: 0, size: 10, totalElements: 1, totalPages: 1, first: true, last: true,
  };

  beforeEach(async () => {
    sessionStorage.clear();
    await TestBed.configureTestingModule({
      imports: [UserDashboardComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([
          { path: 'login', component: UserDashboardComponent },
          { path: 'loans/new', component: UserDashboardComponent },
          { path: 'loans/:id', component: UserDashboardComponent },
        ]),
        provideAnimationsAsync(),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(UserDashboardComponent);
    component = fixture.componentInstance;
    httpTesting = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    authStore = TestBed.inject(AuthStore);
    loanStore = TestBed.inject(LoanStore);
  });

  afterEach(() => {
    httpTesting.verify();
    sessionStorage.clear();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have ALL as default filter', () => {
    expect(component.statusFilter()).toBe('ALL');
  });

  it('should have 3 skeleton items', () => {
    expect(component.skeletonItems.length).toBe(3);
  });

  it('should load loans on init', () => {
    component.ngOnInit();
    const req = httpTesting.expectOne(r => r.url.includes('/loans') && !r.url.includes('/admin'));
    req.flush(mockPagedResponse);
    expect(loanStore.loans().length).toBe(1);
  });

  it('should handle loadLoans error', fakeAsync(() => {
    component.loadLoans();
    for (let i = 0; i < 3; i++) {
      const req = httpTesting.expectOne(r => r.url.includes('/loans') && !r.url.includes('/admin'));
      req.flush({ detail: 'Server error' }, { status: 500, statusText: 'Error' });
      if (i < 2) tick(1000);
    }
    tick();
    expect(loanStore.error()).toBe('Server error');
  }));

  it('should set filter and reload', () => {
    component.setFilter('APPROVED');
    const req = httpTesting.expectOne(r => r.url.includes('/loans') && !r.url.includes('/admin'));
    expect(req.request.params.get('status')).toBe('APPROVED');
    req.flush(mockPagedResponse);
    expect(component.statusFilter()).toBe('APPROVED');
  });

  it('should handle setFilter error', fakeAsync(() => {
    component.setFilter('PENDING');
    for (let i = 0; i < 3; i++) {
      const req = httpTesting.expectOne(r => r.url.includes('/loans') && !r.url.includes('/admin'));
      req.flush({}, { status: 500, statusText: 'Error' });
      if (i < 2) tick(1000);
    }
    tick();
    expect(loanStore.error()).toBeTruthy();
  }));

  it('should go to page', () => {
    component.goToPage(2);
    const req = httpTesting.expectOne(r => r.url.includes('/loans') && !r.url.includes('/admin'));
    expect(req.request.params.get('page')).toBe('2');
    req.flush(mockPagedResponse);
  });

  it('should handle goToPage error', fakeAsync(() => {
    component.goToPage(1);
    for (let i = 0; i < 3; i++) {
      const req = httpTesting.expectOne(r => r.url.includes('/loans') && !r.url.includes('/admin'));
      req.flush({}, { status: 500, statusText: 'Error' });
      if (i < 2) tick(1000);
    }
    tick();
    expect(loanStore.error()).toBeTruthy();
  }));

  it('should navigate to loan detail', () => {
    spyOn(router, 'navigate');
    component.viewDetail('loan-1');
    expect(router.navigate).toHaveBeenCalledWith(['/loans', 'loan-1']);
  });

  it('should navigate to create loan', () => {
    spyOn(router, 'navigate');
    component.navigateToCreateLoan();
    expect(router.navigate).toHaveBeenCalledWith(['/loans/new']);
  });

  it('should logout and navigate to login', () => {
    spyOn(router, 'navigate');
    authStore.setAuthenticated(
      { id: '1', email: 'user@test.com', role: 'ROLE_USER', enabled: true, createdAt: '' },
      'token'
    );
    component.logout();
    expect(authStore.isAuthenticated()).toBeFalse();
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });

  it('should compute kpi items', () => {
    loanStore.setLoans(
      [
        { id: '1', userId: 'u1', amount: 1000, termMonths: 12, status: 'PENDING', createdAt: '', updatedAt: '' },
        { id: '2', userId: 'u1', amount: 2000, termMonths: 24, status: 'APPROVED', createdAt: '', updatedAt: '' },
      ],
      0, 10, 2, 1, true, true,
    );
    const kpis = component.kpiItems();
    expect(kpis.length).toBe(4);
    expect(kpis[0].filterKey).toBe('ALL');
    expect(kpis[1].filterKey).toBe('PENDING');
  });
});
