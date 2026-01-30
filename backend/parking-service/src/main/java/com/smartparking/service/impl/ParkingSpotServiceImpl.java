package com.smartparking.service.impl;

import com.smartparking.dao.mapper.ParkingSpotMapper;
import com.smartparking.domain.model.ParkingSpot;
import com.smartparking.service.ParkingSpotService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ParkingSpotServiceImpl implements ParkingSpotService {

    private final ParkingSpotMapper spotMapper;

    public ParkingSpotServiceImpl(ParkingSpotMapper spotMapper) {
        this.spotMapper = spotMapper;
    }

    @Override
    public List<ParkingSpot> getSpotsByLot(Long lotId) {
        return spotMapper.selectByLotId(lotId);
    }

    @Override
    public ParkingSpot getSpot(Long lotId, String spotNumber) {
        return spotMapper.selectByLotIdAndSpotNumber(lotId, spotNumber);
    }

    @Override
    public boolean isAvailable(Long spotId) {
        ParkingSpot spot = spotMapper.selectById(spotId);
        return spot != null && !Boolean.TRUE.equals(spot.getIsOccupied());
    }
}
