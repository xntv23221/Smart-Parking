package com.smartparking.service.impl;

import com.smartparking.common.error.BusinessException;
import com.smartparking.common.error.ErrorCode;
import com.smartparking.dao.mapper.UserWalletMapper;
import com.smartparking.dao.mapper.WalletLogMapper;
import com.smartparking.domain.model.UserWallet;
import com.smartparking.domain.model.WalletLog;
import com.smartparking.service.WalletService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class WalletServiceImpl implements WalletService {

    private final UserWalletMapper walletMapper;
    private final WalletLogMapper logMapper;

    public WalletServiceImpl(UserWalletMapper walletMapper, WalletLogMapper logMapper) {
        this.walletMapper = walletMapper;
        this.logMapper = logMapper;
    }

    @Override
    public UserWallet getMyWallet(Long userId) {
        UserWallet wallet = walletMapper.selectByUserId(userId);
        if (wallet == null) {
            wallet = new UserWallet();
            wallet.setUserId(userId);
            wallet.setBalance(BigDecimal.ZERO);
            wallet.setVersion(0);
            wallet.setUpdatedAt(LocalDateTime.now());
            walletMapper.insert(wallet);
        }
        return wallet;
    }

    @Override
    @Transactional
    public UserWallet recharge(Long userId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Invalid amount");
        }
        UserWallet wallet = getMyWallet(userId);
        
        int rows = walletMapper.updateBalance(wallet.getWalletId(), amount, wallet.getVersion());
        if (rows == 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "Concurrent update error");
        }
        
        wallet = walletMapper.selectById(wallet.getWalletId());
        
        WalletLog log = new WalletLog();
        log.setWalletId(wallet.getWalletId());
        log.setAmount(amount);
        log.setType(1); // Recharge
        log.setBalanceAfter(wallet.getBalance());
        log.setRemark("充值");
        log.setCreatedAt(LocalDateTime.now());
        logMapper.insert(log);
        
        return wallet;
    }

    @Override
    @Transactional
    public UserWallet withdraw(Long userId, BigDecimal amount, String accountInfo) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "提现金额必须大于0");
        }
        UserWallet wallet = getMyWallet(userId);
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new BusinessException(ErrorCode.PAYMENT_REQUIRED, "余额不足");
        }
        
        // 扣除余额
        int rows = walletMapper.updateBalance(wallet.getWalletId(), amount.negate(), wallet.getVersion());
        if (rows == 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "并发更新失败，请重试");
        }
        
        wallet = walletMapper.selectById(wallet.getWalletId());
        
        WalletLog log = new WalletLog();
        log.setWalletId(wallet.getWalletId());
        log.setAmount(amount.negate());
        log.setType(4); // Withdraw
        log.setBalanceAfter(wallet.getBalance());
        log.setRemark("提现到: " + accountInfo);
        log.setCreatedAt(LocalDateTime.now());
        logMapper.insert(log);
        
        return wallet;
    }

    @Override
    @Transactional
    public void pay(Long userId, BigDecimal amount, Long orderId) {
        UserWallet wallet = getMyWallet(userId);
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new BusinessException(ErrorCode.PAYMENT_REQUIRED, "Insufficient balance");
        }
        
        int rows = walletMapper.updateBalance(wallet.getWalletId(), amount.negate(), wallet.getVersion());
        if (rows == 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "Concurrent update error");
        }
        
        wallet = walletMapper.selectById(wallet.getWalletId());
        
        WalletLog log = new WalletLog();
        log.setWalletId(wallet.getWalletId());
        log.setAmount(amount.negate());
        log.setType(2); // Payment
        log.setBalanceAfter(wallet.getBalance());
        log.setOrderId(orderId);
        log.setRemark("停车费支付");
        log.setCreatedAt(LocalDateTime.now());
        logMapper.insert(log);
    }

    @Override
    public List<WalletLog> getHistory(Long userId) {
        UserWallet wallet = getMyWallet(userId);
        return logMapper.selectByWalletId(wallet.getWalletId());
    }
}
