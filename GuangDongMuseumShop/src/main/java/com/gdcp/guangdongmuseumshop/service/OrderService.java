package com.gdcp.guangdongmuseumshop.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.gdcp.guangdongmuseumshop.dto.OrderDTO;
import com.gdcp.guangdongmuseumshop.dto.CreateOrderRequest;
import com.gdcp.guangdongmuseumshop.dto.OrderSearchDTO;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public interface OrderService {
    Page<OrderDTO> getOrders(OrderSearchDTO searchDTO, Pageable pageable);
    OrderDTO createOrder(String username, CreateOrderRequest request);
    void payOrder(Long orderId);
    Page<OrderDTO> searchOrders(OrderSearchDTO searchDTO, Pageable pageable);
    OrderDTO getOrderDetail(String orderNo);
    List<OrderDTO> getUserOrders(String username, String status);
    void cancelOrder(Long orderId);
    void shipOrder(Long orderId, String shippingCompany, String trackingNo);
    void confirmReceive(Long orderId, String username);
    OrderDTO getOrderById(Long id);
} 