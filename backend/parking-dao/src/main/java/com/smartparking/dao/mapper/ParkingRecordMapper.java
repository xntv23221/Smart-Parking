package com.smartparking.dao.mapper;

import com.smartparking.domain.model.ParkingRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ParkingRecordMapper extends CrudMapper<ParkingRecord, Long> {
    List<ParkingRecord> selectByUserId(@Param("userId") Long userId);
    List<ParkingRecord> selectByLotId(@Param("lotId") Long lotId);
    ParkingRecord selectActiveRecord(@Param("vehicleId") Long vehicleId);
}
