package com.gdcp.guangdongmuseumshop.repository;

import com.gdcp.guangdongmuseumshop.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long>, JpaSpecificationExecutor<Message> {
    Page<Message> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"user"})
    @Override
    Page<Message> findAll(Pageable pageable);

    long countByUserIdAndIsReadFalse(Long userId);

    List<Message> findByUserIdAndIsReadFalse(Long userId);

    @Modifying
    @Query("UPDATE Message m SET m.isRead = true WHERE m.user.id = :userId")
    void markAllAsRead(Long userId);

    @Modifying
    @Query("UPDATE Message m SET m.isRead = true WHERE m.id = :messageId AND m.user.id = :userId")
    void markAsRead(@Param("messageId") Long messageId, @Param("userId") Long userId);
} 