package org.datn.bookstation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventHistoryResponse {
    private Integer id;
    private Integer eventId;
    private String eventName;
    private String actionType;
    private String description;
    private Integer performedBy;
    private String performedByName;
    private String performedByEmail;
    private Long createdAt;
    private String oldValues;
    private String newValues;
}
