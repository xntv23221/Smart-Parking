package com.smartparking.service;

import com.smartparking.domain.model.ParkingRecord;
import java.util.List;

public interface ParkingRecordService {
    ParkingRecord book(Long userId, Long vehicleId, Long lotId);
    ParkingRecord cancel(Long userId, Long recordId);
    ParkingRecord entry(Long userId, Long vehicleId, Long lotId, Long spotId);
    ParkingRecord entry(Long userId, Long vehicleId, Long lotId); // Auto-assign spot
    ParkingRecord exit(Long recordId);
    ParkingRecord pay(Long recordId, String paymentMethod);
    List<ParkingRecord> getMyRecords(Long userId);
}
