package com.smartparking.service.impl;

import com.smartparking.dao.mapper.ParkingLotMapper;
import com.smartparking.domain.model.ParkingLot;
import com.smartparking.service.ParkingLotService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ParkingLotServiceImpl implements ParkingLotService {

    private final ParkingLotMapper parkingLotMapper;

    public ParkingLotServiceImpl(ParkingLotMapper parkingLotMapper) {
        this.parkingLotMapper = parkingLotMapper;
    }

    @Override
    public List<ParkingLot> getAllParkingLots() {
        return parkingLotMapper.selectAll();
    }

    @Override
    public List<ParkingLot> getParkingLotsByManager(Long managerId) {
        return parkingLotMapper.selectByManagerId(managerId);
    }

    @Override
    public void updateParkingLot(ParkingLot parkingLot) {
        parkingLotMapper.update(parkingLot);
    }

    @Override
    public ParkingLot getParkingLotById(Long lotId) {
        return parkingLotMapper.selectById(lotId);
    }

    @Override
    public ParkingLot getParkingLotByName(String name) {
        return parkingLotMapper.selectByName(name);
    }

    @Override
    public void createParkingLot(ParkingLot parkingLot) {
        parkingLotMapper.insert(parkingLot);
    }
}
