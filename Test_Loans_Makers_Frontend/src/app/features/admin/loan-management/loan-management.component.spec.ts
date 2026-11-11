import { ComponentFixture, TestBed } from '@angular/core/testing';
import { LoanManagementComponent } from './loan-management.component';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';

describe('LoanManagementComponent', () => {
  let component: LoanManagementComponent;
  let fixture: ComponentFixture<LoanManagementComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LoanManagementComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        provideAnimationsAsync(),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(LoanManagementComponent);
    component = fixture.componentInstance;
  });

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
    expect(component.searchTerm()).toBe('abc');
    expect(component.statusFilter()).toBe('ALL');
  });
});
