package com.smartparking.domain.model;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PricingRule {
    private Long ruleId;
    private Long lotId;
    private Integer vehicleType;
    private Integer baseDuration;
    private BigDecimal basePrice;
    private BigDecimal extraPricePerHour;
    private BigDecimal dailyCap;
    private LocalDate effectiveDate;
    private Boolean isActive;
}
