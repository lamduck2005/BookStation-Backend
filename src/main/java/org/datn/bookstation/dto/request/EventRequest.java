package org.datn.bookstation.dto.request;

import lombok.Data;
import org.datn.bookstation.entity.enums.EventStatus;
import org.datn.bookstation.entity.enums.EventType;

@Data
public class EventRequest {
    private String eventName;
    private String description;
    private EventType eventType;
    private Integer eventCategoryId;
    private EventStatus status;
    private Long startDate;
    private Long endDate;
    private Integer maxParticipants;
    private String imageUrl;
    private String location;
    private String rules;
    private Boolean isOnline;
}
