import { ComponentFixture, TestBed } from '@angular/core/testing';
import { KpiCardComponent } from './kpi-card.component';

describe('KpiCardComponent', () => {
  let component: KpiCardComponent;
  let fixture: ComponentFixture<KpiCardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [KpiCardComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(KpiCardComponent);
    component = fixture.componentInstance;
    component.icon = 'assessment';
    component.value = 42;
    component.label = 'Total';
  });

  it('should create', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  describe('iconBgClass', () => {
    it('should return blue for primary', () => {
      component.color = 'primary';
      expect(component.iconBgClass).toBe('bg-blue-600');
    });

    it('should return amber for warning', () => {
      component.color = 'warning';
      expect(component.iconBgClass).toBe('bg-amber-500');
    });

    it('should return emerald for success', () => {
      component.color = 'success';
      expect(component.iconBgClass).toBe('bg-emerald-500');
    });

    it('should return red for danger', () => {
      component.color = 'danger';
      expect(component.iconBgClass).toBe('bg-red-500');
    });
  });

  it('should emit cardClick on click', () => {
    fixture.detectChanges();
    spyOn(component.cardClick, 'emit');
    const button = fixture.nativeElement.querySelector('button');
    button.click();
    expect(component.cardClick.emit).toHaveBeenCalled();
  });

  it('should render value and label', () => {
    fixture.detectChanges();
    const el: HTMLElement = fixture.nativeElement;
    expect(el.textContent).toContain('42');
    expect(el.textContent).toContain('Total');
  });

  it('should default active to false', () => {
    expect(component.active).toBe(false);
  });

  it('should set aria-pressed attribute', () => {
    component.active = true;
    fixture.detectChanges();
    const button = fixture.nativeElement.querySelector('button');
    expect(button.getAttribute('aria-pressed')).toBe('true');
  });
});
