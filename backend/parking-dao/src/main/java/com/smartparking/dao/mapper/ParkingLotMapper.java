package com.smartparking.dao.mapper;

import com.smartparking.domain.model.ParkingLot;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ParkingLotMapper extends CrudMapper<ParkingLot, Long> {
    List<ParkingLot> selectAll();
    List<ParkingLot> selectByManagerId(@Param("managerId") Long managerId);
    ParkingLot selectByName(@Param("name") String name);
}
