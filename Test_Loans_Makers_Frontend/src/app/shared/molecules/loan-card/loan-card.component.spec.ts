import { ComponentFixture, TestBed } from '@angular/core/testing';
import { LoanCardComponent } from './loan-card.component';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';

describe('LoanCardComponent', () => {
  let component: LoanCardComponent;
  let fixture: ComponentFixture<LoanCardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LoanCardComponent],
      providers: [provideAnimationsAsync()],
    }).compileComponents();

    fixture = TestBed.createComponent(LoanCardComponent);
    component = fixture.componentInstance;
    component.status = 'PENDING';
    component.amount = 5000000;
    component.termMonths = 12;
    component.date = '2024-01-15T00:00:00Z';
  });

  it('should create', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  describe('statusLabel', () => {
    it('should return Pendiente for PENDING', () => {
      component.status = 'PENDING';
      expect(component.statusLabel).toBe('Pendiente');
    });

    it('should return Aprobado for APPROVED', () => {
      component.status = 'APPROVED';
      expect(component.statusLabel).toBe('Aprobado');
    });

    it('should return Rechazado for REJECTED', () => {
      component.status = 'REJECTED';
      expect(component.statusLabel).toBe('Rechazado');
    });

    it('should return the status for unknown', () => {
      component.status = 'OTHER';
      expect(component.statusLabel).toBe('OTHER');
    });
  });

  it('should emit cardClick on click', () => {
    fixture.detectChanges();
    spyOn(component.cardClick, 'emit');
    const card = fixture.nativeElement.querySelector('mat-card');
    card.click();
    expect(component.cardClick.emit).toHaveBeenCalled();
  });

  it('should display rejection reason when present', () => {
    component.rejectionReason = 'Income too low';
    fixture.detectChanges();
    const el: HTMLElement = fixture.nativeElement;
    expect(el.textContent).toContain('Income too low');
  });

  it('should not display rejection reason when absent', () => {
    component.rejectionReason = undefined;
    fixture.detectChanges();
    const el: HTMLElement = fixture.nativeElement;
    expect(el.querySelector('[role="note"]')).toBeNull();
  });

  it('should display reviewedAt when present', () => {
    component.reviewedAt = '2024-02-01T10:30:00Z';
    fixture.detectChanges();
    const el: HTMLElement = fixture.nativeElement;
    expect(el.textContent).toContain('Revisado');
  });

  it('should display term months', () => {
    fixture.detectChanges();
    const el: HTMLElement = fixture.nativeElement;
    expect(el.textContent).toContain('12 meses');
  });
});
