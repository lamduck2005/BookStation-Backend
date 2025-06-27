package org.datn.bookstation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventGiftClaimResponse {
    private Integer id;
    private Integer eventParticipantId;
    private String participantName;
    private String participantEmail;
    private Integer eventGiftId;
    private String giftName;
    private String giftType;
    private String eventName;
    private Long claimedAt;
    private String claimStatus;
    private String deliveryMethod;
    private Integer deliveryOrderId;
    private String storePickupCode;
    private Integer pickupStoreId;
    private Integer staffConfirmedBy;
    private Boolean autoDelivered;
    private Long completedAt;
    private String notes;
}
