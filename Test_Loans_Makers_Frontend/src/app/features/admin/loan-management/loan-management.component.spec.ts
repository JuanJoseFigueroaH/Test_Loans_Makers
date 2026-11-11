import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { LoanManagementComponent } from './loan-management.component';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter, Router } from '@angular/router';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { AuthStore } from '../../../core/store/auth.store';

describe('LoanManagementComponent', () => {
  let component: LoanManagementComponent;
  let fixture: ComponentFixture<LoanManagementComponent>;
  let httpTesting: HttpTestingController;
  let router: Router;
  let authStore: InstanceType<typeof AuthStore>;

  const mockPagedResponse = {
    content: [
      { id: '1', userId: 'u1', amount: 1000, termMonths: 12, status: 'PENDING', createdAt: '', updatedAt: '' },
    ],
    page: 0, size: 10, totalElements: 1, totalPages: 1, first: true, last: true,
  };

  beforeEach(async () => {
    sessionStorage.clear();
    await TestBed.configureTestingModule({
      imports: [LoanManagementComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([{ path: 'login', component: LoanManagementComponent }]),
        provideAnimationsAsync(),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(LoanManagementComponent);
    component = fixture.componentInstance;
    httpTesting = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    authStore = TestBed.inject(AuthStore);
  });

  afterEach(() => {
    httpTesting.verify();
    sessionStorage.clear();
  });

  function flushInitRequests(): void {
    const loansReq = httpTesting.expectOne(r => r.url.includes('/admin/loans') && !r.url.includes('/summary'));
    loansReq.flush(mockPagedResponse);
    const summaryReq = httpTesting.expectOne(r => r.url.includes('/admin/loans/summary'));
    summaryReq.flush({ total: 1, pending: 1, approved: 0, rejected: 0 });
  }

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have ALL as default filter', () => {
    expect(component.statusFilter()).toBe('ALL');
  });

  it('should have list as default active view', () => {
    expect(component.activeView()).toBe('list');
  });

  it('should have sidebar open by default', () => {
    expect(component.sidebarOpen()).toBe(true);
  });

  it('should toggle sidebar', () => {
    component.toggleSidebar();
    expect(component.sidebarOpen()).toBe(false);
    component.toggleSidebar();
    expect(component.sidebarOpen()).toBe(true);
  });

  it('should load loans and summary on init', () => {
    component.ngOnInit();
    flushInitRequests();
    expect(component.store.loans().length).toBe(1);
    expect(component.store.summary().total).toBe(1);
  });

  it('should set selected loan and switch to detail view', () => {
    const loan = { id: '1', userId: 'u1', amount: 1000, termMonths: 12, status: 'PENDING' as const, createdAt: '', updatedAt: '' };
    component.viewDetail(loan);
    expect(component.activeView()).toBe('detail');
    expect(component.store.selectedLoan()).toEqual(loan);
  });

  it('should clear selected loan and go back to list', () => {
    const loan = { id: '1', userId: 'u1', amount: 1000, termMonths: 12, status: 'PENDING' as const, createdAt: '', updatedAt: '' };
    component.viewDetail(loan);
    component.backToList();
    expect(component.activeView()).toBe('list');
    expect(component.store.selectedLoan()).toBeNull();
  });

  it('should update summary in store', () => {
    component.store.setSummary({ total: 10, pending: 5, approved: 3, rejected: 2 });
    expect(component.store.summary().total).toBe(10);
    expect(component.store.summary().pending).toBe(5);
    expect(component.store.summary().approved).toBe(3);
    expect(component.store.summary().rejected).toBe(2);
  });

  it('should store paginated loans correctly', () => {
    const loans = [
      { id: '1', userId: 'u1', amount: 1000, termMonths: 12, status: 'PENDING' as const, createdAt: '', updatedAt: '' },
      { id: '2', userId: 'u2', amount: 2000, termMonths: 24, status: 'APPROVED' as const, createdAt: '', updatedAt: '' },
    ];
    component.store.setLoans(loans, 0, 10, 2, 1, true, true);
    expect(component.store.loans().length).toBe(2);
    expect(component.store.totalElements()).toBe(2);
    expect(component.store.page()).toBe(0);
  });

  it('should update search term on search', () => {
    component.onSearch('abc');
    const req = httpTesting.expectOne(r => r.url.includes('/admin/loans') && !r.url.includes('/summary'));
    req.flush(mockPagedResponse);
    expect(component.searchTerm()).toBe('abc');
    expect(component.statusFilter()).toBe('ALL');
  });

  it('should set filter and reload loans', () => {
    component.setFilter('APPROVED');
    const req = httpTesting.expectOne(r => r.url.includes('/admin/loans') && !r.url.includes('/summary'));
    expect(req.request.params.get('status')).toBe('APPROVED');
    req.flush(mockPagedResponse);
    expect(component.statusFilter()).toBe('APPROVED');
    expect(component.searchTerm()).toBe('');
  });

  it('should go to page', () => {
    component.goToPage(2);
    const req = httpTesting.expectOne(r => r.url.includes('/admin/loans') && !r.url.includes('/summary'));
    expect(req.request.params.get('page')).toBe('2');
    req.flush(mockPagedResponse);
  });

  it('should handle loadLoans error', fakeAsync(() => {
    component.loadLoans();
    for (let i = 0; i < 3; i++) {
      const req = httpTesting.expectOne(r => r.url.includes('/admin/loans') && !r.url.includes('/summary'));
      req.flush({ detail: 'Server error' }, { status: 500, statusText: 'Error' });
      if (i < 2) tick(1000);
    }
    tick();
    expect(component.store.error()).toBe('Server error');
  }));

  it('should handle setFilter error', fakeAsync(() => {
    component.setFilter('PENDING');
    for (let i = 0; i < 3; i++) {
      const req = httpTesting.expectOne(r => r.url.includes('/admin/loans') && !r.url.includes('/summary'));
      req.flush({}, { status: 500, statusText: 'Error' });
      if (i < 2) tick(1000);
    }
    tick();
    expect(component.store.error()).toBeTruthy();
  }));

  it('should handle summary load failure silently', fakeAsync(() => {
    component.loadSummary();
    for (let i = 0; i < 3; i++) {
      const req = httpTesting.expectOne(r => r.url.includes('/summary'));
      req.flush({}, { status: 500, statusText: 'Error' });
      if (i < 2) tick(1000);
    }
    tick();
    expect(component.store.summary().total).toBe(0);
  }));

  it('should approve loan', () => {
    const loan = { id: 'loan-1', userId: 'u1', amount: 1000, termMonths: 12, status: 'PENDING' as const, createdAt: '', updatedAt: '' };
    component.approveLoan(loan);
    const req = httpTesting.expectOne(r => r.url.includes('/approve'));
    req.flush({ ...loan, status: 'APPROVED' });
    const summaryReq = httpTesting.expectOne(r => r.url.includes('/summary'));
    summaryReq.flush({ total: 1, pending: 0, approved: 1, rejected: 0 });
  });

  it('should not approve when action in progress', () => {
    component.store.setActionLoading('other-loan');
    const loan = { id: 'loan-1', userId: 'u1', amount: 1000, termMonths: 12, status: 'PENDING' as const, createdAt: '', updatedAt: '' };
    component.approveLoan(loan);
    httpTesting.expectNone(r => r.url.includes('/approve'));
  });

  it('should handle approve error with 409 and reload data', () => {
    const loan = { id: 'loan-1', userId: 'u1', amount: 1000, termMonths: 12, status: 'PENDING' as const, createdAt: '', updatedAt: '' };
    component.approveLoan(loan);
    const req = httpTesting.expectOne(r => r.url.includes('/approve'));
    req.flush({ detail: 'Conflict' }, { status: 409, statusText: 'Conflict' });
    const loansReq = httpTesting.expectOne(r => r.url.includes('/admin/loans') && !r.url.includes('/summary') && !r.url.includes('/approve'));
    loansReq.flush(mockPagedResponse);
    const summaryReq = httpTesting.expectOne(r => r.url.includes('/summary'));
    summaryReq.flush({ total: 1, pending: 1, approved: 0, rejected: 0 });
    expect(component.store.loans().length).toBe(1);
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

  it('should clear messages on destroy', () => {
    component.store.setSuccess('Done');
    component.ngOnDestroy();
    expect(component.store.successMessage()).toBeNull();
  });

  it('should have 4 skeleton items', () => {
    expect(component.skeletonItems.length).toBe(4);
  });
});
