import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CreateLoanComponent } from './create-loan.component';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';

describe('CreateLoanComponent', () => {
  let component: CreateLoanComponent;
  let fixture: ComponentFixture<CreateLoanComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CreateLoanComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        provideAnimationsAsync(),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(CreateLoanComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
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

  it('should have 6 term options', () => {
    expect(component.termOptions.length).toBe(6);
  });
});
