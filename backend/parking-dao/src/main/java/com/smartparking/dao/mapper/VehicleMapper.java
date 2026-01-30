package com.smartparking.dao.mapper;

import com.smartparking.domain.model.Vehicle;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface VehicleMapper extends CrudMapper<Vehicle, Long> {
    List<Vehicle> selectByUserId(@Param("userId") Long userId);
    Vehicle selectByPlateNumber(@Param("plateNumber") String plateNumber);
}
