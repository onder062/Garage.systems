import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { GarageApiService } from '../../core/services/garage-api.service';
import { GarageRefreshService } from '../../core/services/garage-refresh.service';
import { VehicleType } from '../../core/models/garage.models';
import { extractErrorMessage } from '../../core/utils/error.util';

@Component({
  selector: 'app-park-vehicle',
  imports: [
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatSnackBarModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './park-vehicle.component.html',
  styleUrl: './park-vehicle.component.scss'
})
export class ParkVehicleComponent {
  private readonly fb = inject(FormBuilder);
  private readonly garageApi = inject(GarageApiService);
  private readonly refreshService = inject(GarageRefreshService);
  private readonly snackBar = inject(MatSnackBar);

  readonly submitting = signal(false);
  readonly vehicleTypes: VehicleType[] = ['CAR', 'JEEP', 'TRUCK'];

  readonly form = this.fb.nonNullable.group({
    plateNumber: ['', [Validators.required, Validators.minLength(2)]],
    ownerName: ['Guest', [Validators.required, Validators.minLength(2)]],
    vehicleType: ['CAR' as VehicleType, Validators.required]
  });

  submit(): void {
    if (this.form.invalid || this.submitting()) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting.set(true);
    this.garageApi.parkVehicle(this.form.getRawValue()).subscribe({
      next: (response) => {
        this.submitting.set(false);
        this.refreshService.notifyRefresh();
        this.form.patchValue({ plateNumber: '' });
        this.snackBar.open(
          `Vehicle parked successfully. Ticket: ${response.ticketId}, Slots: ${response.allocatedSlots}`,
          'Close',
          { duration: 5000 }
        );
      },
      error: (error) => {
        this.submitting.set(false);
        this.snackBar.open(
          extractErrorMessage(error, 'Failed to park vehicle.'),
          'Close',
          { duration: 6000, panelClass: ['error-snackbar'] }
        );
      }
    });
  }
}
