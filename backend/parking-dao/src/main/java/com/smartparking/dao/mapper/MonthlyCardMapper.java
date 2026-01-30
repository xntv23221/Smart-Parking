package com.smartparking.dao.mapper;

import com.smartparking.domain.model.MonthlyCard;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface MonthlyCardMapper extends CrudMapper<MonthlyCard, Long> {
    List<MonthlyCard> selectByUserId(@Param("userId") Long userId);
    MonthlyCard selectActiveByPlate(@Param("plateNumber") String plateNumber, @Param("lotId") Long lotId);
}
