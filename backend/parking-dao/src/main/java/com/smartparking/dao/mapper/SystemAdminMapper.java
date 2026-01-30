package com.smartparking.dao.mapper;

import com.smartparking.domain.model.SystemAdmin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SystemAdminMapper extends CrudMapper<SystemAdmin, Long> {
    SystemAdmin selectByUsername(@Param("username") String username);
}
