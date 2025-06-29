package org.datn.bookstation.repository;

import org.datn.bookstation.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Integer>, JpaSpecificationExecutor<Event> {
    boolean existsByEventName(String eventName);
    
    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM Event e WHERE UPPER(TRIM(e.eventName)) = UPPER(TRIM(:eventName))")
    boolean existsByEventNameIgnoreCase(@Param("eventName") String eventName);
    
    @Query("SELECT e FROM Event e WHERE e.eventCategory.id = :categoryId")
    List<Event> findByEventCategoryId(@Param("categoryId") Integer categoryId);
    
    @Query("SELECT e FROM Event e WHERE e.status = 'PUBLISHED' OR e.status = 'ONGOING' ORDER BY e.createdAt DESC")
    List<Event> findActiveEvents();
}
