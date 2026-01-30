package com.smartparking.service;

import com.smartparking.domain.model.ParkingSpot;
import java.util.List;

public interface ParkingSpotService {
    List<ParkingSpot> getSpotsByLot(Long lotId);
    ParkingSpot getSpot(Long lotId, String spotNumber);
    boolean isAvailable(Long spotId);
    // More management methods can be added
}
