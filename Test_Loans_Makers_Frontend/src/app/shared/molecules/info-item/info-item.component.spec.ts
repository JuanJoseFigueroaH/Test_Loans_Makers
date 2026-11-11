import { ComponentFixture, TestBed } from '@angular/core/testing';
import { InfoItemComponent } from './info-item.component';

describe('InfoItemComponent', () => {
  let component: InfoItemComponent;
  let fixture: ComponentFixture<InfoItemComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InfoItemComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(InfoItemComponent);
    component = fixture.componentInstance;
    component.label = 'Amount';
    component.value = '$5,000,000';
  });

  it('should create', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should render label', () => {
    fixture.detectChanges();
    const el: HTMLElement = fixture.nativeElement;
    expect(el.textContent).toContain('Amount');
  });

  it('should render value', () => {
    fixture.detectChanges();
    const el: HTMLElement = fixture.nativeElement;
    expect(el.textContent).toContain('$5,000,000');
  });

  it('should default mono to false', () => {
    expect(component.mono).toBe(false);
  });

  it('should accept mono input', () => {
    component.mono = true;
    expect(component.mono).toBe(true);
  });
});
