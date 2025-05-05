package com.gdcp.guangdongmuseumshop.repository;

import com.gdcp.guangdongmuseumshop.entity.Order;
import com.gdcp.guangdongmuseumshop.entity.User;
import com.gdcp.guangdongmuseumshop.enums.OrderStatus;
import com.gdcp.guangdongmuseumshop.dto.stats.DailySalesStatsDTO;
import com.gdcp.guangdongmuseumshop.dto.stats.MonthlySalesStatsDTO;
import com.gdcp.guangdongmuseumshop.dto.stats.ProductStatsDTO;
import com.gdcp.guangdongmuseumshop.dto.stats.DailySalesStatsProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {
    Page<Order> findByUserId(Long userId, Pageable pageable);
    
    @Query(value = """
        SELECT DATE(o.created_at) as date,
               COUNT(*) as orderCount, 
               COALESCE(SUM(o.total_amount), 0) as totalAmount
        FROM orders o
        WHERE o.created_at BETWEEN :startDate AND :endDate
        AND o.status = 'PAID'
        GROUP BY DATE(o.created_at)
        ORDER BY date ASC
        """, nativeQuery = true)
    List<DailySalesStatsProjection> getDailySalesStats(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT NEW com.gdcp.guangdongmuseumshop.dto.stats.MonthlySalesStatsDTO(" +
           "YEAR(o.createdAt), MONTH(o.createdAt), COUNT(o), SUM(o.totalAmount)) " +
           "FROM Order o WHERE o.createdAt BETWEEN ?1 AND ?2 GROUP BY YEAR(o.createdAt), MONTH(o.createdAt)")
    List<MonthlySalesStatsDTO> getMonthlySalesStats(LocalDateTime startTime, LocalDateTime endTime);
    
    @Query("SELECT NEW com.gdcp.guangdongmuseumshop.dto.stats.ProductStatsDTO(" +
           "p.id, p.name, COALESCE(SUM(oi.quantity), 0), COALESCE(SUM(oi.quantity * oi.price), 0)) " +
           "FROM OrderItem oi JOIN oi.product p GROUP BY p.id, p.name")
    List<ProductStatsDTO> getTopSellingProducts(Pageable pageable);
    
    @Query("SELECT NEW com.gdcp.guangdongmuseumshop.dto.stats.ProductStatsDTO(" +
           "p.id, p.name, COALESCE(SUM(oi.quantity), 0), COALESCE(SUM(oi.quantity * oi.price), 0)) " +
           "FROM OrderItem oi JOIN oi.product p WHERE p.category.id = ?1 GROUP BY p.id, p.name")
    List<ProductStatsDTO> getProductStatsByCategory(Long categoryId);
    
    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.orderItems WHERE o.user = :user")
    Page<Order> findByUserWithItems(@Param("user") User user, Pageable pageable);
    
    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.orderItems WHERE o.user = :user AND o.status = :status")
    Page<Order> findByUserAndStatusWithItems(@Param("user") User user, @Param("status") OrderStatus status, Pageable pageable);

    Page<Order> findByUser(User user, Pageable pageable);
    Page<Order> findByUserAndStatus(User user, String status, Pageable pageable);

    @Query("SELECT COUNT(o) > 0 FROM Order o JOIN o.orderItems i " +
           "WHERE o.user.id = :userId AND i.product.id = :productId AND o.status = :status")
    boolean existsByUserIdAndProductIdAndStatus(
        @Param("userId") Long userId,
        @Param("productId") Long productId,
        @Param("status") OrderStatus status);

    Optional<Order> findByOrderNo(String orderNo);
    List<Order> findByUser(User user);
    List<Order> findByUserAndStatus(User user, OrderStatus status);

    @Query("SELECT DISTINCT o FROM Order o " +
           "LEFT JOIN FETCH o.orderItems " + 
           "WHERE o.user.id = :userId " +
           "ORDER BY o.createdAt DESC")
    List<Order> findByUserIdWithItems(@Param("userId") Long userId);

    @Query("SELECT DISTINCT o FROM Order o " +
           "LEFT JOIN FETCH o.orderItems " +
           "WHERE o.user.id = :userId AND o.status = :status " + 
           "ORDER BY o.createdAt DESC")
    List<Order> findByUserIdAndStatusWithItems(
        @Param("userId") Long userId,
        @Param("status") OrderStatus status
    );

    @Query("SELECT o FROM Order o JOIN o.orderItems oi " +
           "WHERE o.user.id = :userId AND oi.product.id = :productId AND o.status = :status " +
           "ORDER BY o.createdAt DESC")
    Optional<Order> findFirstByUserIdAndProductIdAndStatus(
        @Param("userId") Long userId,
        @Param("productId") Long productId,
        @Param("status") OrderStatus status);
} 