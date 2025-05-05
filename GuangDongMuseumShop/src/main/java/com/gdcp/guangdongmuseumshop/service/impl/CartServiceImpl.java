package com.gdcp.guangdongmuseumshop.service.impl;

import com.gdcp.guangdongmuseumshop.dto.CartItemDTO;
import com.gdcp.guangdongmuseumshop.entity.Cart;
import com.gdcp.guangdongmuseumshop.entity.CartItem;
import com.gdcp.guangdongmuseumshop.exception.OrderException;

import com.gdcp.guangdongmuseumshop.entity.Product;
import com.gdcp.guangdongmuseumshop.repository.CartItemRepository;
import com.gdcp.guangdongmuseumshop.repository.ProductRepository;
import com.gdcp.guangdongmuseumshop.repository.CartRepository;
import com.gdcp.guangdongmuseumshop.service.CartService;
import com.gdcp.guangdongmuseumshop.vo.CartVO;
import com.gdcp.guangdongmuseumshop.vo.CartDetailVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;
import com.gdcp.guangdongmuseumshop.dto.CartDTO;
import jakarta.persistence.criteria.Predicate;
import com.gdcp.guangdongmuseumshop.enums.CartStatus;
import com.gdcp.guangdongmuseumshop.mapper.CartMapper;
import com.gdcp.guangdongmuseumshop.mapper.CartItemMapper;
import com.gdcp.guangdongmuseumshop.entity.User;
import com.gdcp.guangdongmuseumshop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.domain.PageImpl;

@Service
@Slf4j
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartItemRepository cartItemRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartMapper cartMapper;
    private final CartItemMapper cartItemMapper;

    @Override
    @Transactional
    public CartItemDTO addToCart(Long userId, CartItemDTO cartItemDTO) {
        // 获取或创建用户的购物车
        Cart cart = cartRepository.findByUser_Id(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new OrderException("用户不存在"));
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    newCart.setStatus(CartStatus.NORMAL);
                    return cartRepository.save(newCart);
                });
        
        Product product = productRepository.findById(cartItemDTO.getProductId())
                .orElseThrow(() -> new OrderException("商品不存在"));

        // 验证库存
        if (product.getStock() < cartItemDTO.getQuantity()) {
            throw new OrderException("商品库存不足，当前库存: " + product.getStock());
        }

        CartItem cartItem = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), cartItemDTO.getProductId())
                .orElse(new CartItem());

        if (cartItem.getId() == null) {
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(cartItemDTO.getQuantity());
            cartItem.setPrice(product.getPrice());
            // 设置用户信息
            cartItem.setUser(cart.getUser());
        } else {
            // 验证更新后的总数量是否超过库存
            int newQuantity = cartItem.getQuantity() + cartItemDTO.getQuantity();
            if (product.getStock() < newQuantity) {
                throw new OrderException("商品库存不足，当前库存: " + product.getStock());
            }
            cartItem.setQuantity(newQuantity);
        }

        cartItem = cartItemRepository.save(cartItem);
        CartItemDTO dto = cartItemMapper.toDTO(cartItem);
        // 设置商品名称和图片
        dto.setProductName(product.getName());
        dto.setProductImage(product.getMainImage());
        dto.setTotalPrice(product.getPrice().doubleValue() * cartItem.getQuantity());
        return dto;
    }

    @Override
    @Transactional
    public void removeFromCart(Long userId, Long productId) {
        Cart cart = cartRepository.findByUser_Id(userId)
                .orElseThrow(() -> new OrderException("购物车不存在"));
        cartItemRepository.deleteByCartIdAndProductId(cart.getId(), productId);
    }

    @Override
    @Transactional
    public CartItemDTO updateQuantity(Long userId, Long itemId, Integer quantity) {
        Cart cart = cartRepository.findByUser_Id(userId)
                .orElseThrow(() -> new OrderException("购物车不存在"));
            
        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new OrderException("购物车项不存在"));
        
        // 验证是否是用户自己的购物车项
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new OrderException("无权操作此购物车项");
        }
        
        // 验证库存并调整数量
        Product product = cartItem.getProduct();
        if (product.getStock() < quantity) {
            quantity = product.getStock();
            log.info("请求数量超过库存，已调整为最大库存数量: {}", quantity);
        }
        
        cartItem.setQuantity(quantity);
        cartItem = cartItemRepository.save(cartItem);
        CartItemDTO dto = cartItemMapper.toDTO(cartItem);
        dto.setTotalPrice(cartItem.getProduct().getPrice().doubleValue() * quantity);
        return dto;
    }

    @Override
    @Transactional
    public void updateSelected(Long userId, Long itemId, Boolean selected) {
        Cart cart = cartRepository.findByUser_Id(userId)
                .orElseThrow(() -> new OrderException("购物车不存在"));
            
        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new OrderException("购物车项不存在"));
        
        // 验证是否是用户自己的购物车项
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new OrderException("无权操作此购物车项");
        }
        
        cartItem.setSelected(selected);
        cartItemRepository.save(cartItem);
    }

    @Override
    public List<CartItemDTO> getCartItems(Long userId) {
        log.debug("获取用户购物车商品列表: userId={}", userId);
        Cart cart = cartRepository.findByUser_Id(userId)
                .orElseGet(() -> {
                    log.debug("用户购物车不存在，创建新购物车: userId={}", userId);
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new OrderException("用户不存在"));
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    newCart.setStatus(CartStatus.NORMAL);
                    return cartRepository.save(newCart);
                });
        
        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());
        log.debug("查询到购物车商品数量: {}", cartItems.size());
        
        return cartItems.stream()
                .map(item -> {
                    CartItemDTO dto = cartItemMapper.toDTO(item);
                    Product product = item.getProduct();
                    // 确保设置商品名称和图片
                    dto.setProductName(product.getName());
                    dto.setProductImage(product.getMainImage());
                    dto.setTotalPrice(item.getPrice().doubleValue() * item.getQuantity());
                    log.debug("购物车项信息: 商品ID={}, 名称={}, 价格={}, 数量={}", 
                        product.getId(), product.getName(), item.getPrice(), item.getQuantity());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void clearCart(Long userId) {
        Cart cart = cartRepository.findByUser_Id(userId)
                .orElseThrow(() -> new OrderException("购物车不存在"));
        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());
        cartItemRepository.deleteAll(cartItems);
    }

    @Override
    public List<CartItemDTO> getSelectedItems(Long userId) {
        return cartItemRepository.findSelectedItems(userId)
                .stream()
                .map(item -> {
                    CartItemDTO dto = cartItemMapper.toDTO(item);
                    dto.setTotalPrice(item.getProduct().getPrice().doubleValue() * item.getQuantity());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Page<CartDTO> getAdminCartList(Pageable pageable, String userId, String status) {
        log.debug("查询购物车列表: pageable={}, userId={}, status={}", pageable, userId, status);
        
        // 1. 构建查询条件
        Specification<Cart> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (StringUtils.hasText(userId)) {
                predicates.add(cb.equal(root.get("user").get("id"), Long.valueOf(userId)));
            }
            
            if (StringUtils.hasText(status)) {
                predicates.add(cb.equal(root.get("status"), CartStatus.valueOf(status)));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        
        // 2. 先查询总数
        long total = cartRepository.count(spec);
        
        // 3. 分页查询数据
        List<Cart> carts = cartRepository.findAll((root, query, cb) -> {
            root.fetch("user", JoinType.LEFT);
            return spec.toPredicate(root, query, cb);
        }, pageable.getSort())
        .stream()
        .skip(pageable.getOffset())
        .limit(pageable.getPageSize())
        .collect(Collectors.toList());

        // 4. 转换为 DTO
        List<CartDTO> dtoList = carts.stream()
            .map(cart -> {
                CartDTO dto = cartMapper.toDTO(cart);
                List<CartItem> items = cartItemRepository.findByCartId(cart.getId());
                dto.setItems(items.stream()
                        .map(item -> {
                            CartItemDTO itemDto = cartItemMapper.toDTO(item);
                            itemDto.setTotalPrice(item.getProduct().getPrice().doubleValue() * item.getQuantity());
                            return itemDto;
                        })
                        .collect(Collectors.toList()));
                return dto;
            })
            .collect(Collectors.toList());

        // 5. 构建分页结果
        return new PageImpl<>(dtoList, pageable, total);
    }

    @Override
    public CartDetailVO getAdminCartDetail(Long id) {
        Cart cart = cartRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("购物车不存在"));
        return convertToCartDetailVO(cart);
    }

    @Override
    @Transactional
    public void deleteCart(Long id) {
        cartRepository.deleteById(id);
    }

    @Override
    public void updateCartStatus(Long id, String status) {
        Cart cart = cartRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("购物车不存在"));
        cart.setStatus(CartStatus.valueOf(status));
        cartRepository.save(cart);
    }

    private CartVO convertToCartVO(Cart cart) {
        CartVO vo = new CartVO();
        // 设置VO属性
        return vo;
    }

    private CartDetailVO convertToCartDetailVO(Cart cart) {
        CartDetailVO vo = new CartDetailVO();
        // 设置VO属性
        return vo;
    }
} 