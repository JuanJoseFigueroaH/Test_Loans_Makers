import { ComponentFixture, TestBed } from '@angular/core/testing';
import { LoanDetailComponent } from './loan-detail.component';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter, ActivatedRoute } from '@angular/router';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';

describe('LoanDetailComponent', () => {
  let component: LoanDetailComponent;
  let fixture: ComponentFixture<LoanDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LoanDetailComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        provideAnimationsAsync(),
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: {
                get: () => '00000000-0000-0000-0000-000000000001',
              },
            },
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(LoanDetailComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should return correct status icon for PENDING', () => {
    expect(component.getStatusIcon('PENDING')).toBe('hourglass_empty');
  });

  it('should return correct status icon for APPROVED', () => {
    expect(component.getStatusIcon('APPROVED')).toBe('check_circle');
  });

  it('should return correct status icon for REJECTED', () => {
    expect(component.getStatusIcon('REJECTED')).toBe('cancel');
  });
});
