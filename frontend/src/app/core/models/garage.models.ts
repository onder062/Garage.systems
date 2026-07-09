export type VehicleType = 'CAR' | 'JEEP' | 'TRUCK';

export interface VehicleResponse {
  plateNumber: string;
  ownerName: string;
  vehicleType: VehicleType;
  ticketId: string;
  allocatedSlots: number[];
}

export interface GarageStatusResponse {
  capacity: number;
  occupiedSlots: number;
  availableSlots: number;
  parkedVehicles: VehicleResponse[];
}

export interface ParkVehicleRequest {
  plateNumber: string;
  ownerName: string;
  vehicleType: VehicleType;
}

export interface ParkVehicleResponse {
  ticketId: string;
  allocatedSlots: number;
  vehicle: VehicleResponse;
}

export interface ApiErrorResponse {
  timestamp?: string;
  status: number;
  error?: string;
  message?: string;
  path?: string;
  validationErrors?: Record<string, string>;
}

export type SlotStatus = 'occupied' | 'available' | 'buffer';

export interface SlotViewModel {
  slotNumber: number;
  status: SlotStatus;
  vehicle?: VehicleResponse;
}
