package org.datn.bookstation.repository;

import org.datn.bookstation.entity.EventParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EventParticipantRepository extends JpaRepository<EventParticipant, Integer>, JpaSpecificationExecutor<EventParticipant> {
    
    @Query("SELECT ep FROM EventParticipant ep WHERE ep.event.id = :eventId")
    List<EventParticipant> findByEventId(@Param("eventId") Integer eventId);
    
    @Query("SELECT ep FROM EventParticipant ep WHERE ep.user.id = :userId")
    List<EventParticipant> findByUserId(@Param("userId") Integer userId);
    
    @Query("SELECT ep FROM EventParticipant ep WHERE ep.event.id = :eventId AND ep.user.id = :userId")
    Optional<EventParticipant> findByEventIdAndUserId(@Param("eventId") Integer eventId, @Param("userId") Integer userId);
    
    @Query("SELECT COUNT(ep) FROM EventParticipant ep WHERE ep.event.id = :eventId AND ep.completionStatus = 'JOINED'")
    Integer countRegisteredParticipants(@Param("eventId") Integer eventId);
}
