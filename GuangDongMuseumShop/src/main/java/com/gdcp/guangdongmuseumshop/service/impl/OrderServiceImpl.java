package com.gdcp.guangdongmuseumshop.service.impl;

import com.gdcp.guangdongmuseumshop.dto.OrderItemDTO;
import com.gdcp.guangdongmuseumshop.repository.OrderItemRepository;
import com.gdcp.guangdongmuseumshop.repository.OrderRepository;
import com.gdcp.guangdongmuseumshop.repository.ProductRepository;
import com.gdcp.guangdongmuseumshop.repository.UserRepository;
import com.gdcp.guangdongmuseumshop.service.OrderService;
import com.gdcp.guangdongmuseumshop.dto.OrderDTO;
import com.gdcp.guangdongmuseumshop.dto.OrderSearchDTO;
import com.gdcp.guangdongmuseumshop.entity.Order;
import com.gdcp.guangdongmuseumshop.entity.OrderItem;
import com.gdcp.guangdongmuseumshop.entity.Product;
import com.gdcp.guangdongmuseumshop.entity.User;
import com.gdcp.guangdongmuseumshop.dto.CreateOrderRequest;
import com.gdcp.guangdongmuseumshop.dto.OrderItemRequest;
import com.gdcp.guangdongmuseumshop.enums.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.beans.BeanUtils;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import com.gdcp.guangdongmuseumshop.exception.ResourceNotFoundException;
import com.gdcp.guangdongmuseumshop.exception.BusinessException;
import org.springframework.util.StringUtils;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import com.gdcp.guangdongmuseumshop.mapper.OrderMapper;
import com.gdcp.guangdongmuseumshop.exception.OrderException;
import com.gdcp.guangdongmuseumshop.exception.UserException;
import java.math.BigDecimal;
import com.gdcp.guangdongmuseumshop.service.MessageService;
import com.gdcp.guangdongmuseumshop.service.CartService;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderMapper orderMapper;
    private final MessageService messageService;
    private final CartService cartService;

    @Override
    @Transactional(readOnly = true)
    public Page<OrderDTO> getOrders(OrderSearchDTO searchDTO, Pageable pageable) {
        log.info("执行订单查询");
        log.debug("查询条件: {}", searchDTO);
        
        // 构建查询条件
        Page<Order> orders = orderRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // 添加查询条件
            if (StringUtils.hasText(searchDTO.getOrderNo())) {
                log.debug("添加订单号查询条件: {}", searchDTO.getOrderNo());
                predicates.add(cb.like(root.get("orderNo"), "%" + searchDTO.getOrderNo() + "%"));
            }
            if (StringUtils.hasText(searchDTO.getUsername())) {
                log.debug("添加用户名查询条件: {}", searchDTO.getUsername());
                predicates.add(cb.like(root.get("user").get("username"), "%" + searchDTO.getUsername() + "%"));
            }
            if (searchDTO.getStatus() != null && StringUtils.hasText(searchDTO.getStatus())) {
                log.debug("添加状态查询条件: {}", searchDTO.getStatus());
                predicates.add(cb.equal(root.get("status"), OrderStatus.valueOf(searchDTO.getStatus())));
            }
            
            // 添加日期范围查询条件
            if (StringUtils.hasText(searchDTO.getStartTime()) && StringUtils.hasText(searchDTO.getEndTime())) {
                log.debug("添加日期范围查询条件: {} - {}", searchDTO.getStartTime(), searchDTO.getEndTime());
                try {
                    LocalDateTime startTime = LocalDateTime.parse(searchDTO.getStartTime(), DATE_TIME_FORMATTER);
                    LocalDateTime endTime = LocalDateTime.parse(searchDTO.getEndTime(), DATE_TIME_FORMATTER);
                    predicates.add(cb.between(root.get("createdAt"), startTime, endTime));
                } catch (Exception e) {
                    log.error("日期解析错误: ", e);
                }
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        }, pageable);

        log.info("查询到订单数量: {}", orders.getTotalElements());
        log.debug("订单数据: {}", orders.getContent());

        // 转换为 DTO
        return orders.map(order -> {
            OrderDTO dto = new OrderDTO();
            dto.setId(order.getId());
            dto.setOrderNumber(order.getOrderNo());
            
            // 优化用户信息设置，避免空指针
            User user = order.getUser();
            if (user != null) {
                dto.setUserId(user.getId());
                dto.setUsername(user.getUsername());
            } else {
                dto.setUsername("未知用户");
            }
            
            dto.setTotalAmount(BigDecimal.valueOf(order.getTotalAmount().doubleValue()));
            dto.setStatus(order.getStatus().name());
            dto.setCreatedAt(order.getCreatedAt());
            
            // 获取订单项
            List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
            List<OrderItemDTO> itemDTOs = items.stream()
                .map(item -> {
                    OrderItemDTO itemDTO = new OrderItemDTO();
                    itemDTO.setId(item.getId());
                    itemDTO.setProductId(item.getProduct().getId());
                    itemDTO.setProductName(item.getProductName());
                    itemDTO.setProductImage(item.getProduct().getImage());
                    itemDTO.setProductPrice(item.getPrice());
                    itemDTO.setQuantity(item.getQuantity());
                    itemDTO.setSubtotal(item.getPrice().multiply(new BigDecimal(item.getQuantity())));
                    return itemDTO;
                })
                .collect(Collectors.toList());
            
            dto.setOrderItems(itemDTOs);
            return dto;
        });
    }

    @Override
    @Transactional
    public OrderDTO createOrder(String username, CreateOrderRequest request) {
        log.info("=== 开始创建订单 ===");
        log.info("创建用户: {}", username);
        log.info("订单请求: {}", request);

        try {
            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
            log.info("查询到用户信息: {}", user);

            Order order = new Order();
            order.setUser(user);
            String orderNo = String.format("%s%04d%04d",
                System.currentTimeMillis(),
                user.getId() % 10000,
                (int)(Math.random() * 10000));
            order.setOrderNo(orderNo);
            log.info("生成订单号: {}", orderNo);
            
            order.setStatus(OrderStatus.PENDING);
            order.setShippingName(request.getShipping().getName());
            order.setShippingPhone(request.getShipping().getPhone());
            order.setShippingAddress(request.getShipping().getAddress());
            log.info("订单基本信息: {}", order);

            // 创建订单项并计算总金额
            List<OrderItem> orderItems = new ArrayList<>();
            BigDecimal totalAmount = BigDecimal.ZERO;
            
            for (OrderItemRequest itemRequest : request.getItems()) {
                log.info("处理订单项: {}", itemRequest);
                Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new RuntimeException("商品不存在"));
                log.info("查询到商品信息: {}", product);
                
                // 验证商品价格
                if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new RuntimeException("商品价格无效: " + product.getName());
                }
                
                // 验证商品库存
                if (product.getStock() < itemRequest.getQuantity()) {
                    throw new RuntimeException("商品库存不足: " + product.getName());
                }
                
                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(order);
                orderItem.setProduct(product);
                orderItem.setQuantity(itemRequest.getQuantity());
                orderItem.setPrice(product.getPrice());
                orderItem.setProductName(product.getName());
                orderItem.setProductImage(product.getMainImage());
                
                // 计算该订单项的小计金额并累加到总金额
                BigDecimal itemTotal = product.getPrice().multiply(new BigDecimal(itemRequest.getQuantity()));
                log.info("订单项金额计算 - 单价: {}, 数量: {}, 小计: {}", 
                    product.getPrice(), itemRequest.getQuantity(), itemTotal);
                totalAmount = totalAmount.add(itemTotal);
                
                orderItems.add(orderItem);
                
                // 更新商品库存
                product.setStock(product.getStock() - itemRequest.getQuantity());
                productRepository.save(product);
            }
            
            // 设置订单总金额
            order.setTotalAmount(totalAmount);
            order.setOrderItems(orderItems);
            log.info("订单总额计算完成 - 订单项数量: {}, 总金额: {}", orderItems.size(), totalAmount);
            
            order = orderRepository.save(order);
            log.info("订单保存成功: {}", order);
            
            // 清空购物车
            cartService.clearCart(user.getId());
            log.info("购物车已清空");
            
            return convertToDTO(order);
        } catch (Exception e) {
            log.error("=== 订单创建失败 ===");
            log.error("错误类型: {}", e.getClass().getName());
            log.error("错误信息: {}", e.getMessage());
            log.error("详细堆栈: ", e);
            throw new RuntimeException("创建订单失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void payOrder(Long orderId) {
        log.info("处理订单支付，订单ID: {}", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("订单不存在"));

        // 检查订单状态是否为待付款
        if (!OrderStatus.PENDING.equals(order.getStatus())) {
            throw new BusinessException("订单状态不正确，当前状态: " + order.getStatus());
        }

        // 更新订单状态为已支付
        order.setStatus(OrderStatus.PAID);
        order.setPaymentTime(LocalDateTime.now());
        orderRepository.save(order);
        
        // 发送订单状态变更消息
        messageService.createOrderStatusMessage(
            order.getUser().getId(),
            order.getId(),
            order.getOrderNo(),
            OrderStatus.PAID.name()
        );
    }

    @Override
    public Page<OrderDTO> searchOrders(OrderSearchDTO searchDTO, Pageable pageable) {
        return orderRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (StringUtils.hasText(searchDTO.getOrderNo())) {
                predicates.add(cb.like(root.get("orderNo"), "%" + searchDTO.getOrderNo() + "%"));
            }
            if (StringUtils.hasText(searchDTO.getStatus())) {
                predicates.add(cb.equal(root.get("status"), searchDTO.getStatus()));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        }, pageable).map(orderMapper::toDTO);
    }

    @Override
    public OrderDTO getOrderDetail(String orderNo) {
        Order order = orderRepository.findByOrderNo(orderNo)
            .orElseThrow(() -> new OrderException("订单不存在"));
        return convertToDTO(order);
    }

    @Override
    public List<OrderDTO> getUserOrders(String username, String status) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UserException("用户不存在"));
        List<Order> orders;
        
        if (StringUtils.hasText(status)) {
            orders = orderRepository.findByUserIdAndStatusWithItems(
                user.getId(), 
                OrderStatus.valueOf(status)
            );
        } else {
            orders = orderRepository.findByUserIdWithItems(user.getId());
        }
        
        return orders.stream()
            .map(orderMapper::toDTO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId) {
        log.info("处理订单取消，订单ID: {}", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("订单不存在"));

        // 检查订单状态是否为待付款
        if (!OrderStatus.PENDING.equals(order.getStatus())) {
            throw new BusinessException("只能取消待付款的订单，当前状态: " + order.getStatus());
        }

        // 更新订单状态为已取消
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        log.info("订单已取消: {}", order.getOrderNo());
        
        // 发送订单状态变更消息
        messageService.createOrderStatusMessage(
            order.getUser().getId(),
            order.getId(),
            order.getOrderNo(),
            OrderStatus.CANCELLED.name()
        );
    }

    @Override
    @Transactional
    public void shipOrder(Long orderId, String shippingCompany, String trackingNo) {
        log.info("处理订单发货，订单ID: {}, 物流公司: {}, 物流单号: {}", orderId, shippingCompany, trackingNo);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("订单不存在"));

        if (!OrderStatus.PAID.equals(order.getStatus())) {
            throw new BusinessException("只能发货已支付的订单，当前状态: " + order.getStatus());
        }

        order.setStatus(OrderStatus.SHIPPED);
        order.setShippingTime(LocalDateTime.now());
        order.setShippingCompany(shippingCompany);
        order.setTrackingNo(trackingNo);
        orderRepository.save(order);
        log.info("订单已发货: {}", order.getOrderNo());
        
        // 发送订单状态变更消息
        messageService.createOrderStatusMessage(
            order.getUser().getId(),
            order.getId(),
            order.getOrderNo(),
            OrderStatus.SHIPPED.name()
        );
    }

    @Override
    @Transactional
    public void confirmReceive(Long orderId, String username) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("订单不存在"));
            
        // 验证订单所属用户
        if (!order.getUser().getUsername().equals(username)) {
            throw new RuntimeException("无权操作此订单");
        }
        
        // 验证订单状态
        if (order.getStatus() != OrderStatus.SHIPPED) {
            throw new RuntimeException("订单状态不正确");
        }
        
        // 更新订单状态为已完成
        order.setStatus(OrderStatus.COMPLETED);
        order.setCompletionTime(LocalDateTime.now());
        orderRepository.save(order);
        
        log.info("订单确认收货成功: orderId={}, username={}", orderId, username);
        
        // 发送订单状态变更消息
        messageService.createOrderStatusMessage(
            order.getUser().getId(),
            order.getId(),
            order.getOrderNo(),
            OrderStatus.COMPLETED.name()
        );
    }

    @Override
    public OrderDTO getOrderById(Long id) {
        log.info("根据ID获取订单: {}", id);
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("订单不存在"));
        return convertToDTO(order);
    }

    private OrderDTO convertToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        
        // 基本信息
        dto.setId(order.getId());
        dto.setOrderNumber(order.getOrderNo());
        
        // 优化用户信息设置
        User user = order.getUser();
        if (user != null) {
            dto.setUserId(user.getId());
            dto.setUsername(user.getUsername());
        } else {
            dto.setUsername("未知用户");
        }
        
        dto.setTotalAmount(BigDecimal.valueOf(order.getTotalAmount().doubleValue()));
        dto.setStatus(order.getStatus().name());
        
        // 时间信息
        dto.setCreatedAt(order.getCreatedAt());
        dto.setPaymentTime(order.getPaymentTime());
        dto.setShippingTime(order.getShippingTime());
        dto.setCompletionTime(order.getCompletionTime());
        
        // 收货信息
        dto.setShippingName(order.getShippingName());
        dto.setShippingPhone(order.getShippingPhone());
        dto.setShippingAddress(order.getShippingAddress());
        
        // 物流信息
        dto.setShippingCompany(order.getShippingCompany());
        dto.setTrackingNumber(order.getTrackingNo());
        
        // 订单项信息
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        List<OrderItemDTO> itemDTOs = items.stream()
            .map(item -> {
                OrderItemDTO itemDTO = new OrderItemDTO();
                itemDTO.setId(item.getId());
                itemDTO.setProductId(item.getProduct().getId());
                itemDTO.setProductName(item.getProductName());
                itemDTO.setProductImage(item.getProduct().getImage());
                itemDTO.setProductPrice(item.getPrice());
                itemDTO.setQuantity(item.getQuantity());
                itemDTO.setSubtotal(item.getPrice().multiply(new BigDecimal(item.getQuantity())));
                return itemDTO;
            })
            .collect(Collectors.toList());
        
        dto.setOrderItems(itemDTOs);
        
        return dto;
    }
} 