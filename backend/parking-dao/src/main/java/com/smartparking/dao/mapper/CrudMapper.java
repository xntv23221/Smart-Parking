package com.smartparking.dao.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * A conventional CRUD contract for MyBatis mappers.
 *
 * <p>Concrete mappers can extend this interface and add domain-specific queries.</p>
 */
public interface CrudMapper<T, ID> {
    T selectById(@Param("id") ID id);

    int insert(T entity);

    int update(T entity);

    int deleteById(@Param("id") ID id);

    List<T> selectPage(@Param("offset") int offset, @Param("limit") int limit);
}

