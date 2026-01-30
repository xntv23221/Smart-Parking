package com.smartparking.service;

import com.smartparking.domain.model.UserWallet;
import com.smartparking.domain.model.WalletLog;
import java.math.BigDecimal;
import java.util.List;

public interface WalletService {
    UserWallet getMyWallet(Long userId);
    UserWallet recharge(Long userId, BigDecimal amount);
    UserWallet withdraw(Long userId, BigDecimal amount, String accountInfo);
    void pay(Long userId, BigDecimal amount, Long orderId);
    List<WalletLog> getHistory(Long userId);
}
