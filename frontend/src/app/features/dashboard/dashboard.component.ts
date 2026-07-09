import { Component, OnDestroy, OnInit, inject, signal } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Subject, takeUntil } from 'rxjs';
import { GarageApiService } from '../../core/services/garage-api.service';
import { GarageRefreshService } from '../../core/services/garage-refresh.service';
import { GarageStatusResponse } from '../../core/models/garage.models';

@Component({
  selector: 'app-dashboard',
  imports: [MatCardModule, MatIconModule, MatProgressSpinnerModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit, OnDestroy {
  private readonly garageApi = inject(GarageApiService);
  private readonly refreshService = inject(GarageRefreshService);
  private readonly destroy$ = new Subject<void>();

  readonly loading = signal(true);
  readonly status = signal<GarageStatusResponse | null>(null);

  ngOnInit(): void {
    this.loadStatus();
    this.refreshService.refresh$.pipe(takeUntil(this.destroy$)).subscribe(() => this.loadStatus());
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private loadStatus(): void {
    this.loading.set(true);
    this.garageApi.getStatus().subscribe({
      next: (response) => {
        this.status.set(response);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }
}
