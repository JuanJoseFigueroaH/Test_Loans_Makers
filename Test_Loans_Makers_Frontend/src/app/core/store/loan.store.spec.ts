import { TestBed } from '@angular/core/testing';
import { LoanStore } from './loan.store';
import { Loan } from '../models/loan.model';

describe('LoanStore', () => {
  let store: InstanceType<typeof LoanStore>;

  const mockLoan = (overrides: Partial<Loan> = {}): Loan => ({
    id: '1',
    userId: 'u1',
    amount: 5000000,
    termMonths: 12,
    status: 'PENDING',
    createdAt: '2024-01-01T00:00:00Z',
    updatedAt: '2024-01-01T00:00:00Z',
    ...overrides,
  });

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [LoanStore] });
    store = TestBed.inject(LoanStore);
  });

  describe('initial state', () => {
    it('should have empty loans', () => {
      expect(store.loans()).toEqual([]);
    });

    it('should have no selected loan', () => {
      expect(store.selectedLoan()).toBeNull();
    });

    it('should not be loading', () => {
      expect(store.loading()).toBeFalse();
    });

    it('should have no error', () => {
      expect(store.error()).toBeNull();
    });

    it('should not have loans', () => {
      expect(store.hasLoans()).toBeFalse();
    });

    it('should have zero loan count', () => {
      expect(store.loanCount()).toBe(0);
    });
  });

  describe('setLoading', () => {
    it('should set loading and clear error and success', () => {
      store.setError('prev error');
      store.setLoading();

      expect(store.loading()).toBeTrue();
      expect(store.error()).toBeNull();
      expect(store.success()).toBeFalse();
    });
  });

  describe('setLoans', () => {
    it('should set loans with pagination metadata', () => {
      const loans = [mockLoan(), mockLoan({ id: '2', status: 'APPROVED' })];
      store.setLoans(loans, 0, 10, 2, 1, true, true);

      expect(store.loans().length).toBe(2);
      expect(store.page()).toBe(0);
      expect(store.size()).toBe(10);
      expect(store.totalElements()).toBe(2);
      expect(store.totalPages()).toBe(1);
      expect(store.first()).toBeTrue();
      expect(store.last()).toBeTrue();
      expect(store.loading()).toBeFalse();
    });
  });

  describe('computed properties', () => {
    it('should filter pending loans', () => {
      store.setLoans(
        [mockLoan({ id: '1', status: 'PENDING' }), mockLoan({ id: '2', status: 'APPROVED' }), mockLoan({ id: '3', status: 'REJECTED' })],
        0, 10, 3, 1, true, true
      );

      expect(store.pendingLoans().length).toBe(1);
      expect(store.approvedLoans().length).toBe(1);
      expect(store.rejectedLoans().length).toBe(1);
      expect(store.pendingCount()).toBe(1);
      expect(store.approvedCount()).toBe(1);
      expect(store.rejectedCount()).toBe(1);
    });

    it('should compute hasLoans correctly', () => {
      expect(store.hasLoans()).toBeFalse();
      store.setLoans([mockLoan()], 0, 10, 1, 1, true, true);
      expect(store.hasLoans()).toBeTrue();
    });

    it('should compute pagination flags', () => {
      store.setLoans([mockLoan()], 0, 10, 20, 2, true, false);
      expect(store.hasPreviousPage()).toBeFalse();
      expect(store.hasNextPage()).toBeTrue();

      store.setLoans([mockLoan()], 1, 10, 20, 2, false, true);
      expect(store.hasPreviousPage()).toBeTrue();
      expect(store.hasNextPage()).toBeFalse();
    });
  });

  describe('setSelectedLoan', () => {
    it('should set selected loan and clear errors', () => {
      const loan = mockLoan();
      store.setSelectedLoan(loan);

      expect(store.selectedLoan()).toEqual(loan);
      expect(store.detailLoading()).toBeFalse();
      expect(store.error()).toBeNull();
    });
  });

  describe('setDetailLoading', () => {
    it('should set detail loading', () => {
      store.setDetailLoading();
      expect(store.detailLoading()).toBeTrue();
      expect(store.error()).toBeNull();
    });
  });

  describe('setCreated', () => {
    it('should set created loan and success flag', () => {
      const loan = mockLoan();
      store.setCreated(loan);

      expect(store.createdLoan()).toEqual(loan);
      expect(store.success()).toBeTrue();
      expect(store.loading()).toBeFalse();
      expect(store.error()).toBeNull();
    });
  });

  describe('setError', () => {
    it('should set error and stop all loading states', () => {
      store.setLoading();
      store.setError('Something went wrong');

      expect(store.error()).toBe('Something went wrong');
      expect(store.loading()).toBeFalse();
      expect(store.detailLoading()).toBeFalse();
      expect(store.success()).toBeFalse();
    });
  });

  describe('clearState', () => {
    it('should clear transient state without affecting loans', () => {
      store.setLoans([mockLoan()], 0, 10, 1, 1, true, true);
      store.setSelectedLoan(mockLoan());
      store.setError('err');

      store.clearState();

      expect(store.createdLoan()).toBeNull();
      expect(store.selectedLoan()).toBeNull();
      expect(store.error()).toBeNull();
      expect(store.success()).toBeFalse();
      expect(store.loans().length).toBe(1);
    });
  });

  describe('reset', () => {
    it('should reset everything to initial state', () => {
      store.setLoans([mockLoan()], 1, 10, 20, 2, false, true);
      store.setSelectedLoan(mockLoan());
      store.setError('err');

      store.reset();

      expect(store.loans()).toEqual([]);
      expect(store.selectedLoan()).toBeNull();
      expect(store.loading()).toBeFalse();
      expect(store.error()).toBeNull();
      expect(store.page()).toBe(0);
      expect(store.totalElements()).toBe(0);
    });
  });
});
