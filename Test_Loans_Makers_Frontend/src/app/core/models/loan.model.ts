export interface Loan {
  id: string;
  userId: string;
  amount: number;
  termMonths: number;
  status: LoanStatus;
  createdAt: string;
  updatedAt: string;
  reviewedAt?: string;
  reviewedBy?: string;
  rejectionReason?: string;
}

export interface CreateLoanRequest {
  amount: number;
  termMonths: number;
}

export interface PagedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface LoanSummary {
  total: number;
  pending: number;
  approved: number;
  rejected: number;
}

export type LoanStatus = 'PENDING' | 'APPROVED' | 'REJECTED';
