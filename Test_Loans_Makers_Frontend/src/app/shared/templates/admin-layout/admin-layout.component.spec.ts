import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AdminLayoutComponent } from './admin-layout.component';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';

describe('AdminLayoutComponent', () => {
  let component: AdminLayoutComponent;
  let fixture: ComponentFixture<AdminLayoutComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminLayoutComponent],
      providers: [provideAnimationsAsync()],
    }).compileComponents();

    fixture = TestBed.createComponent(AdminLayoutComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should have default pageTitle', () => {
    expect(component.pageTitle).toBe('Panel Administrativo');
  });

  it('should have default sidebarOpen true', () => {
    expect(component.sidebarOpen).toBe(true);
  });

  it('should emit logoutClick', () => {
    spyOn(component.logoutClick, 'emit');
    component.logoutClick.emit();
    expect(component.logoutClick.emit).toHaveBeenCalled();
  });

  it('should emit toggleClick', () => {
    spyOn(component.toggleClick, 'emit');
    component.toggleClick.emit();
    expect(component.toggleClick.emit).toHaveBeenCalled();
  });

  it('should render page title', () => {
    component.pageTitle = 'Custom Title';
    fixture.detectChanges();
    const el: HTMLElement = fixture.nativeElement;
    expect(el.textContent).toContain('Custom Title');
  });

  it('should display user email when provided', () => {
    component.userEmail = 'admin@test.com';
    fixture.detectChanges();
    const el: HTMLElement = fixture.nativeElement;
    expect(el.textContent).toContain('admin@test.com');
  });
});
