package com.smartparking.service;

import com.smartparking.domain.model.ParkingLot;
import java.util.List;

public interface ParkingLotService {
    List<ParkingLot> getAllParkingLots();

    List<ParkingLot> getParkingLotsByManager(Long managerId);

    void updateParkingLot(ParkingLot parkingLot);

    ParkingLot getParkingLotById(Long lotId);

    ParkingLot getParkingLotByName(String name);

    void createParkingLot(ParkingLot parkingLot);
}
