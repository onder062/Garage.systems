import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./layout/shell/shell.component').then((m) => m.ShellComponent),
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./features/dashboard/dashboard.component').then((m) => m.DashboardComponent)
      },
      {
        path: 'park',
        loadComponent: () =>
          import('./features/park-vehicle/park-vehicle.component').then((m) => m.ParkVehicleComponent)
      },
      {
        path: 'status',
        loadComponent: () =>
          import('./features/garage-status/garage-status.component').then((m) => m.GarageStatusComponent)
      },
      {
        path: 'vehicles',
        loadComponent: () =>
          import('./features/vehicle-list/vehicle-list.component').then((m) => m.VehicleListComponent)
      }
    ]
  },
  { path: '**', redirectTo: 'dashboard' }
];
