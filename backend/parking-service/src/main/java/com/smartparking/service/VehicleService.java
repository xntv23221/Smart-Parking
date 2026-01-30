package com.smartparking.service;

import com.smartparking.domain.model.Vehicle;
import java.util.List;

public interface VehicleService {
    List<Vehicle> getMyVehicles(Long userId);
    void addVehicle(Long userId, Vehicle vehicle);
    void removeVehicle(Long userId, Long vehicleId);
    void setDefault(Long userId, Long vehicleId);
}
