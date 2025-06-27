package org.datn.bookstation.dto.request;

import lombok.Data;
import org.datn.bookstation.entity.enums.ParticipantStatus;

@Data
public class EventParticipantRequest {
    private Integer eventId;
    private Integer userId;
    private Boolean isWinner;
    private Integer giftReceivedId;
    private ParticipantStatus completionStatus;
    private String notes;
}
