import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, retry, timeout } from 'rxjs';
import { Loan, CreateLoanRequest, PagedResponse, LoanStatus, LoanSummary } from '../models/loan.model';
import { environment } from '@env/environment';

@Injectable({ providedIn: 'root' })
export class LoanService {
  private readonly apiUrl = `${environment.apiUrl}/loans`;
  private readonly adminUrl = `${environment.apiUrl}/admin/loans`;

  constructor(private readonly http: HttpClient) {}

  createLoan(request: CreateLoanRequest): Observable<Loan> {
    return this.http.post<Loan>(this.apiUrl, request).pipe(
      timeout(15000),
    );
  }

  getMyLoans(page = 0, size = 10, status?: LoanStatus): Observable<PagedResponse<Loan>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    if (status) {
      params = params.set('status', status);
    }
    return this.http.get<PagedResponse<Loan>>(this.apiUrl, { params }).pipe(
      timeout(15000),
      retry({ count: 2, delay: 1000 }),
    );
  }

  getLoanById(id: string): Observable<Loan> {
    return this.http.get<Loan>(`${this.apiUrl}/${id}`).pipe(
      timeout(15000),
      retry({ count: 2, delay: 1000 }),
    );
  }

  getAdminLoans(page = 0, size = 10, status?: LoanStatus, search?: string): Observable<PagedResponse<Loan>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    if (status) {
      params = params.set('status', status);
    }
    if (search) {
      params = params.set('search', search);
    }
    return this.http.get<PagedResponse<Loan>>(this.adminUrl, { params }).pipe(
      timeout(15000),
      retry({ count: 2, delay: 1000 }),
    );
  }

  getAdminLoanById(id: string): Observable<Loan> {
    return this.http.get<Loan>(`${this.adminUrl}/${id}`).pipe(
      timeout(15000),
      retry({ count: 2, delay: 1000 }),
    );
  }

  getAdminSummary(): Observable<LoanSummary> {
    return this.http.get<LoanSummary>(`${this.adminUrl}/summary`).pipe(
      timeout(15000),
      retry({ count: 2, delay: 1000 }),
    );
  }

  approveLoan(loanId: string): Observable<Loan> {
    return this.http.patch<Loan>(`${this.adminUrl}/${loanId}/approve`, {}).pipe(
      timeout(15000),
    );
  }

  rejectLoan(loanId: string, reason: string): Observable<Loan> {
    return this.http.patch<Loan>(`${this.adminUrl}/${loanId}/reject`, { reason }).pipe(
      timeout(15000),
    );
  }
}
