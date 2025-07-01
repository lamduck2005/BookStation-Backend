package org.datn.bookstation.repository;

import org.datn.bookstation.entity.EventHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EventHistoryRepository extends JpaRepository<EventHistory, Integer>, JpaSpecificationExecutor<EventHistory> {
    
    @Query("SELECT eh FROM EventHistory eh WHERE eh.event.id = :eventId ORDER BY eh.createdAt DESC")
    List<EventHistory> findByEventId(@Param("eventId") Integer eventId);
    
    @Query("SELECT eh FROM EventHistory eh WHERE eh.performedBy.id = :userId ORDER BY eh.createdAt DESC")
    List<EventHistory> findByUserId(@Param("userId") Integer userId);
    
    @Query("SELECT eh FROM EventHistory eh WHERE eh.event.id = :eventId AND eh.performedBy.id = :userId ORDER BY eh.createdAt DESC")
    List<EventHistory> findByEventIdAndUserId(@Param("eventId") Integer eventId, @Param("userId") Integer userId);
}
