import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { LoginComponent } from './login.component';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter, Router } from '@angular/router';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { AuthStore } from '../../../core/store/auth.store';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let httpTesting: HttpTestingController;
  let router: Router;
  let authStore: InstanceType<typeof AuthStore>;

  beforeEach(async () => {
    sessionStorage.clear();
    await TestBed.configureTestingModule({
      imports: [LoginComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([
          { path: 'dashboard', component: LoginComponent },
          { path: 'admin', component: LoginComponent },
        ]),
        provideAnimationsAsync(),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    httpTesting = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    authStore = TestBed.inject(AuthStore);
    fixture.detectChanges();
  });

  afterEach(() => {
    httpTesting.verify();
    sessionStorage.clear();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have an invalid form when empty', () => {
    expect(component.loginForm.valid).toBeFalsy();
  });

  it('should require email', () => {
    const emailControl = component.loginForm.get('email');
    expect(emailControl?.errors?.['required']).toBeTruthy();
  });

  it('should require password', () => {
    const passwordControl = component.loginForm.get('password');
    expect(passwordControl?.errors?.['required']).toBeTruthy();
  });

  it('should validate email format', () => {
    const emailControl = component.loginForm.get('email');
    emailControl?.setValue('invalid-email');
    expect(emailControl?.errors?.['email']).toBeTruthy();

    emailControl?.setValue('valid@test.com');
    expect(emailControl?.errors).toBeNull();
  });

  it('should be valid when email and password are provided', () => {
    component.loginForm.patchValue({
      email: 'test@test.com',
      password: '123',
    });
    expect(component.loginForm.valid).toBeTruthy();
  });

  it('should not submit when form is invalid', () => {
    component.onSubmit();
    expect(component.loginForm.get('email')?.touched).toBeTruthy();
    expect(component.loginForm.get('password')?.touched).toBeTruthy();
  });

  it('should call login service on valid submit', () => {
    component.loginForm.patchValue({ email: 'test@test.com', password: '123' });
    component.onSubmit();
    const req = httpTesting.expectOne(r => r.url.includes('/auth/login'));
    expect(req.request.method).toBe('POST');
    req.flush({
      token: 'jwt',
      user: { id: '1', email: 'test@test.com', role: 'ROLE_USER', enabled: true, createdAt: '' },
    });
  });

  it('should navigate to /admin for admin user', fakeAsync(() => {
    spyOn(router, 'navigate');
    component.loginForm.patchValue({ email: 'admin@test.com', password: '123' });
    component.onSubmit();
    const req = httpTesting.expectOne(r => r.url.includes('/auth/login'));
    req.flush({
      token: 'jwt',
      user: { id: '2', email: 'admin@test.com', role: 'ROLE_ADMIN', enabled: true, createdAt: '' },
    });
    tick();
    expect(router.navigate).toHaveBeenCalledWith(['/admin']);
  }));

  it('should navigate to /dashboard for regular user', fakeAsync(() => {
    spyOn(router, 'navigate');
    component.loginForm.patchValue({ email: 'user@test.com', password: '123' });
    component.onSubmit();
    const req = httpTesting.expectOne(r => r.url.includes('/auth/login'));
    req.flush({
      token: 'jwt',
      user: { id: '1', email: 'user@test.com', role: 'ROLE_USER', enabled: true, createdAt: '' },
    });
    tick();
    expect(router.navigate).toHaveBeenCalledWith(['/dashboard']);
  }));

  it('should set error on failed login', fakeAsync(() => {
    component.loginForm.patchValue({ email: 'test@test.com', password: 'bad' });
    component.onSubmit();
    for (let i = 0; i < 3; i++) {
      const req = httpTesting.expectOne(r => r.url.includes('/auth/login'));
      req.flush({ detail: 'Invalid credentials' }, { status: 401, statusText: 'Unauthorized' });
      if (i < 2) tick(1000);
    }
    tick();
    expect(authStore.error()).toBe('Invalid credentials');
  }));

  it('should default hidePassword to true', () => {
    expect(component.hidePassword).toBeTrue();
  });

  describe('getFieldError', () => {
    it('should return required message', () => {
      component.loginForm.get('email')?.markAsTouched();
      expect(component.getFieldError('email')).toBe('Este campo es obligatorio');
    });

    it('should return email format message', () => {
      component.loginForm.get('email')?.setValue('bad');
      component.loginForm.get('email')?.markAsTouched();
      expect(component.getFieldError('email')).toBe('Ingrese un email válido');
    });

    it('should return empty for valid field', () => {
      component.loginForm.get('email')?.setValue('good@test.com');
      expect(component.getFieldError('email')).toBe('');
    });
  });
});
