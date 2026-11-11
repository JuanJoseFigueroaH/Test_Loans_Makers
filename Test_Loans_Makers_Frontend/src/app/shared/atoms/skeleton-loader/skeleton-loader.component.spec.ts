import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SkeletonLoaderComponent } from './skeleton-loader.component';

describe('SkeletonLoaderComponent', () => {
  let component: SkeletonLoaderComponent;
  let fixture: ComponentFixture<SkeletonLoaderComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SkeletonLoaderComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(SkeletonLoaderComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  describe('sizeClass', () => {
    it('should default to full width and sm height', () => {
      expect(component.sizeClass).toBe('w-full h-3.5');
    });

    it('should return short width', () => {
      component.width = 'short';
      expect(component.sizeClass).toContain('w-[30%]');
    });

    it('should return medium width', () => {
      component.width = 'medium';
      expect(component.sizeClass).toContain('w-[50%]');
    });

    it('should return wide width', () => {
      component.width = 'wide';
      expect(component.sizeClass).toContain('w-[70%]');
    });

    it('should return md height', () => {
      component.height = 'md';
      expect(component.sizeClass).toContain('h-5');
    });

    it('should return lg height', () => {
      component.height = 'lg';
      expect(component.sizeClass).toContain('h-6');
    });
  });

  it('should have presentation role', () => {
    fixture.detectChanges();
    const el = fixture.nativeElement.querySelector('[role="presentation"]');
    expect(el).toBeTruthy();
  });
});
