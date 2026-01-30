package com.smartparking.service.impl;

import com.smartparking.dao.mapper.MonthlyCardMapper;
import com.smartparking.domain.model.MonthlyCard;
import com.smartparking.service.MonthlyCardService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MonthlyCardServiceImpl implements MonthlyCardService {
    private final MonthlyCardMapper cardMapper;

    public MonthlyCardServiceImpl(MonthlyCardMapper cardMapper) {
        this.cardMapper = cardMapper;
    }

    @Override
    public List<MonthlyCard> getMyCards(Long userId) {
        return cardMapper.selectByUserId(userId);
    }

    @Override
    public MonthlyCard buyCard(Long userId, Long lotId, String plateNumber, int months) {
        MonthlyCard card = new MonthlyCard();
        card.setUserId(userId);
        card.setLotId(lotId);
        card.setStartDate(LocalDate.now());
        card.setEndDate(LocalDate.now().plusMonths(months));
        card.setPlateNumber(plateNumber);
        card.setStatus(1); // Active
        card.setCreatedAt(LocalDateTime.now());
        cardMapper.insert(card);
        return card;
    }
}
