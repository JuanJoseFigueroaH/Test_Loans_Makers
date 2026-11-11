import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { LoanDetailComponent } from './loan-detail.component';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter, Router, ActivatedRoute } from '@angular/router';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { LoanStore } from '../../../core/store/loan.store';

describe('LoanDetailComponent', () => {
  let component: LoanDetailComponent;
  let fixture: ComponentFixture<LoanDetailComponent>;
  let httpTesting: HttpTestingController;
  let router: Router;
  let loanStore: InstanceType<typeof LoanStore>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LoanDetailComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([{ path: 'dashboard', component: LoanDetailComponent }]),
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
    httpTesting = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    loanStore = TestBed.inject(LoanStore);
  });

  afterEach(() => {
    httpTesting.verify();
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

  it('should return help icon for unknown status', () => {
    expect(component.getStatusIcon('OTHER')).toBe('help');
  });

  it('should load loan on init', () => {
    component.ngOnInit();
    const req = httpTesting.expectOne(r => r.url.includes('/loans/00000000'));
    req.flush({
      id: '00000000-0000-0000-0000-000000000001',
      userId: 'u1', amount: 5000000, termMonths: 12,
      status: 'PENDING', createdAt: '', updatedAt: '',
    });
    expect(loanStore.selectedLoan()).toBeTruthy();
  });


  it('should set error on 404', fakeAsync(() => {
    component.ngOnInit();
    for (let i = 0; i < 3; i++) {
      const req = httpTesting.expectOne(r => r.url.includes('/loans/00000000'));
      req.flush({}, { status: 404, statusText: 'Not Found' });
      if (i < 2) tick(1000);
    }
    tick();
    expect(loanStore.error()).toBe('Solicitud no encontrada');
  }));

  it('should set generic error on other failures', fakeAsync(() => {
    component.ngOnInit();
    for (let i = 0; i < 3; i++) {
      const req = httpTesting.expectOne(r => r.url.includes('/loans/00000000'));
      req.flush({ detail: 'Server error' }, { status: 500, statusText: 'Error' });
      if (i < 2) tick(1000);
    }
    tick();
    expect(loanStore.error()).toBe('Server error');
  }));

  it('should navigate back to dashboard', () => {
    spyOn(router, 'navigate');
    component.goBack();
    expect(router.navigate).toHaveBeenCalledWith(['/dashboard']);
  });
});
