import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'statusLabel',
  standalone: true,
})
export class StatusLabelPipe implements PipeTransform {
  private static readonly LABELS: Record<string, string> = {
    PENDING: 'PENDIENTE',
    APPROVED: 'APROBADO',
    REJECTED: 'RECHAZADO',
  };

  transform(status: string): string {
    return StatusLabelPipe.LABELS[status] ?? status;
  }
}
