import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RejectDialogComponent, RejectDialogData } from './reject-dialog.component';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';

describe('RejectDialogComponent', () => {
  let component: RejectDialogComponent;
  let fixture: ComponentFixture<RejectDialogComponent>;
  let dialogRef: jasmine.SpyObj<MatDialogRef<RejectDialogComponent>>;

  const mockData: RejectDialogData = { loanId: 'loan-1', amount: 5000000 };

  beforeEach(async () => {
    dialogRef = jasmine.createSpyObj('MatDialogRef', ['close']);

    await TestBed.configureTestingModule({
      imports: [RejectDialogComponent],
      providers: [
        { provide: MatDialogRef, useValue: dialogRef },
        { provide: MAT_DIALOG_DATA, useValue: mockData },
        provideAnimationsAsync(),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(RejectDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have empty reason by default', () => {
    expect(component.reason).toBe('');
  });

  it('should have showError false by default', () => {
    expect(component.showError).toBe(false);
  });

  it('should receive dialog data', () => {
    expect(component.data.loanId).toBe('loan-1');
    expect(component.data.amount).toBe(5000000);
  });

  it('should close dialog on cancel', () => {
    component.onCancel();
    expect(dialogRef.close).toHaveBeenCalledWith();
  });

  it('should show error when confirming with empty reason', () => {
    component.reason = '';
    component.onConfirm();
    expect(component.showError).toBeTrue();
    expect(dialogRef.close).not.toHaveBeenCalledWith(jasmine.any(String));
  });

  it('should show error when confirming with whitespace-only reason', () => {
    component.reason = '   ';
    component.onConfirm();
    expect(component.showError).toBeTrue();
  });

  it('should close dialog with trimmed reason on valid confirm', () => {
    component.reason = '  Does not meet criteria  ';
    component.onConfirm();
    expect(dialogRef.close).toHaveBeenCalledWith('Does not meet criteria');
  });
});
