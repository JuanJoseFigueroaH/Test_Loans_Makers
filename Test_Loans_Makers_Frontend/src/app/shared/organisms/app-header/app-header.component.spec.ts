import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AppHeaderComponent } from './app-header.component';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';

describe('AppHeaderComponent', () => {
  let component: AppHeaderComponent;
  let fixture: ComponentFixture<AppHeaderComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AppHeaderComponent],
      providers: [provideAnimationsAsync()],
    }).compileComponents();

    fixture = TestBed.createComponent(AppHeaderComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should emit logoutClick', () => {
    fixture.detectChanges();
    spyOn(component.logoutClick, 'emit');
    component.logoutClick.emit();
    expect(component.logoutClick.emit).toHaveBeenCalled();
  });

  it('should emit menuClick', () => {
    fixture.detectChanges();
    spyOn(component.menuClick, 'emit');
    component.menuClick.emit();
    expect(component.menuClick.emit).toHaveBeenCalled();
  });

  it('should display user email when provided', () => {
    component.userEmail = 'user@test.com';
    fixture.detectChanges();
    const el: HTMLElement = fixture.nativeElement;
    expect(el.textContent).toContain('user@test.com');
  });

  it('should default showMenuButton to false', () => {
    expect(component.showMenuButton).toBe(false);
  });
});
