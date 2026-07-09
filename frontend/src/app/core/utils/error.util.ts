import { HttpErrorResponse } from '@angular/common/http';
import { ApiErrorResponse } from '../models/garage.models';

export function extractErrorMessage(error: unknown, fallback: string): string {
  if (!(error instanceof HttpErrorResponse)) {
    return fallback;
  }

  const body = error.error as ApiErrorResponse | string | null;
  if (!body) {
    return fallback;
  }

  if (typeof body === 'string') {
    return body;
  }

  if (body.validationErrors && Object.keys(body.validationErrors).length > 0) {
    return Object.values(body.validationErrors).join(', ');
  }

  return body.message ?? fallback;
}
