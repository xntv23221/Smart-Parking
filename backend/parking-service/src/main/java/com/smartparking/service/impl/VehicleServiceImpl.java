package com.smartparking.service.impl;

import com.smartparking.common.error.BusinessException;
import com.smartparking.common.error.ErrorCode;
import com.smartparking.dao.mapper.VehicleMapper;
import com.smartparking.domain.model.Vehicle;
import com.smartparking.service.VehicleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class VehicleServiceImpl implements VehicleService {

    private final VehicleMapper vehicleMapper;

    public VehicleServiceImpl(VehicleMapper vehicleMapper) {
        this.vehicleMapper = vehicleMapper;
    }

    @Override
    public List<Vehicle> getMyVehicles(Long userId) {
        return vehicleMapper.selectByUserId(userId);
    }

    @Override
    @Transactional
    public void addVehicle(Long userId, Vehicle vehicle) {
        if (vehicleMapper.selectByPlateNumber(vehicle.getPlateNumber()) != null) {
            throw new BusinessException(ErrorCode.CONFLICT, "Plate number already exists");
        }
        vehicle.setUserId(userId);
        vehicle.setCreatedAt(LocalDateTime.now());
        if (Boolean.TRUE.equals(vehicle.getIsDefault())) {
            clearDefault(userId);
        }
        vehicleMapper.insert(vehicle);
    }

    @Override
    @Transactional
    public void removeVehicle(Long userId, Long vehicleId) {
        Vehicle v = vehicleMapper.selectById(vehicleId);
        if (v != null && v.getUserId().equals(userId)) {
            vehicleMapper.deleteById(vehicleId);
        }
    }

    @Override
    @Transactional
    public void setDefault(Long userId, Long vehicleId) {
        Vehicle v = vehicleMapper.selectById(vehicleId);
        if (v == null || !v.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Vehicle not found");
        }
        clearDefault(userId);
        v.setIsDefault(true);
        vehicleMapper.update(v);
    }

    private void clearDefault(Long userId) {
        List<Vehicle> vehicles = vehicleMapper.selectByUserId(userId);
        for (Vehicle v : vehicles) {
            if (Boolean.TRUE.equals(v.getIsDefault())) {
                v.setIsDefault(false);
                vehicleMapper.update(v);
            }
        }
    }
}
