import { ComponentFixture, TestBed } from '@angular/core/testing';
import { StatusBadgeComponent } from './status-badge.component';

describe('StatusBadgeComponent', () => {
  let component: StatusBadgeComponent;
  let fixture: ComponentFixture<StatusBadgeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [StatusBadgeComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(StatusBadgeComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    component.status = 'PENDING';
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  describe('label', () => {
    it('should return PENDIENTE for PENDING', () => {
      component.status = 'PENDING';
      expect(component.label).toBe('PENDIENTE');
    });

    it('should return APROBADO for APPROVED', () => {
      component.status = 'APPROVED';
      expect(component.label).toBe('APROBADO');
    });

    it('should return RECHAZADO for REJECTED', () => {
      component.status = 'REJECTED';
      expect(component.label).toBe('RECHAZADO');
    });

    it('should return the status itself for unknown status', () => {
      component.status = 'UNKNOWN';
      expect(component.label).toBe('UNKNOWN');
    });
  });

  describe('dotClass', () => {
    it('should return amber for PENDING', () => {
      component.status = 'PENDING';
      expect(component.dotClass).toBe('bg-amber-500');
    });

    it('should return emerald for APPROVED', () => {
      component.status = 'APPROVED';
      expect(component.dotClass).toBe('bg-emerald-500');
    });

    it('should return red for REJECTED', () => {
      component.status = 'REJECTED';
      expect(component.dotClass).toBe('bg-red-500');
    });

    it('should return gray for unknown', () => {
      component.status = 'OTHER';
      expect(component.dotClass).toBe('bg-gray-500');
    });
  });

  describe('badgeClasses', () => {
    it('should contain amber classes for PENDING', () => {
      component.status = 'PENDING';
      expect(component.badgeClasses).toContain('bg-amber-100');
      expect(component.badgeClasses).toContain('text-amber-700');
    });

    it('should contain emerald classes for APPROVED', () => {
      component.status = 'APPROVED';
      expect(component.badgeClasses).toContain('bg-emerald-100');
    });

    it('should contain red classes for REJECTED', () => {
      component.status = 'REJECTED';
      expect(component.badgeClasses).toContain('bg-red-100');
    });

    it('should contain gray classes for unknown', () => {
      component.status = 'OTHER';
      expect(component.badgeClasses).toContain('bg-gray-100');
    });

    it('should use sm size by default', () => {
      component.status = 'PENDING';
      expect(component.badgeClasses).toContain('px-3');
    });

    it('should use lg size when set', () => {
      component.status = 'PENDING';
      component.size = 'lg';
      expect(component.badgeClasses).toContain('px-4');
    });
  });

  it('should render the label in the template', () => {
    component.status = 'APPROVED';
    fixture.detectChanges();
    const el: HTMLElement = fixture.nativeElement;
    expect(el.textContent).toContain('APROBADO');
  });
});
