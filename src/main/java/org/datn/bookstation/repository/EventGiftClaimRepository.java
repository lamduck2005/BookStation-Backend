package org.datn.bookstation.repository;

import org.datn.bookstation.entity.EventGiftClaim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EventGiftClaimRepository extends JpaRepository<EventGiftClaim, Integer>, JpaSpecificationExecutor<EventGiftClaim> {
    
    @Query("SELECT egc FROM EventGiftClaim egc WHERE egc.eventParticipant.id = :participantId")
    List<EventGiftClaim> findByParticipantId(@Param("participantId") Integer participantId);
    
    @Query("SELECT egc FROM EventGiftClaim egc WHERE egc.eventGift.id = :giftId")
    List<EventGiftClaim> findByGiftId(@Param("giftId") Integer giftId);
    
    @Query("SELECT egc FROM EventGiftClaim egc WHERE egc.eventParticipant.user.id = :userId")
    List<EventGiftClaim> findByUserId(@Param("userId") Integer userId);
    
    boolean existsByEventParticipantIdAndEventGiftId(Integer eventParticipantId, Integer eventGiftId);
}
