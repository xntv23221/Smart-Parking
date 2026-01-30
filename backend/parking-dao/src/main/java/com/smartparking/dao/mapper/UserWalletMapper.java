package com.smartparking.dao.mapper;

import com.smartparking.domain.model.UserWallet;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserWalletMapper extends CrudMapper<UserWallet, Long> {
    UserWallet selectByUserId(@Param("userId") Long userId);
    int updateBalance(@Param("walletId") Long walletId, @Param("amount") java.math.BigDecimal amount, @Param("version") Integer version);
}
