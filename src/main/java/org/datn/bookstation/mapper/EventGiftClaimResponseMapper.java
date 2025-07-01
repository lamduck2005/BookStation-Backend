package org.datn.bookstation.mapper;

import org.datn.bookstation.dto.response.EventGiftClaimResponse;
import org.datn.bookstation.entity.EventGiftClaim;
import org.springframework.stereotype.Component;

@Component
public class EventGiftClaimResponseMapper {

    public EventGiftClaimResponse toResponse(EventGiftClaim claim) {
        return EventGiftClaimResponse.builder()
                .id(claim.getId())
                .eventParticipantId(claim.getEventParticipant().getId())
                .participantName(claim.getEventParticipant().getUser().getFullName())
                .participantEmail(claim.getEventParticipant().getUser().getEmail())
                .eventGiftId(claim.getEventGift().getId())
                .giftName(claim.getEventGift().getGiftName())
                .giftType(claim.getEventGift().getGiftType())
                .eventName(claim.getEventParticipant().getEvent().getEventName())
                .claimedAt(claim.getClaimedAt())
                .claimStatus(claim.getClaimStatus() != null ? claim.getClaimStatus().name() : null)
                .deliveryMethod(claim.getDeliveryMethod() != null ? claim.getDeliveryMethod().name() : null)
                .deliveryOrderId(claim.getDeliveryOrder() != null ? claim.getDeliveryOrder().getId() : null)
                .storePickupCode(claim.getStorePickupCode())
                .pickupStoreId(claim.getPickupStoreId())
                .staffConfirmedBy(claim.getStaffConfirmedBy())
                .autoDelivered(claim.getAutoDelivered())
                .completedAt(claim.getCompletedAt())
                .notes(claim.getNotes())
                .build();
    }
}
