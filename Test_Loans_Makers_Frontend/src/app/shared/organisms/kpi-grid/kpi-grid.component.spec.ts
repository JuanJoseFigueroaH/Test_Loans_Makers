import { ComponentFixture, TestBed } from '@angular/core/testing';
import { KpiGridComponent, KpiItem } from './kpi-grid.component';

describe('KpiGridComponent', () => {
  let component: KpiGridComponent;
  let fixture: ComponentFixture<KpiGridComponent>;

  const mockItems: KpiItem[] = [
    { icon: 'assessment', value: 10, label: 'Total', color: 'primary', filterKey: 'ALL' },
    { icon: 'hourglass_empty', value: 5, label: 'Pending', color: 'warning', filterKey: 'PENDING', active: true },
    { icon: 'check_circle', value: 3, label: 'Approved', color: 'success', filterKey: 'APPROVED' },
    { icon: 'cancel', value: 2, label: 'Rejected', color: 'danger', filterKey: 'REJECTED' },
  ];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [KpiGridComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(KpiGridComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    component.items = mockItems;
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should emit kpiClick with filterKey', () => {
    component.items = mockItems;
    fixture.detectChanges();
    spyOn(component.kpiClick, 'emit');
    component.kpiClick.emit('PENDING');
    expect(component.kpiClick.emit).toHaveBeenCalledWith('PENDING');
  });

  it('should render all KPI cards', () => {
    component.items = mockItems;
    fixture.detectChanges();
    const cards = fixture.nativeElement.querySelectorAll('app-kpi-card');
    expect(cards.length).toBe(4);
  });

  it('should default to empty items', () => {
    expect(component.items).toEqual([]);
  });
});
