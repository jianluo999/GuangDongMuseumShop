package com.gdcp.guangdongmuseumshop.service.impl;

import com.gdcp.guangdongmuseumshop.dto.PaymentDTO;
import com.gdcp.guangdongmuseumshop.dto.RefundRequestDTO;
import com.gdcp.guangdongmuseumshop.enums.MessageType;
import com.gdcp.guangdongmuseumshop.enums.OrderStatus;
import com.gdcp.guangdongmuseumshop.enums.PaymentStatus;
import com.gdcp.guangdongmuseumshop.exception.OrderException;
import com.gdcp.guangdongmuseumshop.entity.Order;
import com.gdcp.guangdongmuseumshop.entity.Payment;
import com.gdcp.guangdongmuseumshop.repository.OrderRepository;
import com.gdcp.guangdongmuseumshop.repository.PaymentRepository;
import com.gdcp.guangdongmuseumshop.service.MessageService;
import com.gdcp.guangdongmuseumshop.service.PaymentService;
import com.gdcp.guangdongmuseumshop.vo.PaymentQRCodeVO;
import com.gdcp.guangdongmuseumshop.vo.PaymentStatusVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final MessageService messageService;

    @Override
    @Transactional
    public PaymentDTO createPayment(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException("订单不存在"));

        // 检查是否已存在支付记录
        if (paymentRepository.findByOrderId(orderId).isPresent()) {
            throw new OrderException("该订单已存在支付记录");
        }

        Payment payment = new Payment();
        payment.setPaymentNo(generatePaymentNo());
        payment.setOrder(order);
        payment.setAmount(order.getTotalAmount());
        payment.setStatus(PaymentStatus.PENDING);

        payment = paymentRepository.save(payment);
        return convertToDTO(payment);
    }

    @Override
    @Transactional
    public PaymentDTO processPayment(String paymentNo) {
        Payment payment = paymentRepository.findByPaymentNo(paymentNo)
                .orElseThrow(() -> new OrderException("支付记录不存在"));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new OrderException("支付状态异常");
        }

        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidTime(LocalDateTime.now());
        payment = paymentRepository.save(payment);

        // 发送支付成功消息
        messageService.createOrderMessage(
                payment.getOrder().getUser().getId(),
                payment.getOrder().getId(),
                String.format("订单 %s 支付成功", payment.getOrder().getOrderNo())
        );

        return convertToDTO(payment);
    }

    @Override
    public PaymentDTO getPaymentByOrderId(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new OrderException("支付记录不存在"));
        return convertToDTO(payment);
    }

    @Override
    @Transactional
    public PaymentDTO requestRefund(Long userId, RefundRequestDTO refundRequest) {
        Payment payment = paymentRepository.findByOrderIdAndUserId(refundRequest.getOrderId(), userId)
                .orElseThrow(() -> new OrderException("支付记录不存在"));

        if (payment.getStatus() != PaymentStatus.PAID) {
            throw new OrderException("当前支付状态不支持退款");
        }

        payment.setStatus(PaymentStatus.REFUND_PENDING);
        payment.setRefundReason(refundRequest.getReason());
        payment = paymentRepository.save(payment);

        // 发送退款申请消息
        messageService.createOrderMessage(
                payment.getOrder().getUser().getId(),
                payment.getOrder().getId(),
                String.format("订单 %s 的退款申请已提交，请等待处理", payment.getOrder().getOrderNo())
        );

        return convertToDTO(payment);
    }

    @Override
    @Transactional
    public PaymentDTO processRefund(String paymentNo) {
        Payment payment = paymentRepository.findByPaymentNo(paymentNo)
                .orElseThrow(() -> new OrderException("支付记录不存在"));

        if (payment.getStatus() != PaymentStatus.REFUND_PENDING) {
            throw new OrderException("当前支付状态不支持退款处理");
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setRefundTime(LocalDateTime.now());
        payment = paymentRepository.save(payment);

        // 发送退款成功消息
        messageService.createOrderMessage(
                payment.getOrder().getUser().getId(),
                payment.getOrder().getId(),
                String.format("订单 %s 已退款成功", payment.getOrder().getOrderNo())
        );

        return convertToDTO(payment);
    }

    @Override
    @Transactional
    public void cancelPayment(String paymentNo) {
        Payment payment = paymentRepository.findByPaymentNo(paymentNo)
                .orElseThrow(() -> new OrderException("支付记录不存在"));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new OrderException("当前支付状态不支持取消");
        }

        payment.setStatus(PaymentStatus.CANCELLED);
        paymentRepository.save(payment);
    }

    @Override
    public PaymentDTO getPaymentByPaymentNo(String paymentNo) {
        Payment payment = paymentRepository.findByPaymentNo(paymentNo)
                .orElseThrow(() -> new OrderException("支付记录不存在"));
        return convertToDTO(payment);
    }

    @Override
    public PaymentQRCodeVO generatePaymentQRCode(Long orderId, String paymentMethod) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException("订单不存在"));

        // 这里应该调用实际的支付平台API生成二维码
        // 这里仅作演示，返回模拟数据
        PaymentQRCodeVO qrCodeVO = new PaymentQRCodeVO();
        qrCodeVO.setQrCodeUrl("https://example.com/mock-qr-code.png");
        qrCodeVO.setPaymentMethod(paymentMethod);
        qrCodeVO.setOrderId(orderId);
        qrCodeVO.setAmount(order.getTotalAmount());
        qrCodeVO.setExpireTime(System.currentTimeMillis() + 30 * 60 * 1000); // 30分钟后过期

        return qrCodeVO;
    }

    @Override
    public PaymentStatusVO checkPaymentStatus(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException("订单不存在"));

        // 这里应该调用实际的支付平台API查询支付状态
        // 这里仅作演示，返回模拟数据
        PaymentStatusVO statusVO = new PaymentStatusVO();
        statusVO.setStatus("PENDING");
        statusVO.setMessage("等待支付");
        statusVO.setOrderId(orderId);
        statusVO.setAmount(order.getTotalAmount());
        // 由于Order实体中可能没有paymentMethod字段，这里使用一个默认值
        statusVO.setPaymentMethod("UNKNOWN");
        statusVO.setPaymentTime(null);

        return statusVO;
    }

    @Override
    @Transactional
    public void confirmPayment(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException("订单不存在"));

        // 这里应该验证支付平台的支付结果
        // 这里仅作演示，直接更新订单状态
        order.setStatus(OrderStatus.PAID);
        order.setPaymentTime(LocalDateTime.now());
        orderRepository.save(order);
    }

    @Override
    @Transactional
    public void cancelPayment(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException("订单不存在"));

        // 这里应该调用支付平台的取消接口
        // 这里仅作演示，直接更新订单状态
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    private String generatePaymentNo() {
        return "PAY" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    private PaymentDTO convertToDTO(Payment payment) {
        PaymentDTO dto = new PaymentDTO();
        dto.setId(payment.getId());
        dto.setPaymentNo(payment.getPaymentNo());
        dto.setOrderId(payment.getOrder().getId());
        dto.setAmount(payment.getAmount());
        dto.setStatus(payment.getStatus());
        dto.setRefundReason(payment.getRefundReason());
        dto.setPaidTime(payment.getPaidTime());
        dto.setRefundTime(payment.getRefundTime());
        dto.setCreatedAt(payment.getCreatedAt());
        return dto;
    }
} 