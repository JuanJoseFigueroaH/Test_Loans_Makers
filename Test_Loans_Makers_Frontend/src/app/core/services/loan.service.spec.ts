import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { LoanService } from './loan.service';

describe('LoanService', () => {
  let service: LoanService;
  let httpTesting: HttpTestingController;

  const mockLoan = {
    id: 'loan-1',
    userId: 'user-1',
    amount: 5000000,
    termMonths: 12,
    status: 'PENDING',
    createdAt: '2024-01-01T00:00:00Z',
    updatedAt: '2024-01-01T00:00:00Z',
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        LoanService,
      ],
    });

    service = TestBed.inject(LoanService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  describe('createLoan', () => {
    it('should POST to /loans', () => {
      service.createLoan({ amount: 5000000, termMonths: 12 }).subscribe((loan) => {
        expect(loan.id).toBe('loan-1');
        expect(loan.status).toBe('PENDING');
      });

      const req = httpTesting.expectOne(r => r.url.includes('/loans') && r.method === 'POST');
      expect(req.request.body).toEqual({ amount: 5000000, termMonths: 12 });
      req.flush(mockLoan);
    });
  });

  describe('getMyLoans', () => {
    it('should GET /loans with pagination params', () => {
      service.getMyLoans(0, 10).subscribe((res) => {
        expect(res.content.length).toBe(1);
      });

      const req = httpTesting.expectOne(r => r.url.includes('/loans') && r.method === 'GET' && !r.url.includes('/admin'));
      expect(req.request.params.get('page')).toBe('0');
      expect(req.request.params.get('size')).toBe('10');
      req.flush({ content: [mockLoan], page: 0, size: 10, totalElements: 1, totalPages: 1, first: true, last: true });
    });

    it('should include status filter when provided', () => {
      service.getMyLoans(0, 10, 'PENDING').subscribe();

      const req = httpTesting.expectOne(r => r.url.includes('/loans') && !r.url.includes('/admin'));
      expect(req.request.params.get('status')).toBe('PENDING');
      req.flush({ content: [], page: 0, size: 10, totalElements: 0, totalPages: 0, first: true, last: true });
    });

    it('should not include status param when undefined', () => {
      service.getMyLoans(0, 10).subscribe();

      const req = httpTesting.expectOne(r => r.url.includes('/loans') && !r.url.includes('/admin'));
      expect(req.request.params.has('status')).toBeFalse();
      req.flush({ content: [], page: 0, size: 10, totalElements: 0, totalPages: 0, first: true, last: true });
    });
  });

  describe('getLoanById', () => {
    it('should GET /loans/:id', () => {
      service.getLoanById('loan-1').subscribe((loan) => {
        expect(loan.id).toBe('loan-1');
      });

      const req = httpTesting.expectOne(r => r.url.includes('/loans/loan-1') && !r.url.includes('/admin'));
      expect(req.request.method).toBe('GET');
      req.flush(mockLoan);
    });
  });

  describe('getAdminLoans', () => {
    it('should GET /admin/loans with pagination', () => {
      service.getAdminLoans(0, 10).subscribe((res) => {
        expect(res.content.length).toBe(1);
      });

      const req = httpTesting.expectOne(r => r.url.includes('/admin/loans') && r.method === 'GET' && !r.url.includes('/summary'));
      expect(req.request.params.get('page')).toBe('0');
      req.flush({ content: [mockLoan], page: 0, size: 10, totalElements: 1, totalPages: 1, first: true, last: true });
    });

    it('should include status and search params', () => {
      service.getAdminLoans(0, 10, 'APPROVED', 'loan-id').subscribe();

      const req = httpTesting.expectOne(r => r.url.includes('/admin/loans') && r.method === 'GET');
      expect(req.request.params.get('status')).toBe('APPROVED');
      expect(req.request.params.get('search')).toBe('loan-id');
      req.flush({ content: [], page: 0, size: 10, totalElements: 0, totalPages: 0, first: true, last: true });
    });
  });

  describe('getAdminLoanById', () => {
    it('should GET /admin/loans/:id', () => {
      service.getAdminLoanById('loan-1').subscribe((loan) => {
        expect(loan.id).toBe('loan-1');
      });

      const req = httpTesting.expectOne(r => r.url.includes('/admin/loans/loan-1'));
      expect(req.request.method).toBe('GET');
      req.flush(mockLoan);
    });
  });

  describe('getAdminSummary', () => {
    it('should GET /admin/loans/summary', () => {
      service.getAdminSummary().subscribe((summary) => {
        expect(summary.total).toBe(10);
        expect(summary.pending).toBe(5);
      });

      const req = httpTesting.expectOne(r => r.url.includes('/admin/loans/summary'));
      expect(req.request.method).toBe('GET');
      req.flush({ total: 10, pending: 5, approved: 3, rejected: 2 });
    });
  });

  describe('approveLoan', () => {
    it('should PATCH /admin/loans/:id/approve', () => {
      service.approveLoan('loan-1').subscribe((loan) => {
        expect(loan.status).toBe('APPROVED');
      });

      const req = httpTesting.expectOne(r => r.url.includes('/admin/loans/loan-1/approve'));
      expect(req.request.method).toBe('PATCH');
      expect(req.request.body).toEqual({});
      req.flush({ ...mockLoan, status: 'APPROVED' });
    });
  });

  describe('rejectLoan', () => {
    it('should PATCH /admin/loans/:id/reject with reason', () => {
      service.rejectLoan('loan-1', 'Does not meet criteria').subscribe((loan) => {
        expect(loan.status).toBe('REJECTED');
        expect(loan.rejectionReason).toBe('Does not meet criteria');
      });

      const req = httpTesting.expectOne(r => r.url.includes('/admin/loans/loan-1/reject'));
      expect(req.request.method).toBe('PATCH');
      expect(req.request.body).toEqual({ reason: 'Does not meet criteria' });
      req.flush({ ...mockLoan, status: 'REJECTED', rejectionReason: 'Does not meet criteria' });
    });
  });

  describe('error handling', () => {
    it('should propagate 400 error on invalid loan creation', () => {
      service.createLoan({ amount: -1, termMonths: 0 }).subscribe({
        error: (err) => {
          expect(err.status).toBe(400);
        },
      });

      const req = httpTesting.expectOne(r => r.url.includes('/loans') && r.method === 'POST');
      req.flush({ title: 'Invalid Loan Request' }, { status: 400, statusText: 'Bad Request' });
    });

    it('should propagate 409 on concurrent modification', () => {
      service.approveLoan('loan-1').subscribe({
        error: (err) => {
          expect(err.status).toBe(409);
        },
      });

      const req = httpTesting.expectOne(r => r.url.includes('/approve'));
      req.flush({ title: 'Concurrency Conflict' }, { status: 409, statusText: 'Conflict' });
    });

    it('should propagate 404 on non-existent loan', () => {
      service.getLoanById('nonexistent').subscribe({
        error: (err) => {
          expect(err.status).toBe(404);
        },
      });

      const req = httpTesting.expectOne(r => r.url.includes('/loans/nonexistent'));
      req.flush({ title: 'Loan Not Found' }, { status: 404, statusText: 'Not Found' });
    });
  });
});
