import { GarageStatusResponse, SlotViewModel } from '../models/garage.models';

export function buildSlotViewModels(status: GarageStatusResponse): SlotViewModel[] {
  const occupiedMap = new Map<number, GarageStatusResponse['parkedVehicles'][number]>();

  for (const vehicle of status.parkedVehicles) {
    for (const slot of vehicle.allocatedSlots) {
      occupiedMap.set(slot, vehicle);
    }
  }

  const bufferSlots = new Set<number>();
  const sortedVehicles = [...status.parkedVehicles].sort(
    (left, right) => Math.min(...left.allocatedSlots) - Math.min(...right.allocatedSlots)
  );

  for (let index = 0; index < sortedVehicles.length - 1; index++) {
    const maxLeft = Math.max(...sortedVehicles[index].allocatedSlots);
    const minRight = Math.min(...sortedVehicles[index + 1].allocatedSlots);
    if (minRight - maxLeft === 2) {
      bufferSlots.add(maxLeft + 1);
    }
  }

  const slots: SlotViewModel[] = [];
  for (let slotNumber = 1; slotNumber <= status.capacity; slotNumber++) {
    if (occupiedMap.has(slotNumber)) {
      slots.push({
        slotNumber,
        status: 'occupied',
        vehicle: occupiedMap.get(slotNumber)
      });
      continue;
    }

    if (bufferSlots.has(slotNumber)) {
      slots.push({ slotNumber, status: 'buffer' });
      continue;
    }

    slots.push({ slotNumber, status: 'available' });
  }

  return slots;
}

export function slotTooltip(vehicle: GarageStatusResponse['parkedVehicles'][number]): string {
  return `${vehicle.plateNumber} | ${vehicle.vehicleType} | Slots: ${vehicle.allocatedSlots.join(', ')}`;
}
