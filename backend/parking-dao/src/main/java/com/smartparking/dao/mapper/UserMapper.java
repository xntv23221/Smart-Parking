package com.smartparking.dao.mapper;

import com.smartparking.domain.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper extends CrudMapper<User, Long> {
    User selectByUsername(@Param("username") String username);
    User selectByPhone(@Param("phone") String phone);
}
