import { TestBed, fakeAsync, tick } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { AuthService } from './auth.service';

describe('AuthService', () => {
  let service: AuthService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        AuthService,
      ],
    });

    service = TestBed.inject(AuthService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  describe('login', () => {
    it('should POST to /auth/login', () => {
      const mockResponse = {
        token: 'jwt-token',
        user: { id: '1', email: 'test@test.com', role: 'ROLE_USER', enabled: true, createdAt: '' },
      };

      service.login({ email: 'test@test.com', password: '123' }).subscribe((res) => {
        expect(res.token).toBe('jwt-token');
        expect(res.user.email).toBe('test@test.com');
      });

      const req = httpTesting.expectOne(r => r.url.includes('/auth/login'));
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual({ email: 'test@test.com', password: '123' });
      req.flush(mockResponse);
    });

    it('should propagate error on failed login', fakeAsync(() => {
      let errorStatus: number | undefined;
      service.login({ email: 'wrong@test.com', password: 'bad' }).subscribe({
        error: (err) => {
          errorStatus = err.status;
        },
      });

      for (let i = 0; i < 3; i++) {
        const req = httpTesting.expectOne(r => r.url.includes('/auth/login'));
        req.flush({ title: 'Invalid Credentials' }, { status: 401, statusText: 'Unauthorized' });
        if (i < 2) tick(1000);
      }
      tick();
      expect(errorStatus).toBe(401);
    }));
  });

  describe('me', () => {
    it('should GET /auth/me', () => {
      const mockUser = { id: '1', email: 'test@test.com', role: 'ROLE_USER', enabled: true, createdAt: '' };

      service.me().subscribe((user) => {
        expect(user.email).toBe('test@test.com');
        expect(user.role).toBe('ROLE_USER');
      });

      const req = httpTesting.expectOne(r => r.url.includes('/auth/me'));
      expect(req.request.method).toBe('GET');
      req.flush(mockUser);
    });
  });
});
