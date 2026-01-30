package com.smartparking.service.impl;

import com.smartparking.dao.mapper.InvoiceMapper;
import com.smartparking.domain.model.Invoice;
import com.smartparking.service.InvoiceService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class InvoiceServiceImpl implements InvoiceService {
    private final InvoiceMapper invoiceMapper;

    public InvoiceServiceImpl(InvoiceMapper invoiceMapper) {
        this.invoiceMapper = invoiceMapper;
    }

    @Override
    public List<Invoice> getMyInvoices(Long userId) {
        return invoiceMapper.selectByUserId(userId);
    }

    @Override
    public Invoice applyInvoice(Long userId, BigDecimal amount, String title, String taxNo) {
        Invoice invoice = new Invoice();
        invoice.setUserId(userId);
        invoice.setAmount(amount);
        invoice.setTitle(title);
        invoice.setTaxNo(taxNo);
        invoice.setStatus(0); // Pending
        invoice.setCreatedAt(LocalDateTime.now());
        invoiceMapper.insert(invoice);
        return invoice;
    }
}
