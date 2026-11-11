import { ComponentFixture, TestBed } from '@angular/core/testing';
import { EmptyStateComponent } from './empty-state.component';

describe('EmptyStateComponent', () => {
  let component: EmptyStateComponent;
  let fixture: ComponentFixture<EmptyStateComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EmptyStateComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(EmptyStateComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should have default icon', () => {
    expect(component.icon).toBe('inbox');
  });

  it('should have default title', () => {
    expect(component.title).toBe('Sin resultados');
  });

  it('should have empty default description', () => {
    expect(component.description).toBe('');
  });

  it('should render custom title', () => {
    component.title = 'No data found';
    fixture.detectChanges();
    const el: HTMLElement = fixture.nativeElement;
    expect(el.textContent).toContain('No data found');
  });

  it('should render custom description', () => {
    component.description = 'Try adjusting your filters';
    fixture.detectChanges();
    const el: HTMLElement = fixture.nativeElement;
    expect(el.textContent).toContain('Try adjusting your filters');
  });

  it('should render custom icon', () => {
    component.icon = 'search_off';
    fixture.detectChanges();
    const el: HTMLElement = fixture.nativeElement;
    expect(el.textContent).toContain('search_off');
  });

  it('should have status role', () => {
    fixture.detectChanges();
    const el = fixture.nativeElement.querySelector('[role="status"]');
    expect(el).toBeTruthy();
  });
});
