package com.smartparking.service;

import com.smartparking.domain.model.MonthlyCard;
import java.util.List;

public interface MonthlyCardService {
    List<MonthlyCard> getMyCards(Long userId);
    MonthlyCard buyCard(Long userId, Long lotId, String plateNumber, int months);
}
