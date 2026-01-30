package com.smartparking.dao.mapper;

import com.smartparking.domain.model.PricingRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface PricingRuleMapper extends CrudMapper<PricingRule, Long> {
    List<PricingRule> selectByLotId(@Param("lotId") Long lotId);
    PricingRule selectActiveRule(@Param("lotId") Long lotId, @Param("vehicleType") Integer vehicleType);
}
