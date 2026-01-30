package com.smartparking.dao.mapper;

import com.smartparking.domain.model.Manager;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ManagerMapper extends CrudMapper<Manager, Long> {
    Manager selectByUsername(@Param("username") String username);
}
