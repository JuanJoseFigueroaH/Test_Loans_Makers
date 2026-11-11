import { TestBed } from '@angular/core/testing';
import { AdminLoanStore } from './admin-loan.store';
import { Loan } from '../models/loan.model';

describe('AdminLoanStore', () => {
  let store: InstanceType<typeof AdminLoanStore>;

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
    TestBed.configureTestingModule({ providers: [AdminLoanStore] });
    store = TestBed.inject(AdminLoanStore);
  });

  describe('initial state', () => {
    it('should have empty loans', () => {
      expect(store.loans()).toEqual([]);
    });

    it('should have no selected loan', () => {
      expect(store.selectedLoan()).toBeNull();
    });

    it('should have zero summary', () => {
      expect(store.summary()).toEqual({ total: 0, pending: 0, approved: 0, rejected: 0 });
    });

    it('should not be loading', () => {
      expect(store.loading()).toBeFalse();
    });

    it('should have no action in progress', () => {
      expect(store.isActionInProgress()).toBeFalse();
      expect(store.actionLoading()).toBeNull();
    });
  });

  describe('setLoading', () => {
    it('should set loading and clear messages', () => {
      store.setError('prev');
      store.setLoading();

      expect(store.loading()).toBeTrue();
      expect(store.error()).toBeNull();
      expect(store.successMessage()).toBeNull();
    });
  });

  describe('setLoans', () => {
    it('should set loans with pagination', () => {
      const loans = [mockLoan(), mockLoan({ id: '2' })];
      store.setLoans(loans, 0, 10, 2, 1, true, true);

      expect(store.loans().length).toBe(2);
      expect(store.totalElements()).toBe(2);
      expect(store.loading()).toBeFalse();
    });
  });

  describe('setSummary', () => {
    it('should update summary', () => {
      store.setSummary({ total: 10, pending: 5, approved: 3, rejected: 2 });

      expect(store.summary().total).toBe(10);
      expect(store.summary().pending).toBe(5);
      expect(store.summary().approved).toBe(3);
      expect(store.summary().rejected).toBe(2);
    });
  });

  describe('setSelectedLoan', () => {
    it('should set selected loan', () => {
      const loan = mockLoan();
      store.setSelectedLoan(loan);

      expect(store.selectedLoan()).toEqual(loan);
      expect(store.detailLoading()).toBeFalse();
    });
  });

  describe('setActionLoading', () => {
    it('should set action loading for specific loan', () => {
      store.setActionLoading('loan-1');

      expect(store.actionLoading()).toBe('loan-1');
      expect(store.isActionInProgress()).toBeTrue();
    });
  });

  describe('updateLoan', () => {
    it('should update loan in list', () => {
      const loan = mockLoan({ id: 'x', status: 'PENDING' });
      store.setLoans([loan], 0, 10, 1, 1, true, true);
      store.setActionLoading('x');

      const updated = mockLoan({ id: 'x', status: 'APPROVED' });
      store.updateLoan(updated);

      expect(store.loans()[0].status).toBe('APPROVED');
      expect(store.actionLoading()).toBeNull();
    });

    it('should update selected loan if it matches', () => {
      const loan = mockLoan({ id: 'x' });
      store.setLoans([loan], 0, 10, 1, 1, true, true);
      store.setSelectedLoan(loan);

      const updated = mockLoan({ id: 'x', status: 'REJECTED', rejectionReason: 'No meets criteria' });
      store.updateLoan(updated);

      expect(store.selectedLoan()?.status).toBe('REJECTED');
      expect(store.selectedLoan()?.rejectionReason).toBe('No meets criteria');
    });

    it('should not update selected loan if different id', () => {
      const loan1 = mockLoan({ id: 'x' });
      const loan2 = mockLoan({ id: 'y' });
      store.setLoans([loan1, loan2], 0, 10, 2, 1, true, true);
      store.setSelectedLoan(loan1);

      const updated = mockLoan({ id: 'y', status: 'APPROVED' });
      store.updateLoan(updated);

      expect(store.selectedLoan()?.id).toBe('x');
      expect(store.selectedLoan()?.status).toBe('PENDING');
    });
  });

  describe('setSuccess', () => {
    it('should set success message and clear action loading', () => {
      store.setActionLoading('loan-1');
      store.setSuccess('Approved');

      expect(store.successMessage()).toBe('Approved');
      expect(store.actionLoading()).toBeNull();
    });
  });

  describe('setError', () => {
    it('should set error and stop all loading', () => {
      store.setLoading();
      store.setActionLoading('loan-1');
      store.setError('Failed');

      expect(store.error()).toBe('Failed');
      expect(store.loading()).toBeFalse();
      expect(store.actionLoading()).toBeNull();
      expect(store.successMessage()).toBeNull();
    });
  });

  describe('clearMessages', () => {
    it('should clear error and success', () => {
      store.setError('err');
      store.setSuccess('ok');
      store.clearMessages();

      expect(store.error()).toBeNull();
      expect(store.successMessage()).toBeNull();
    });
  });

  describe('clearSelectedLoan', () => {
    it('should clear selected loan', () => {
      store.setSelectedLoan(mockLoan());
      store.clearSelectedLoan();

      expect(store.selectedLoan()).toBeNull();
    });
  });

  describe('computed properties', () => {
    it('should compute pagination flags', () => {
      store.setLoans([mockLoan()], 0, 10, 20, 2, true, false);
      expect(store.hasPreviousPage()).toBeFalse();
      expect(store.hasNextPage()).toBeTrue();
    });

    it('should compute totalLoans from array', () => {
      store.setLoans([mockLoan(), mockLoan({ id: '2' })], 0, 10, 2, 1, true, true);
      expect(store.totalLoans()).toBe(2);
    });

    it('should compute pendingCount', () => {
      store.setLoans(
        [mockLoan({ status: 'PENDING' }), mockLoan({ id: '2', status: 'APPROVED' })],
        0, 10, 2, 1, true, true
      );
      expect(store.pendingCount()).toBe(1);
    });

    it('should filter approved and rejected loans', () => {
      store.setLoans(
        [
          mockLoan({ id: '1', status: 'APPROVED' }),
          mockLoan({ id: '2', status: 'REJECTED' }),
          mockLoan({ id: '3', status: 'PENDING' }),
        ],
        0, 10, 3, 1, true, true
      );
      expect(store.approvedLoans().length).toBe(1);
      expect(store.rejectedLoans().length).toBe(1);
    });
  });

  describe('reset', () => {
    it('should reset to initial state', () => {
      store.setLoans([mockLoan()], 1, 10, 20, 2, false, true);
      store.setSelectedLoan(mockLoan());
      store.setSummary({ total: 10, pending: 5, approved: 3, rejected: 2 });

      store.reset();

      expect(store.loans()).toEqual([]);
      expect(store.selectedLoan()).toBeNull();
      expect(store.summary()).toEqual({ total: 0, pending: 0, approved: 0, rejected: 0 });
      expect(store.page()).toBe(0);
    });
  });
});
