import { computed } from '@angular/core';
import { signalStore, withState, withComputed, withMethods, patchState } from '@ngrx/signals';
import { Loan, LoanSummary } from '../models/loan.model';

export interface AdminLoanState {
  loans: Loan[];
  selectedLoan: Loan | null;
  summary: LoanSummary;
  loading: boolean;
  detailLoading: boolean;
  actionLoading: string | null;
  error: string | null;
  successMessage: string | null;
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

const initialState: AdminLoanState = {
  loans: [],
  selectedLoan: null,
  summary: { total: 0, pending: 0, approved: 0, rejected: 0 },
  loading: false,
  detailLoading: false,
  actionLoading: null,
  error: null,
  successMessage: null,
  page: 0,
  size: 10,
  totalElements: 0,
  totalPages: 0,
  first: true,
  last: true,
};

export const AdminLoanStore = signalStore(
  { providedIn: 'root' },
  withState(initialState),
  withComputed((store) => ({
    isActionInProgress: computed(() => store.actionLoading() !== null),
    hasPreviousPage: computed(() => !store.first()),
    hasNextPage: computed(() => !store.last()),
    totalLoans: computed(() => store.loans().length),
    pendingCount: computed(() => store.loans().filter(l => l.status === 'PENDING').length),
    approvedLoans: computed(() => store.loans().filter(l => l.status === 'APPROVED')),
    rejectedLoans: computed(() => store.loans().filter(l => l.status === 'REJECTED')),
  })),
  withMethods((store) => ({
    setLoading(): void {
      patchState(store, { loading: true, error: null, successMessage: null });
    },
    setLoans(loans: Loan[], page: number, size: number, totalElements: number, totalPages: number, first: boolean, last: boolean): void {
      patchState(store, { loans, page, size, totalElements, totalPages, first, last, loading: false, error: null });
    },
    setSummary(summary: LoanSummary): void {
      patchState(store, { summary });
    },
    setSelectedLoan(loan: Loan): void {
      patchState(store, { selectedLoan: loan, detailLoading: false, error: null });
    },
    setDetailLoading(): void {
      patchState(store, { detailLoading: true, error: null });
    },
    setActionLoading(loanId: string): void {
      patchState(store, { actionLoading: loanId, error: null, successMessage: null });
    },
    updateLoan(updated: Loan): void {
      const loans = store.loans().map(l => l.id === updated.id ? updated : l);
      patchState(store, { loans, actionLoading: null, error: null });
      if (store.selectedLoan()?.id === updated.id) {
        patchState(store, { selectedLoan: updated });
      }
    },
    setSuccess(message: string): void {
      patchState(store, { successMessage: message, actionLoading: null });
    },
    setError(error: string): void {
      patchState(store, { loading: false, detailLoading: false, actionLoading: null, error, successMessage: null });
    },
    clearMessages(): void {
      patchState(store, { error: null, successMessage: null });
    },
    clearSelectedLoan(): void {
      patchState(store, { selectedLoan: null });
    },
    reset(): void {
      patchState(store, { ...initialState });
    },
  }))
);
