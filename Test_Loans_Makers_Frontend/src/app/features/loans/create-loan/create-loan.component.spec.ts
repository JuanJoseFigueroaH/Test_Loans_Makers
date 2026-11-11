import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { CreateLoanComponent } from './create-loan.component';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter, Router } from '@angular/router';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { LoanStore } from '../../../core/store/loan.store';

describe('CreateLoanComponent', () => {
  let component: CreateLoanComponent;
  let fixture: ComponentFixture<CreateLoanComponent>;
  let httpTesting: HttpTestingController;
  let router: Router;
  let loanStore: InstanceType<typeof LoanStore>;

  beforeEach(async () => {
    sessionStorage.clear();
    await TestBed.configureTestingModule({
      imports: [CreateLoanComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([{ path: 'dashboard', component: CreateLoanComponent }]),
        provideAnimationsAsync(),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(CreateLoanComponent);
    component = fixture.componentInstance;
    httpTesting = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    loanStore = TestBed.inject(LoanStore);
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
    expect(component.loanForm.valid).toBeFalsy();
  });

  it('should require amount', () => {
    const amountControl = component.loanForm.get('amount');
    expect(amountControl?.errors?.['required']).toBeTruthy();
  });

  it('should require termMonths', () => {
    const termControl = component.loanForm.get('termMonths');
    expect(termControl?.errors?.['required']).toBeTruthy();
  });

  it('should reject zero amount', () => {
    const amountControl = component.loanForm.get('amount');
    amountControl?.setValue(0);
    expect(amountControl?.errors?.['min']).toBeTruthy();
  });

  it('should reject negative amount', () => {
    const amountControl = component.loanForm.get('amount');
    amountControl?.setValue(-5000);
    expect(amountControl?.errors?.['min']).toBeTruthy();
  });

  it('should accept valid amount', () => {
    const amountControl = component.loanForm.get('amount');
    amountControl?.setValue(15000000);
    expect(amountControl?.errors).toBeNull();
  });

  it('should reject zero term months', () => {
    const termControl = component.loanForm.get('termMonths');
    termControl?.setValue(0);
    expect(termControl?.errors?.['min']).toBeTruthy();
  });

  it('should accept valid term months', () => {
    const termControl = component.loanForm.get('termMonths');
    termControl?.setValue(36);
    expect(termControl?.errors).toBeNull();
  });

  it('should be valid when amount and termMonths are provided', () => {
    component.loanForm.patchValue({
      amount: 15000000,
      termMonths: 36,
    });
    expect(component.loanForm.valid).toBeTruthy();
  });

  it('should not submit when form is invalid', () => {
    component.onSubmit();
    expect(component.loanForm.get('amount')?.touched).toBeTruthy();
    expect(component.loanForm.get('termMonths')?.touched).toBeTruthy();
  });

  it('should return correct error message for required fields', () => {
    component.loanForm.get('amount')?.markAsTouched();
    expect(component.getFieldError('amount')).toBe('Este campo es obligatorio');
  });

  it('should return correct error message for min amount', () => {
    component.loanForm.get('amount')?.setValue(0);
    component.loanForm.get('amount')?.markAsTouched();
    expect(component.getFieldError('amount')).toBe('El monto debe ser mayor a cero');
  });

  it('should return correct error message for min termMonths', () => {
    component.loanForm.get('termMonths')?.setValue(0);
    component.loanForm.get('termMonths')?.markAsTouched();
    expect(component.getFieldError('termMonths')).toBe('El plazo debe ser mayor a cero');
  });

  it('should return empty for valid field', () => {
    component.loanForm.get('amount')?.setValue(5000);
    expect(component.getFieldError('amount')).toBe('');
  });

  it('should have 6 term options', () => {
    expect(component.termOptions.length).toBe(6);
  });

  it('should call loan service on valid submit', () => {
    component.loanForm.patchValue({ amount: 5000000, termMonths: 12 });
    component.onSubmit();
    const req = httpTesting.expectOne(r => r.url.includes('/loans') && r.method === 'POST');
    req.flush({
      id: 'loan-1', userId: 'u1', amount: 5000000, termMonths: 12,
      status: 'PENDING', createdAt: '', updatedAt: '',
    });
    expect(loanStore.success()).toBeTrue();
    expect(loanStore.createdLoan()).toBeTruthy();
  });

  it('should set error on failed creation', () => {
    component.loanForm.patchValue({ amount: 5000000, termMonths: 12 });
    component.onSubmit();
    const req = httpTesting.expectOne(r => r.url.includes('/loans') && r.method === 'POST');
    req.flush({ detail: 'Validation failed' }, { status: 400, statusText: 'Bad Request' });
    expect(loanStore.error()).toBe('Validation failed');
  });

  it('should reset form on newRequest', () => {
    component.loanForm.patchValue({ amount: 5000, termMonths: 12 });
    component.newRequest();
    expect(component.loanForm.get('amount')?.value).toBeNull();
    expect(component.loanForm.get('termMonths')?.value).toBeNull();
  });

  it('should navigate to dashboard on goToDashboard', () => {
    spyOn(router, 'navigate');
    component.goToDashboard();
    expect(router.navigate).toHaveBeenCalledWith(['/dashboard']);
  });

  it('should clear state on destroy', () => {
    loanStore.setError('some error');
    component.ngOnDestroy();
    expect(loanStore.error()).toBeNull();
  });
});
