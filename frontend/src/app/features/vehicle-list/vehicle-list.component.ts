import { Component, OnDestroy, OnInit, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { Subject, takeUntil } from 'rxjs';
import { GarageApiService } from '../../core/services/garage-api.service';
import { GarageRefreshService } from '../../core/services/garage-refresh.service';
import { VehicleResponse } from '../../core/models/garage.models';
import { extractErrorMessage } from '../../core/utils/error.util';

@Component({
  selector: 'app-vehicle-list',
  imports: [
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatSnackBarModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './vehicle-list.component.html',
  styleUrl: './vehicle-list.component.scss'
})
export class VehicleListComponent implements OnInit, OnDestroy {
  private readonly garageApi = inject(GarageApiService);
  private readonly refreshService = inject(GarageRefreshService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly destroy$ = new Subject<void>();

  readonly loading = signal(true);
  readonly leavingPlate = signal<string | null>(null);
  readonly vehicles = signal<VehicleResponse[]>([]);
  readonly displayedColumns = ['plateNumber', 'vehicleType', 'occupiedSlots', 'entryTime', 'actions'];

  ngOnInit(): void {
    this.loadVehicles();
    this.refreshService.refresh$.pipe(takeUntil(this.destroy$)).subscribe(() => this.loadVehicles());
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  leaveVehicle(plateNumber: string): void {
    if (this.leavingPlate()) {
      return;
    }

    this.leavingPlate.set(plateNumber);
    this.garageApi.removeVehicle(plateNumber).subscribe({
      next: () => {
        this.leavingPlate.set(null);
        this.refreshService.notifyRefresh();
        this.snackBar.open(`Vehicle ${plateNumber} removed successfully.`, 'Close', { duration: 4000 });
      },
      error: (error) => {
        this.leavingPlate.set(null);
        this.snackBar.open(
          extractErrorMessage(error, 'Failed to remove vehicle.'),
          'Close',
          { duration: 6000, panelClass: ['error-snackbar'] }
        );
      }
    });
  }

  formatSlots(slots: number[]): string {
    return slots.join(', ');
  }

  private loadVehicles(): void {
    this.loading.set(true);
    this.garageApi.getVehicles().subscribe({
      next: (response) => {
        this.vehicles.set(response);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }
}
