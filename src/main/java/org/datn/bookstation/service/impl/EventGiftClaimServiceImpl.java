package org.datn.bookstation.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.datn.bookstation.dto.request.EventGiftClaimRequest;
import org.datn.bookstation.dto.response.EventGiftClaimResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.entity.EventGiftClaim;
import org.datn.bookstation.entity.EventParticipant;
import org.datn.bookstation.entity.EventGift;
import org.datn.bookstation.entity.enums.GiftClaimStatus;
import org.datn.bookstation.entity.enums.GiftDeliveryMethod;
import org.datn.bookstation.mapper.EventGiftClaimMapper;
import org.datn.bookstation.mapper.EventGiftClaimResponseMapper;
import org.datn.bookstation.repository.EventGiftClaimRepository;
import org.datn.bookstation.repository.EventParticipantRepository;
import org.datn.bookstation.repository.EventGiftRepository;
import org.datn.bookstation.service.EventGiftClaimService;
import org.datn.bookstation.specification.EventGiftClaimSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventGiftClaimServiceImpl implements EventGiftClaimService {

    private final EventGiftClaimRepository eventGiftClaimRepository;
    private final EventParticipantRepository eventParticipantRepository;
    private final EventGiftRepository eventGiftRepository;
    private final EventGiftClaimMapper eventGiftClaimMapper;
    private final EventGiftClaimResponseMapper eventGiftClaimResponseMapper;

    @Override
    @Transactional
    public EventGiftClaimResponse create(EventGiftClaimRequest request) {
        log.info("Creating new event gift claim for participant: {}, gift: {}", 
                request.getEventParticipantId(), request.getEventGiftId());

        EventParticipant participant = eventParticipantRepository.findById(request.getEventParticipantId())
                .orElseThrow(() -> new RuntimeException("Event participant not found with id: " + request.getEventParticipantId()));

        EventGift gift = eventGiftRepository.findById(request.getEventGiftId())
                .orElseThrow(() -> new RuntimeException("Event gift not found with id: " + request.getEventGiftId()));

        // Validate business rules
        validateClaimRequest(participant, gift);

        EventGiftClaim claim = eventGiftClaimMapper.toEntity(request, participant, gift);
        claim = eventGiftClaimRepository.save(claim);

        log.info("Successfully created event gift claim with id: {}", claim.getId());
        return eventGiftClaimResponseMapper.toResponse(claim);
    }

    @Override
    public EventGiftClaimResponse findById(Integer id) {
        log.info("Finding event gift claim by id: {}", id);
        
        EventGiftClaim claim = eventGiftClaimRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event gift claim not found with id: " + id));
        
        return eventGiftClaimResponseMapper.toResponse(claim);
    }

    @Override
    @Transactional
    public EventGiftClaimResponse update(Integer id, EventGiftClaimRequest request) {
        log.info("Updating event gift claim with id: {}", id);

        EventGiftClaim existingClaim = eventGiftClaimRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event gift claim not found with id: " + id));

        eventGiftClaimMapper.updateEntity(existingClaim, request);
        existingClaim = eventGiftClaimRepository.save(existingClaim);

        log.info("Successfully updated event gift claim with id: {}", id);
        return eventGiftClaimResponseMapper.toResponse(existingClaim);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        log.info("Deleting event gift claim with id: {}", id);

        if (!eventGiftClaimRepository.existsById(id)) {
            throw new RuntimeException("Event gift claim not found with id: " + id);
        }

        eventGiftClaimRepository.deleteById(id);
        log.info("Successfully deleted event gift claim with id: {}", id);
    }

    @Override
    public PaginationResponse<EventGiftClaimResponse> findAll(
            Integer eventId, Integer eventParticipantId, Integer eventGiftId, Integer userId,
            String claimStatus, String deliveryMethod, String giftType, Boolean autoDelivered,
            Long claimedStartTime, Long claimedEndTime, Long completedStartTime, Long completedEndTime,
            Integer pickupStoreId, Pageable pageable) {

        log.info("Finding all event gift claims with filters");

        Specification<EventGiftClaim> spec = null;

        if (eventId != null) {
            spec = EventGiftClaimSpecification.hasEventId(eventId);
        }
        if (eventParticipantId != null) {
            spec = (spec == null) ? EventGiftClaimSpecification.hasEventParticipantId(eventParticipantId) : 
                   spec.and(EventGiftClaimSpecification.hasEventParticipantId(eventParticipantId));
        }
        if (eventGiftId != null) {
            spec = (spec == null) ? EventGiftClaimSpecification.hasEventGiftId(eventGiftId) : 
                   spec.and(EventGiftClaimSpecification.hasEventGiftId(eventGiftId));
        }
        if (userId != null) {
            spec = (spec == null) ? EventGiftClaimSpecification.hasUserId(userId) : 
                   spec.and(EventGiftClaimSpecification.hasUserId(userId));
        }
        if (claimStatus != null) {
            spec = (spec == null) ? EventGiftClaimSpecification.hasClaimStatus(claimStatus) : 
                   spec.and(EventGiftClaimSpecification.hasClaimStatus(claimStatus));
        }
        if (deliveryMethod != null) {
            spec = (spec == null) ? EventGiftClaimSpecification.hasDeliveryMethod(deliveryMethod) : 
                   spec.and(EventGiftClaimSpecification.hasDeliveryMethod(deliveryMethod));
        }
        if (giftType != null) {
            spec = (spec == null) ? EventGiftClaimSpecification.hasGiftType(giftType) : 
                   spec.and(EventGiftClaimSpecification.hasGiftType(giftType));
        }
        if (autoDelivered != null) {
            spec = (spec == null) ? EventGiftClaimSpecification.hasAutoDelivered(autoDelivered) : 
                   spec.and(EventGiftClaimSpecification.hasAutoDelivered(autoDelivered));
        }
        if (claimedStartTime != null || claimedEndTime != null) {
            spec = (spec == null) ? EventGiftClaimSpecification.claimedBetween(claimedStartTime, claimedEndTime) : 
                   spec.and(EventGiftClaimSpecification.claimedBetween(claimedStartTime, claimedEndTime));
        }
        if (completedStartTime != null || completedEndTime != null) {
            spec = (spec == null) ? EventGiftClaimSpecification.completedBetween(completedStartTime, completedEndTime) : 
                   spec.and(EventGiftClaimSpecification.completedBetween(completedStartTime, completedEndTime));
        }
        if (pickupStoreId != null) {
            spec = (spec == null) ? EventGiftClaimSpecification.hasPickupStoreId(pickupStoreId) : 
                   spec.and(EventGiftClaimSpecification.hasPickupStoreId(pickupStoreId));
        }

        Page<EventGiftClaim> claimsPage = eventGiftClaimRepository.findAll(spec, pageable);
        
        return PaginationResponse.<EventGiftClaimResponse>builder()
                .content(claimsPage.getContent().stream()
                        .map(eventGiftClaimResponseMapper::toResponse)
                        .toList())
                .pageNumber(claimsPage.getNumber())
                .pageSize(claimsPage.getSize())
                .totalElements(claimsPage.getTotalElements())
                .totalPages(claimsPage.getTotalPages())
                .build();
    }

    @Override
    @Transactional
    public EventGiftClaimResponse claimGift(Integer eventParticipantId, Integer eventGiftId, EventGiftClaimRequest request) {
        log.info("Processing gift claim for participant: {}, gift: {}", eventParticipantId, eventGiftId);

        EventParticipant participant = eventParticipantRepository.findById(eventParticipantId)
                .orElseThrow(() -> new RuntimeException("Event participant not found with id: " + eventParticipantId));

        EventGift gift = eventGiftRepository.findById(eventGiftId)
                .orElseThrow(() -> new RuntimeException("Event gift not found with id: " + eventGiftId));

        // Check if already claimed
        boolean alreadyClaimed = eventGiftClaimRepository.existsByEventParticipantIdAndEventGiftId(eventParticipantId, eventGiftId);
        if (alreadyClaimed) {
            throw new RuntimeException("Gift already claimed by this participant");
        }

        // Validate business rules
        validateClaimRequest(participant, gift);

        // Create claim
        EventGiftClaim claim = eventGiftClaimMapper.toEntity(request, participant, gift);
        
        // Set store pickup code for store pickup method
        if (claim.getDeliveryMethod() == GiftDeliveryMethod.STORE_PICKUP) {
            claim.setStorePickupCode(generatePickupCode());
        }

        // Auto-deliver for points and vouchers
        if ("POINT".equals(gift.getGiftType()) || "VOUCHER".equals(gift.getGiftType())) {
            claim.setAutoDelivered(true);
            claim.setClaimStatus(GiftClaimStatus.DELIVERED);
            claim.setCompletedAt(System.currentTimeMillis());
        }

        claim = eventGiftClaimRepository.save(claim);

        // Update remaining quantity
        if (gift.getRemainingQuantity() != null && gift.getRemainingQuantity() > 0) {
            gift.setRemainingQuantity(gift.getRemainingQuantity() - 1);
            eventGiftRepository.save(gift);
        }

        log.info("Successfully processed gift claim with id: {}", claim.getId());
        return eventGiftClaimResponseMapper.toResponse(claim);
    }

    @Override
    @Transactional
    public EventGiftClaimResponse approveClaimByAdmin(Integer claimId, String adminNotes) {
        log.info("Approving claim by admin: {}", claimId);

        EventGiftClaim claim = eventGiftClaimRepository.findById(claimId)
                .orElseThrow(() -> new RuntimeException("Event gift claim not found with id: " + claimId));

        if (claim.getClaimStatus() != GiftClaimStatus.PENDING) {
            throw new RuntimeException("Only pending claims can be approved");
        }

        claim.setClaimStatus(GiftClaimStatus.APPROVED);
        claim.setNotes(adminNotes);
        claim = eventGiftClaimRepository.save(claim);

        log.info("Successfully approved claim with id: {}", claimId);
        return eventGiftClaimResponseMapper.toResponse(claim);
    }

    @Override
    @Transactional
    public EventGiftClaimResponse rejectClaimByAdmin(Integer claimId, String adminNotes) {
        log.info("Rejecting claim by admin: {}", claimId);

        EventGiftClaim claim = eventGiftClaimRepository.findById(claimId)
                .orElseThrow(() -> new RuntimeException("Event gift claim not found with id: " + claimId));

        if (claim.getClaimStatus() != GiftClaimStatus.PENDING) {
            throw new RuntimeException("Only pending claims can be rejected");
        }

        claim.setClaimStatus(GiftClaimStatus.REJECTED);
        claim.setNotes(adminNotes);
        claim = eventGiftClaimRepository.save(claim);

        // Restore remaining quantity
        EventGift gift = claim.getEventGift();
        if (gift.getRemainingQuantity() != null) {
            gift.setRemainingQuantity(gift.getRemainingQuantity() + 1);
            eventGiftRepository.save(gift);
        }

        log.info("Successfully rejected claim with id: {}", claimId);
        return eventGiftClaimResponseMapper.toResponse(claim);
    }

    @Override
    @Transactional
    public EventGiftClaimResponse confirmDeliveryByStaff(Integer claimId, Integer staffId, String notes) {
        log.info("Confirming delivery by staff: {} for claim: {}", staffId, claimId);

        EventGiftClaim claim = eventGiftClaimRepository.findById(claimId)
                .orElseThrow(() -> new RuntimeException("Event gift claim not found with id: " + claimId));

        if (claim.getClaimStatus() != GiftClaimStatus.APPROVED) {
            throw new RuntimeException("Only approved claims can be delivered");
        }

        claim.setClaimStatus(GiftClaimStatus.DELIVERED);
        claim.setStaffConfirmedBy(staffId);
        claim.setCompletedAt(System.currentTimeMillis());
        claim.setNotes(notes);
        claim = eventGiftClaimRepository.save(claim);

        log.info("Successfully confirmed delivery for claim with id: {}", claimId);
        return eventGiftClaimResponseMapper.toResponse(claim);
    }

    @Override
    @Transactional
    public EventGiftClaimResponse markAsAutoDelivered(Integer claimId) {
        log.info("Marking claim as auto-delivered: {}", claimId);

        EventGiftClaim claim = eventGiftClaimRepository.findById(claimId)
                .orElseThrow(() -> new RuntimeException("Event gift claim not found with id: " + claimId));

        claim.setAutoDelivered(true);
        claim.setClaimStatus(GiftClaimStatus.DELIVERED);
        claim.setCompletedAt(System.currentTimeMillis());
        claim = eventGiftClaimRepository.save(claim);

        log.info("Successfully marked claim as auto-delivered with id: {}", claimId);
        return eventGiftClaimResponseMapper.toResponse(claim);
    }

    @Override
    public PaginationResponse<EventGiftClaimResponse> findPendingClaims(Pageable pageable) {
        log.info("Finding pending claims");
        return findAll(null, null, null, null, "PENDING", null, null, null, null, null, null, null, null, pageable);
    }

    @Override
    public PaginationResponse<EventGiftClaimResponse> findClaimsByUser(Integer userId, Pageable pageable) {
        log.info("Finding claims by user: {}", userId);
        return findAll(null, null, null, userId, null, null, null, null, null, null, null, null, null, pageable);
    }

    @Override
    public PaginationResponse<EventGiftClaimResponse> findClaimsByEvent(Integer eventId, Pageable pageable) {
        log.info("Finding claims by event: {}", eventId);
        return findAll(eventId, null, null, null, null, null, null, null, null, null, null, null, null, pageable);
    }

    private void validateClaimRequest(EventParticipant participant, EventGift gift) {
        // Check if gift belongs to the same event
        if (!gift.getEvent().getId().equals(participant.getEvent().getId())) {
            throw new RuntimeException("Gift does not belong to the participant's event");
        }

        // Check if gift is active
        if (!gift.getIsActive()) {
            throw new RuntimeException("Gift is not active");
        }

        // Check remaining quantity
        if (gift.getRemainingQuantity() != null && gift.getRemainingQuantity() <= 0) {
            throw new RuntimeException("Gift is out of stock");
        }

        // Check if participant has enough points (if required)
        // This could be extended based on business requirements
    }

    private String generatePickupCode() {
        return "PU-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
