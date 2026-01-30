package com.smartparking.service;

import com.smartparking.domain.model.PricingRule;
import java.math.BigDecimal;

public interface PricingService {
    BigDecimal calculateFee(Long lotId, Integer vehicleType, long durationMinutes);
    void addRule(PricingRule rule);
}
