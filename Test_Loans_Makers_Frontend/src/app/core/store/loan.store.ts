import { computed } from '@angular/core';
import { signalStore, withState, withComputed, withMethods, patchState } from '@ngrx/signals';
import { Loan } from '../models/loan.model';

export interface LoanState {
  loans: Loan[];
  selectedLoan: Loan | null;
  createdLoan: Loan | null;
  loading: boolean;
  detailLoading: boolean;
  error: string | null;
  success: boolean;
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

const initialState: LoanState = {
  loans: [],
  selectedLoan: null,
  createdLoan: null,
  loading: false,
  detailLoading: false,
  error: null,
  success: false,
  page: 0,
  size: 10,
  totalElements: 0,
  totalPages: 0,
  first: true,
  last: true,
};

export const LoanStore = signalStore(
  { providedIn: 'root' },
  withState(initialState),
  withComputed((store) => ({
    hasLoans: computed(() => store.loans().length > 0),
    loanCount: computed(() => store.loans().length),
    pendingLoans: computed(() => store.loans().filter(l => l.status === 'PENDING')),
    approvedLoans: computed(() => store.loans().filter(l => l.status === 'APPROVED')),
    rejectedLoans: computed(() => store.loans().filter(l => l.status === 'REJECTED')),
    pendingCount: computed(() => store.loans().filter(l => l.status === 'PENDING').length),
    approvedCount: computed(() => store.loans().filter(l => l.status === 'APPROVED').length),
    rejectedCount: computed(() => store.loans().filter(l => l.status === 'REJECTED').length),
    hasPreviousPage: computed(() => !store.first()),
    hasNextPage: computed(() => !store.last()),
  })),
  withMethods((store) => ({
    setLoading(): void {
      patchState(store, { loading: true, error: null, success: false });
    },
    setLoans(loans: Loan[], page: number, size: number, totalElements: number, totalPages: number, first: boolean, last: boolean): void {
      patchState(store, { loans, page, size, totalElements, totalPages, first, last, loading: false, error: null });
    },
    setSelectedLoan(loan: Loan): void {
      patchState(store, { selectedLoan: loan, detailLoading: false, error: null });
    },
    setDetailLoading(): void {
      patchState(store, { detailLoading: true, error: null });
    },
    setCreated(loan: Loan): void {
      patchState(store, {
        createdLoan: loan,
        loading: false,
        error: null,
        success: true,
      });
    },
    setError(error: string): void {
      patchState(store, { loading: false, detailLoading: false, error, success: false });
    },
    clearState(): void {
      patchState(store, { createdLoan: null, selectedLoan: null, error: null, success: false });
    },
    reset(): void {
      patchState(store, { ...initialState });
    },
  }))
);
