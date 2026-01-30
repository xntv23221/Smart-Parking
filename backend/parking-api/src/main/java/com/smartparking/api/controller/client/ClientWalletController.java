package com.smartparking.api.controller.client;

import com.smartparking.common.api.Result;
import com.smartparking.common.security.UserContextHolder;
import com.smartparking.domain.model.UserWallet;
import com.smartparking.domain.model.WalletLog;
import com.smartparking.service.WalletService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/client/v1/wallet")
public class ClientWalletController {

    private final WalletService walletService;

    public ClientWalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping
    public Result<UserWallet> get() {
        Long userId = UserContextHolder.get().getUserId();
        return Result.ok(walletService.getMyWallet(userId));
    }

    @PostMapping("/recharge")
    public Result<UserWallet> recharge(@RequestBody Map<String, BigDecimal> params) {
        Long userId = UserContextHolder.get().getUserId();
        BigDecimal amount = params.get("amount");
        return Result.ok(walletService.recharge(userId, amount));
    }

    @PostMapping("/withdraw")
    public Result<UserWallet> withdraw(@RequestBody Map<String, Object> params) {
        Long userId = UserContextHolder.get().getUserId();
        BigDecimal amount = new BigDecimal(params.get("amount").toString());
        String accountInfo = (String) params.get("accountInfo");
        if (accountInfo == null || accountInfo.trim().isEmpty()) {
            throw new IllegalArgumentException("提现账户信息不能为空");
        }
        return Result.ok(walletService.withdraw(userId, amount, accountInfo));
    }

    @GetMapping("/history")
    public Result<List<WalletLog>> history() {
        Long userId = UserContextHolder.get().getUserId();
        return Result.ok(walletService.getHistory(userId));
    }
}
