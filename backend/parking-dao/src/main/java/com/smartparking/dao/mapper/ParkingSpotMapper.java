package com.smartparking.dao.mapper;

import com.smartparking.domain.model.ParkingSpot;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ParkingSpotMapper extends CrudMapper<ParkingSpot, Long> {
    List<ParkingSpot> selectByLotId(@Param("lotId") Long lotId);
    ParkingSpot selectByLotIdAndSpotNumber(@Param("lotId") Long lotId, @Param("spotNumber") String spotNumber);
    ParkingSpot selectFirstAvailable(@Param("lotId") Long lotId);
    int countAvailableByLotId(@Param("lotId") Long lotId);
}
