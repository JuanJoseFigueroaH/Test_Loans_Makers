import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors, HttpErrorResponse } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Router } from '@angular/router';
import { authInterceptor } from './auth.interceptor';
import { AuthStore } from '../store/auth.store';

describe('AuthInterceptor', () => {
  let httpClient: HttpClient;
  let httpTesting: HttpTestingController;
  let authStore: InstanceType<typeof AuthStore>;
  let router: Router;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        AuthStore,
        {
          provide: Router,
          useValue: { navigate: jasmine.createSpy('navigate') },
        },
      ],
    });

    httpClient = TestBed.inject(HttpClient);
    httpTesting = TestBed.inject(HttpTestingController);
    authStore = TestBed.inject(AuthStore);
    router = TestBed.inject(Router);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('should not add Authorization header when no token', () => {
    httpClient.get('/api/test').subscribe();

    const req = httpTesting.expectOne('/api/test');
    expect(req.request.headers.has('Authorization')).toBeFalse();
    req.flush({});
  });

  it('should add Authorization header when token exists', () => {
    authStore.setAuthenticated(
      { id: '1', email: 'test@test.com', role: 'ROLE_USER', enabled: true, createdAt: '' },
      'my-jwt-token'
    );

    httpClient.get('/api/test').subscribe();

    const req = httpTesting.expectOne('/api/test');
    expect(req.request.headers.get('Authorization')).toBe('Bearer my-jwt-token');
    req.flush({});
  });

  it('should logout and redirect on 401 response', () => {
    authStore.setAuthenticated(
      { id: '1', email: 'test@test.com', role: 'ROLE_USER', enabled: true, createdAt: '' },
      'expired-token'
    );

    httpClient.get('/api/test').subscribe({
      error: (error: HttpErrorResponse) => {
        expect(error.status).toBe(401);
      },
    });

    const req = httpTesting.expectOne('/api/test');
    req.flush({ message: 'Unauthorized' }, { status: 401, statusText: 'Unauthorized' });

    expect(authStore.isAuthenticated()).toBeFalse();
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });

  it('should not logout on non-401 errors', () => {
    authStore.setAuthenticated(
      { id: '1', email: 'test@test.com', role: 'ROLE_USER', enabled: true, createdAt: '' },
      'valid-token'
    );

    httpClient.get('/api/test').subscribe({
      error: (error: HttpErrorResponse) => {
        expect(error.status).toBe(500);
      },
    });

    const req = httpTesting.expectOne('/api/test');
    req.flush({ message: 'Server Error' }, { status: 500, statusText: 'Internal Server Error' });

    expect(authStore.isAuthenticated()).toBeTrue();
    expect(router.navigate).not.toHaveBeenCalled();
  });

  it('should propagate the error after handling 401', () => {
    let errorReceived = false;

    httpClient.get('/api/test').subscribe({
      error: () => {
        errorReceived = true;
      },
    });

    const req = httpTesting.expectOne('/api/test');
    req.flush({}, { status: 401, statusText: 'Unauthorized' });

    expect(errorReceived).toBeTrue();
  });

  it('should not modify non-401 error responses', () => {
    httpClient.get('/api/test').subscribe({
      error: (error: HttpErrorResponse) => {
        expect(error.status).toBe(403);
      },
    });

    const req = httpTesting.expectOne('/api/test');
    req.flush({ message: 'Forbidden' }, { status: 403, statusText: 'Forbidden' });

    expect(authStore.token()).toBeNull();
  });
});
