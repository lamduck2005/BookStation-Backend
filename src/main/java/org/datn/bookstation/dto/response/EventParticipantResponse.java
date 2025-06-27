package org.datn.bookstation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.datn.bookstation.entity.enums.ParticipantStatus;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventParticipantResponse {
    private Integer id;
    private Integer eventId;
    private String eventName;
    private Integer userId;
    private String userEmail;
    private String userName;
    private Long joinedAt;
    private Boolean isWinner;
    private ParticipantStatus completionStatus;
    private String notes;
}
