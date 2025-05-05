package com.gdcp.guangdongmuseumshop.service;

import com.gdcp.guangdongmuseumshop.dto.PaymentDTO;
import com.gdcp.guangdongmuseumshop.dto.RefundRequestDTO;
import com.gdcp.guangdongmuseumshop.vo.PaymentQRCodeVO;
import com.gdcp.guangdongmuseumshop.vo.PaymentStatusVO;

public interface PaymentService {
    PaymentDTO createPayment(Long orderId);
    PaymentDTO processPayment(String paymentNo);
    PaymentDTO getPaymentByOrderId(Long orderId);
    PaymentDTO requestRefund(Long userId, RefundRequestDTO refundRequest);
    PaymentDTO processRefund(String paymentNo);
    void cancelPayment(String paymentNo);
    PaymentDTO getPaymentByPaymentNo(String paymentNo);
    PaymentQRCodeVO generatePaymentQRCode(Long orderId, String paymentMethod);
    PaymentStatusVO checkPaymentStatus(Long orderId);
    void confirmPayment(Long orderId);
    void cancelPayment(Long orderId);
} 