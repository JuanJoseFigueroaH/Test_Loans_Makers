import { ComponentFixture, TestBed } from '@angular/core/testing';
import { PaginationComponent } from './pagination.component';

describe('PaginationComponent', () => {
  let component: PaginationComponent;
  let fixture: ComponentFixture<PaginationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PaginationComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(PaginationComponent);
    component = fixture.componentInstance;
    component.currentPage = 0;
    component.totalPages = 5;
  });

  it('should create', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should display page info', () => {
    fixture.detectChanges();
    const el: HTMLElement = fixture.nativeElement;
    expect(el.textContent).toContain('Página 1 de 5');
  });

  it('should display total elements when provided', () => {
    component.totalElements = 50;
    fixture.detectChanges();
    const el: HTMLElement = fixture.nativeElement;
    expect(el.textContent).toContain('50 registros');
  });

  it('should emit previous page on prev click', () => {
    component.hasPrevious = true;
    component.currentPage = 2;
    fixture.detectChanges();
    spyOn(component.pageChange, 'emit');
    const buttons = fixture.nativeElement.querySelectorAll('button');
    buttons[0].click();
    expect(component.pageChange.emit).toHaveBeenCalledWith(1);
  });

  it('should emit next page on next click', () => {
    component.hasNext = true;
    component.currentPage = 2;
    fixture.detectChanges();
    spyOn(component.pageChange, 'emit');
    const buttons = fixture.nativeElement.querySelectorAll('button');
    buttons[1].click();
    expect(component.pageChange.emit).toHaveBeenCalledWith(3);
  });

  it('should disable previous button when hasPrevious is false', () => {
    component.hasPrevious = false;
    fixture.detectChanges();
    const buttons = fixture.nativeElement.querySelectorAll('button');
    expect(buttons[0].disabled).toBeTrue();
  });

  it('should disable next button when hasNext is false', () => {
    component.hasNext = false;
    fixture.detectChanges();
    const buttons = fixture.nativeElement.querySelectorAll('button');
    expect(buttons[1].disabled).toBeTrue();
  });

  it('should default hasPrevious to false', () => {
    expect(component.hasPrevious).toBe(false);
  });

  it('should default hasNext to false', () => {
    expect(component.hasNext).toBe(false);
  });
});
