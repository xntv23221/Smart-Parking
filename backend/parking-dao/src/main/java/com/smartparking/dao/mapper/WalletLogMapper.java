package com.smartparking.dao.mapper;

import com.smartparking.domain.model.WalletLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface WalletLogMapper extends CrudMapper<WalletLog, Long> {
    List<WalletLog> selectByWalletId(@Param("walletId") Long walletId);
}
