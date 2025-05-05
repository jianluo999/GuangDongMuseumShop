package com.gdcp.guangdongmuseumshop.repository;

import com.gdcp.guangdongmuseumshop.entity.SearchHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {
    
    Optional<SearchHistory> findByKeyword(String keyword);
    
    @Query(value = """
        SELECT keyword 
        FROM search_history 
        GROUP BY keyword 
        ORDER BY SUM(search_count) DESC, MAX(search_time) DESC 
        LIMIT :limit
        """, 
        nativeQuery = true)
    List<String> findTopSearches(@Param("limit") int limit);
} 