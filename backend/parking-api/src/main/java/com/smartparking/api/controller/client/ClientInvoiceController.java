package com.smartparking.api.controller.client;

import com.smartparking.common.api.Result;
import com.smartparking.common.security.UserContextHolder;
import com.smartparking.domain.model.Invoice;
import com.smartparking.service.InvoiceService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/client/v1/invoices")
public class ClientInvoiceController {

    private final InvoiceService invoiceService;

    public ClientInvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @GetMapping
    public Result<List<Invoice>> list() {
        Long userId = UserContextHolder.get().getUserId();
        return Result.ok(invoiceService.getMyInvoices(userId));
    }

    @PostMapping
    public Result<Invoice> apply(@RequestBody Map<String, Object> params) {
        Long userId = UserContextHolder.get().getUserId();
        BigDecimal amount = new BigDecimal(params.get("amount").toString());
        String title = (String) params.get("title");
        String taxNo = (String) params.get("taxNo");
        return Result.ok(invoiceService.applyInvoice(userId, amount, title, taxNo));
    }
}
