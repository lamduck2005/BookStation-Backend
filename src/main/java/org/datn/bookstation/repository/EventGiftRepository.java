package org.datn.bookstation.repository;

import org.datn.bookstation.entity.EventGift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EventGiftRepository extends JpaRepository<EventGift, Integer>, JpaSpecificationExecutor<EventGift> {
    
    @Query("SELECT eg FROM EventGift eg WHERE eg.event.id = :eventId")
    List<EventGift> findByEventId(@Param("eventId") Integer eventId);
    
    @Query("SELECT eg FROM EventGift eg WHERE eg.event.id = :eventId AND eg.isActive = true")
    List<EventGift> findActiveByEventId(@Param("eventId") Integer eventId);
}
