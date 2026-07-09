import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  GarageStatusResponse,
  ParkVehicleRequest,
  ParkVehicleResponse,
  VehicleResponse
} from '../models/garage.models';

@Injectable({ providedIn: 'root' })
export class GarageApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/garage`;

  getStatus(): Observable<GarageStatusResponse> {
    return this.http.get<GarageStatusResponse>(`${this.baseUrl}/status`);
  }

  getVehicles(): Observable<VehicleResponse[]> {
    return this.http.get<VehicleResponse[]>(`${this.baseUrl}/vehicles`);
  }

  parkVehicle(request: ParkVehicleRequest): Observable<ParkVehicleResponse> {
    return this.http.post<ParkVehicleResponse>(`${this.baseUrl}/park`, request);
  }

  removeVehicle(plateNumber: string): Observable<VehicleResponse> {
    return this.http.delete<VehicleResponse>(`${this.baseUrl}/remove/${encodeURIComponent(plateNumber)}`);
  }
}
