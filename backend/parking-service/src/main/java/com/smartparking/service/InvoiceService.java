package com.smartparking.service;

import com.smartparking.domain.model.Invoice;
import java.math.BigDecimal;
import java.util.List;

public interface InvoiceService {
    List<Invoice> getMyInvoices(Long userId);
    Invoice applyInvoice(Long userId, BigDecimal amount, String title, String taxNo);
}
