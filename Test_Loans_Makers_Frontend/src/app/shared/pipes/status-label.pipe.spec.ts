import { StatusLabelPipe } from './status-label.pipe';

describe('StatusLabelPipe', () => {
  let pipe: StatusLabelPipe;

  beforeEach(() => {
    pipe = new StatusLabelPipe();
  });

  it('should create', () => {
    expect(pipe).toBeTruthy();
  });

  it('should return PENDIENTE for PENDING', () => {
    expect(pipe.transform('PENDING')).toBe('PENDIENTE');
  });

  it('should return APROBADO for APPROVED', () => {
    expect(pipe.transform('APPROVED')).toBe('APROBADO');
  });

  it('should return RECHAZADO for REJECTED', () => {
    expect(pipe.transform('REJECTED')).toBe('RECHAZADO');
  });

  it('should return the original value for unknown status', () => {
    expect(pipe.transform('UNKNOWN')).toBe('UNKNOWN');
  });
});
