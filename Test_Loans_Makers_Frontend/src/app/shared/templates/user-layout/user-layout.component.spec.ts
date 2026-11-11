import { ComponentFixture, TestBed } from '@angular/core/testing';
import { UserLayoutComponent } from './user-layout.component';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';

describe('UserLayoutComponent', () => {
  let component: UserLayoutComponent;
  let fixture: ComponentFixture<UserLayoutComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UserLayoutComponent],
      providers: [provideAnimationsAsync()],
    }).compileComponents();

    fixture = TestBed.createComponent(UserLayoutComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should emit logoutClick', () => {
    spyOn(component.logoutClick, 'emit');
    component.logoutClick.emit();
    expect(component.logoutClick.emit).toHaveBeenCalled();
  });

  it('should accept userEmail input', () => {
    component.userEmail = 'user@test.com';
    fixture.detectChanges();
    expect(component.userEmail).toBe('user@test.com');
  });

  it('should have undefined userEmail by default', () => {
    expect(component.userEmail).toBeUndefined();
  });
});
