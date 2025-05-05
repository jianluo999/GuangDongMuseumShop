package com.gdcp.guangdongmuseumshop.controller;

import com.gdcp.guangdongmuseumshop.common.Result;
import com.gdcp.guangdongmuseumshop.dto.PaymentDTO;
import com.gdcp.guangdongmuseumshop.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/payments")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminPaymentController {

    private final PaymentService paymentService;

    @PostMapping("/{paymentNo}/refund")
    public Result<PaymentDTO> processRefund(@PathVariable String paymentNo) {
        return Result.success(paymentService.processRefund(paymentNo));
    }

    @GetMapping("/{paymentNo}")
    public Result<PaymentDTO> getPaymentDetails(@PathVariable String paymentNo) {
        return Result.success(paymentService.getPaymentByPaymentNo(paymentNo));
    }
} 