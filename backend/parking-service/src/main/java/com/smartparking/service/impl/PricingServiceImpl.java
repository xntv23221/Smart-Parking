package com.smartparking.service.impl;

import com.smartparking.dao.mapper.PricingRuleMapper;
import com.smartparking.domain.model.PricingRule;
import com.smartparking.service.PricingService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PricingServiceImpl implements PricingService {

    private final PricingRuleMapper ruleMapper;

    public PricingServiceImpl(PricingRuleMapper ruleMapper) {
        this.ruleMapper = ruleMapper;
    }

    @Override
    public BigDecimal calculateFee(Long lotId, Integer vehicleType, long durationMinutes) {
        PricingRule rule = ruleMapper.selectActiveRule(lotId, vehicleType);
        if (rule == null) {
            // Default fallback
            return new BigDecimal(durationMinutes).divide(new BigDecimal(60), 2, java.math.RoundingMode.CEILING).multiply(new BigDecimal("10"));
        }

        if (durationMinutes <= rule.getBaseDuration()) {
            return BigDecimal.ZERO;
        }

        BigDecimal fee = rule.getBasePrice();
        long remainingMinutes = durationMinutes - 60; // Assume base price covers 1st hour
        
        if (remainingMinutes > 0) {
            BigDecimal extraHours = new BigDecimal(remainingMinutes).divide(new BigDecimal(60), 2, java.math.RoundingMode.CEILING);
            fee = fee.add(extraHours.multiply(rule.getExtraPricePerHour()));
        }

        if (rule.getDailyCap() != null && fee.compareTo(rule.getDailyCap()) > 0) {
            fee = rule.getDailyCap();
        }

        return fee;
    }

    @Override
    public void addRule(PricingRule rule) {
        ruleMapper.insert(rule);
    }
}
