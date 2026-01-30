package com.smartparking.api.controller.client;

import com.smartparking.common.api.Result;
import com.smartparking.common.security.UserContextHolder;
import com.smartparking.domain.model.MonthlyCard;
import com.smartparking.service.MonthlyCardService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/client/v1/monthly-cards")
public class ClientMonthlyCardController {

    private final MonthlyCardService cardService;

    public ClientMonthlyCardController(MonthlyCardService cardService) {
        this.cardService = cardService;
    }

    @GetMapping
    public Result<List<MonthlyCard>> list() {
        Long userId = UserContextHolder.get().getUserId();
        return Result.ok(cardService.getMyCards(userId));
    }

    @PostMapping
    public Result<MonthlyCard> buy(@RequestBody Map<String, Object> params) {
        Long userId = UserContextHolder.get().getUserId();
        Long lotId = Long.valueOf(params.get("lotId").toString());
        String plateNumber = (String) params.get("plateNumber");
        int months = Integer.parseInt(params.get("months").toString());
        return Result.ok(cardService.buyCard(userId, lotId, plateNumber, months));
    }
}
