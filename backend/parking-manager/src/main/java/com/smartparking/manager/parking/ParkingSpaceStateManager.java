package com.smartparking.manager.parking;

import com.smartparking.domain.enums.ParkingSpaceStatus;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Component
public class ParkingSpaceStateManager {
    private final Map<ParkingSpaceStatus, Set<ParkingSpaceStatus>> transitions = new EnumMap<>(ParkingSpaceStatus.class);

    public ParkingSpaceStateManager() {
        transitions.put(ParkingSpaceStatus.FREE, EnumSet.of(ParkingSpaceStatus.BOOKED, ParkingSpaceStatus.LOCKED));
        transitions.put(ParkingSpaceStatus.BOOKED, EnumSet.of(ParkingSpaceStatus.OCCUPIED, ParkingSpaceStatus.FREE));
        transitions.put(ParkingSpaceStatus.OCCUPIED, EnumSet.of(ParkingSpaceStatus.FREE));
        transitions.put(ParkingSpaceStatus.LOCKED, EnumSet.of(ParkingSpaceStatus.FREE));
    }

    public void validateTransition(ParkingSpaceStatus from, ParkingSpaceStatus to) {
        Set<ParkingSpaceStatus> allowed = transitions.getOrDefault(from, EnumSet.noneOf(ParkingSpaceStatus.class));
        if (!allowed.contains(to)) {
            throw new IllegalStateException("Illegal parking space status transition: " + from + " -> " + to);
        }
    }
}

