package com.smartparking.manager.parking;

import com.smartparking.domain.enums.ParkingSpaceStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ParkingSpaceStateManagerTest {
    @Test
    void allowsValidTransitions() {
        ParkingSpaceStateManager m = new ParkingSpaceStateManager();
        assertDoesNotThrow(() -> m.validateTransition(ParkingSpaceStatus.FREE, ParkingSpaceStatus.BOOKED));
        assertDoesNotThrow(() -> m.validateTransition(ParkingSpaceStatus.BOOKED, ParkingSpaceStatus.OCCUPIED));
        assertDoesNotThrow(() -> m.validateTransition(ParkingSpaceStatus.OCCUPIED, ParkingSpaceStatus.FREE));
    }

    @Test
    void rejectsInvalidTransitions() {
        ParkingSpaceStateManager m = new ParkingSpaceStateManager();
        assertThrows(IllegalStateException.class, () -> m.validateTransition(ParkingSpaceStatus.FREE, ParkingSpaceStatus.OCCUPIED));
        assertThrows(IllegalStateException.class, () -> m.validateTransition(ParkingSpaceStatus.OCCUPIED, ParkingSpaceStatus.BOOKED));
    }
}

