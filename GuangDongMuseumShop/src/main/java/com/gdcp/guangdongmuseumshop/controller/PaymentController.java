package com.gdcp.guangdongmuseumshop.controller;

import com.gdcp.guangdongmuseumshop.common.Result;
import com.gdcp.guangdongmuseumshop.dto.PaymentDTO;
import com.gdcp.guangdongmuseumshop.dto.RefundRequestDTO;
import com.gdcp.guangdongmuseumshop.security.SecurityUtils;
import com.gdcp.guangdongmuseumshop.service.PaymentService;
import com.gdcp.guangdongmuseumshop.vo.PaymentQRCodeVO;
import com.gdcp.guangdongmuseumshop.vo.PaymentStatusVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/orders/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public Result<PaymentDTO> createPayment(@PathVariable Long orderId) {
        return Result.success(paymentService.createPayment(orderId));
    }

    @GetMapping("/orders/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public Result<PaymentDTO> getPaymentByOrder(@PathVariable Long orderId) {
        return Result.success(paymentService.getPaymentByOrderId(orderId));
    }

    @PostMapping("/{paymentNo}/pay")
    @PreAuthorize("isAuthenticated()")
    public Result<PaymentDTO> processPayment(@PathVariable String paymentNo) {
        return Result.success(paymentService.processPayment(paymentNo));
    }

    @PostMapping("/refund")
    @PreAuthorize("isAuthenticated()")
    public Result<PaymentDTO> requestRefund(@Valid @RequestBody RefundRequestDTO refundRequest) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(paymentService.requestRefund(userId, refundRequest));
    }

    @DeleteMapping("/{paymentNo}")
    @PreAuthorize("isAuthenticated()")
    public Result<?> cancelPayment(@PathVariable String paymentNo) {
        paymentService.cancelPayment(paymentNo);
        return Result.success("支付已取消");
    }

    @PostMapping("/orders/{orderId}/qrcode")
    @PreAuthorize("isAuthenticated()")
    public Result<PaymentQRCodeVO> getPaymentQRCode(
            @PathVariable Long orderId,
            @RequestBody PaymentDTO paymentDTO) {
        PaymentQRCodeVO qrCode = paymentService.generatePaymentQRCode(orderId, paymentDTO.getPaymentMethod());
        return Result.success(qrCode);
    }

    @GetMapping("/orders/{orderId}/status")
    @PreAuthorize("isAuthenticated()")
    public Result<PaymentStatusVO> getPaymentStatus(@PathVariable Long orderId) {
        PaymentStatusVO status = paymentService.checkPaymentStatus(orderId);
        return Result.success(status);
    }

    @PostMapping("/orders/{orderId}/confirm")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> confirmPayment(@PathVariable Long orderId) {
        paymentService.confirmPayment(orderId);
        return Result.success(null);
    }

    @PostMapping("/orders/{orderId}/cancel")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> cancelPayment(@PathVariable Long orderId) {
        paymentService.cancelPayment(orderId);
        return Result.success(null);
    }
} 