package com.smartparking.dao.mapper;

import com.smartparking.domain.model.AuditRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface AuditRequestMapper extends CrudMapper<AuditRequest, Long> {
    List<AuditRequest> selectByUserId(@Param("userId") Long userId);
    List<AuditRequest> selectByStatus(@Param("status") Integer status);
}
