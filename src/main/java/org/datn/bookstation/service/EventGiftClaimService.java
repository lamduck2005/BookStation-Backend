package org.datn.bookstation.service;

import org.datn.bookstation.dto.request.EventGiftClaimRequest;
import org.datn.bookstation.dto.response.EventGiftClaimResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.springframework.data.domain.Pageable;

public interface EventGiftClaimService {
    
    EventGiftClaimResponse create(EventGiftClaimRequest request);
    
    EventGiftClaimResponse findById(Integer id);
    
    EventGiftClaimResponse update(Integer id, EventGiftClaimRequest request);
    
    void delete(Integer id);
    
    PaginationResponse<EventGiftClaimResponse> findAll(
            Integer eventId,
            Integer eventParticipantId,
            Integer eventGiftId,
            Integer userId,
            String claimStatus,
            String deliveryMethod,
            String giftType,
            Boolean autoDelivered,
            Long claimedStartTime,
            Long claimedEndTime,
            Long completedStartTime,
            Long completedEndTime,
            Integer pickupStoreId,
            Pageable pageable
    );
    
    // Business logic methods
    EventGiftClaimResponse claimGift(Integer eventParticipantId, Integer eventGiftId, EventGiftClaimRequest request);
    
    EventGiftClaimResponse approveClaimByAdmin(Integer claimId, String adminNotes);
    
    EventGiftClaimResponse rejectClaimByAdmin(Integer claimId, String adminNotes);
    
    EventGiftClaimResponse confirmDeliveryByStaff(Integer claimId, Integer staffId, String notes);
    
    EventGiftClaimResponse markAsAutoDelivered(Integer claimId);
    
    PaginationResponse<EventGiftClaimResponse> findPendingClaims(Pageable pageable);
    
    PaginationResponse<EventGiftClaimResponse> findClaimsByUser(Integer userId, Pageable pageable);
    
    PaginationResponse<EventGiftClaimResponse> findClaimsByEvent(Integer eventId, Pageable pageable);
}
