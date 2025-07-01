package org.datn.bookstation.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventGiftClaimRequest {

    @NotNull(message = "Event participant ID is required")
    @Positive(message = "Event participant ID must be positive")
    private Integer eventParticipantId;

    @NotNull(message = "Event gift ID is required")
    @Positive(message = "Event gift ID must be positive")
    private Integer eventGiftId;

    private String deliveryMethod; // ONLINE_SHIPPING, STORE_PICKUP

    private Integer deliveryOrderId; // For ONLINE_SHIPPING method

    private Integer pickupStoreId; // For STORE_PICKUP method

    private String notes;
}
