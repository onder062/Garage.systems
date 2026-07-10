package com.onder.garage.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import com.onder.garage.dto.ParkVehicleRequest;
import com.onder.garage.dto.ParkVehicleResponse;
import com.onder.garage.dto.VehicleResponse;
import com.onder.garage.exception.GarageFullException;
import com.onder.garage.exception.InvalidVehicleException;
import com.onder.garage.exception.VehicleAlreadyExistsException;
import com.onder.garage.exception.VehicleNotFoundException;
import com.onder.garage.model.ParkingSpot;
import com.onder.garage.model.Vehicle;
import com.onder.garage.model.VehicleType;
import com.onder.garage.repository.JpaGarageRepository;
import com.onder.garage.repository.ParkingSpotJpaRepository;
import com.onder.garage.repository.TicketJpaRepository;
import com.onder.garage.repository.TicketSequenceJpaRepository;
import com.onder.garage.repository.VehicleJpaRepository;
import com.onder.garage.util.ParkingSpotUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest
@Import(JpaGarageRepository.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class GarageServiceImplTest {

    @Autowired
    private JpaGarageRepository repository;

    @Autowired
    private VehicleJpaRepository vehicleJpaRepository;

    @Autowired
    private TicketJpaRepository ticketJpaRepository;

    @Autowired
    private ParkingSpotJpaRepository parkingSpotJpaRepository;

    @Autowired
    private TicketSequenceJpaRepository ticketSequenceJpaRepository;

    private ExecutorService executorService;

    @BeforeEach
    void cleanDatabase() {
        parkingSpotJpaRepository.deleteAll();
        ticketJpaRepository.deleteAll();
        vehicleJpaRepository.deleteAll();
        ticketSequenceJpaRepository.deleteAll();
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        if (executorService != null) {
            executorService.shutdownNow();
            executorService.awaitTermination(2, TimeUnit.SECONDS);
        }
    }

    private GarageServiceImpl service(int capacity) {
        return new GarageServiceImpl(repository, capacity);
    }

    @Test
    void parkCar_shouldAllocateOneSlot() {
        JpaGarageRepository spyRepository = spy(new JpaGarageRepository(
                vehicleJpaRepository,
                ticketJpaRepository,
                parkingSpotJpaRepository,
                ticketSequenceJpaRepository
        ));
        GarageServiceImpl garageService = new GarageServiceImpl(spyRepository, 10);

        ParkVehicleResponse response = garageService.parkVehicle(new ParkVehicleRequest("34ABC34", "Alice", VehicleType.CAR));

        assertNotNull(response);
        assertEquals(1, response.allocatedSlots());
        assertEquals("34ABC34", response.vehicle().plateNumber());
        assertEquals(1, garageService.getOccupiedCapacity());
        verify(spyRepository).saveVehicle(any());
    }

    @Test
    void parkJeep_shouldAllocateTwoConsecutiveSlots() {
        GarageServiceImpl garageService = service(10);

        ParkVehicleResponse response = garageService.parkVehicle(new ParkVehicleRequest("34JEEP34", "Bob", VehicleType.JEEP));

        assertEquals(2, response.allocatedSlots());
        assertEquals(2, garageService.getOccupiedCapacity());
        assertEquals(8, garageService.getRemainingCapacity());
    }

    @Test
    void sampleOutput1Layout_shouldKeepOneBufferBetweenVehicles() {
        GarageServiceImpl garageService = service(10);

        garageService.parkVehicle(new ParkVehicleRequest("34CAR34", "Car Owner", VehicleType.CAR));
        garageService.parkVehicle(new ParkVehicleRequest("34TRK34", "Truck Owner", VehicleType.TRUCK));

        List<ParkingSpot> spots = ParkingSpotUtils.sortBySlotOrder(repository.findAllParkingSpots());

        ParkingSpot s1 = spots.get(0);
        ParkingSpot s2 = spots.get(1);
        ParkingSpot s3 = spots.get(2);
        ParkingSpot s6 = spots.get(5);

        assertTrue(s1.isOccupied());
        assertEquals("34CAR34", s1.getVehicle().getPlateNumber());

        assertFalse(s2.isOccupied());
        assertNull(s2.getVehicle());

        assertTrue(s3.isOccupied());
        assertEquals("34TRK34", s3.getVehicle().getPlateNumber());
        assertTrue(s6.isOccupied());
        assertEquals("34TRK34", s6.getVehicle().getPlateNumber());
    }

    @Test
    void parkTruck_shouldAllocateFourConsecutiveSlots() {
        GarageServiceImpl garageService = service(10);

        ParkVehicleResponse response = garageService.parkVehicle(new ParkVehicleRequest("34TRK34", "Carol", VehicleType.TRUCK));

        assertEquals(4, response.allocatedSlots());
        assertEquals(4, garageService.getOccupiedCapacity());
        assertEquals(6, garageService.getRemainingCapacity());
    }

    @Test
    void garageFullOnTenSlotGarage_shouldThrowGarageFullException() {
        GarageServiceImpl garageService = service(10);
        garageService.parkVehicle(new ParkVehicleRequest("34CAR34", "Car Owner", VehicleType.CAR));
        garageService.parkVehicle(new ParkVehicleRequest("34TRK34", "Truck Owner", VehicleType.TRUCK));
        garageService.parkVehicle(new ParkVehicleRequest("34CAR02", "Car Two", VehicleType.CAR));
        garageService.parkVehicle(new ParkVehicleRequest("34CAR03", "Car Three", VehicleType.CAR));

        GarageFullException ex = assertThrows(
                GarageFullException.class,
                () -> garageService.parkVehicle(new ParkVehicleRequest("34CAR04", "Car Four", VehicleType.CAR))
        );
        assertTrue(ex.getMessage().contains("Garage is full"));
    }

    @Test
    void vehicleAlreadyExists_shouldThrowException() {
        GarageServiceImpl garageService = service(10);
        garageService.parkVehicle(new ParkVehicleRequest("34DUP34", "Frank", VehicleType.CAR));

        assertThrows(
                VehicleAlreadyExistsException.class,
                () -> garageService.parkVehicle(new ParkVehicleRequest("34DUP34", "Frank 2", VehicleType.JEEP))
        );
    }

    @Test
    void removeVehicle_shouldReleaseSlotsAndReturnVehicle() {
        GarageServiceImpl garageService = service(10);
        garageService.parkVehicle(new ParkVehicleRequest("34REM34", "Grace", VehicleType.JEEP));

        VehicleResponse removed = garageService.removeVehicle("34REM34");

        assertEquals("34REM34", removed.plateNumber());
        assertEquals(0, garageService.getOccupiedCapacity());
        assertEquals(10, garageService.getRemainingCapacity());
        assertThrows(VehicleNotFoundException.class, () -> garageService.findVehicle("34REM34"));
        assertThrows(VehicleNotFoundException.class, () -> garageService.removeVehicle("34REM34"));
    }

    @Test
    void findVehicle_shouldReturnVehicleWhenPresent() {
        GarageServiceImpl garageService = service(10);
        garageService.parkVehicle(new ParkVehicleRequest("34FIND34", "Henry", VehicleType.CAR));

        assertEquals("34FIND34", garageService.findVehicle("34FIND34").plateNumber());
        assertThrows(VehicleNotFoundException.class, () -> garageService.findVehicle("34NONE34"));
    }

    @Test
    void remainingCapacity_shouldBeCalculatedFromOccupiedSlots() {
        GarageServiceImpl garageService = service(7);
        garageService.parkVehicle(new ParkVehicleRequest("34CAP01", "Ivy", VehicleType.JEEP));
        garageService.parkVehicle(new ParkVehicleRequest("34CAP02", "Jack", VehicleType.CAR));

        assertEquals(4, garageService.getRemainingCapacity());
        assertEquals(3, garageService.getOccupiedCapacity());
    }

    @Test
    void adjacentPlacementWithoutBuffer_shouldBeRejected() {
        GarageServiceImpl garageService = service(6);
        garageService.parkVehicle(new ParkVehicleRequest("34A", "Owner A", VehicleType.CAR));
        garageService.parkVehicle(new ParkVehicleRequest("34B", "Owner B", VehicleType.CAR));
        garageService.parkVehicle(new ParkVehicleRequest("34C", "Owner C", VehicleType.CAR));

        assertThrows(
                GarageFullException.class,
                () -> garageService.parkVehicle(new ParkVehicleRequest("34D", "Owner D", VehicleType.CAR))
        );
    }

    @Test
    void garageStatus_shouldReturnCapacityAndVehicleSnapshotWithSlotNumbers() {
        GarageServiceImpl garageService = service(10);
        garageService.parkVehicle(new ParkVehicleRequest("34STA01", "Liam", VehicleType.CAR));
        garageService.parkVehicle(new ParkVehicleRequest("34STA02", "Mia", VehicleType.TRUCK));

        var status = garageService.getGarageStatus();

        assertEquals(10, status.capacity());
        assertEquals(5, status.occupiedSlots());
        assertEquals(5, status.availableSlots());
        assertEquals(2, status.parkedVehicles().size());
        assertEquals(List.of(1), status.parkedVehicles().stream()
                .filter(v -> v.plateNumber().equals("34STA01"))
                .findFirst()
                .orElseThrow()
                .allocatedSlots());
        assertEquals(List.of(3, 4, 5, 6), status.parkedVehicles().stream()
                .filter(v -> v.plateNumber().equals("34STA02"))
                .findFirst()
                .orElseThrow()
                .allocatedSlots());
    }

    @Test
    void serviceInit_shouldRespectPreloadedParkingSpots() {
        repository.saveParkingSpot(ParkingSpot.builder()
                .id("S-1")
                .occupied(true)
                .vehicle(Vehicle.builder()
                        .plateNumber("34PRE01")
                        .ownerName("Noah")
                        .vehicleType(VehicleType.CAR)
                        .ticketId("TKT-1")
                        .build())
                .build());
        repository.saveParkingSpot(ParkingSpot.builder().id("S-2").occupied(false).build());
        GarageServiceImpl garageService = service(2);

        assertEquals(1, garageService.getOccupiedCapacity());
        assertEquals(1, garageService.getRemainingCapacity());
    }

    @Test
    void invalidPlate_shouldThrowInvalidVehicleException() {
        GarageServiceImpl garageService = service(10);

        assertThrows(InvalidVehicleException.class, () -> garageService.removeVehicle("   "));
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void concurrency_shouldHandleSimultaneousParkingRequestsThreadSafely() throws Exception {
        GarageServiceImpl garageService = service(10);
        int requestCount = 8;
        executorService = Executors.newFixedThreadPool(8);
        CountDownLatch startLatch = new CountDownLatch(1);

        List<Callable<Object>> tasks = IntStream.range(0, requestCount)
                .mapToObj(i -> (Callable<Object>) () -> {
                    startLatch.await(5, TimeUnit.SECONDS);
                    try {
                        return garageService.parkVehicle(new ParkVehicleRequest(
                                "34CON" + i,
                                "User-" + i,
                                VehicleType.CAR
                        ));
                    } catch (GarageFullException ex) {
                        return ex;
                    }
                })
                .toList();

        List<Future<Object>> futures = new ArrayList<>();
        for (Callable<Object> task : tasks) {
            futures.add(executorService.submit(task));
        }
        startLatch.countDown();

        int successCount = 0;
        int fullCount = 0;
        List<String> ticketIds = new ArrayList<>();
        for (Future<Object> future : futures) {
            Object result = future.get(10, TimeUnit.SECONDS);
            if (result instanceof ParkVehicleResponse response) {
                successCount++;
                ticketIds.add(response.ticketId());
            } else {
                fullCount++;
            }
        }

        assertEquals(5, successCount);
        assertEquals(3, fullCount);
        assertEquals(5, ticketIds.stream().distinct().count());
        assertEquals(5, garageService.getOccupiedCapacity());
        assertEquals(5, garageService.getRemainingCapacity());
    }
}
