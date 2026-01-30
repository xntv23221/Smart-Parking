package com.smartparking.dao.mapper;

import com.smartparking.domain.model.Invoice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface InvoiceMapper extends CrudMapper<Invoice, Long> {
    List<Invoice> selectByUserId(@Param("userId") Long userId);
}
