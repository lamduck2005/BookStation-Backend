package org.datn.bookstation.mapper;

import org.datn.bookstation.dto.response.EventHistoryResponse;
import org.datn.bookstation.entity.EventHistory;
import org.springframework.stereotype.Component;

@Component
public class EventHistoryResponseMapper {

    public EventHistoryResponse toResponse(EventHistory history) {
        return EventHistoryResponse.builder()
                .id(history.getId())
                .eventId(history.getEvent().getId())
                .eventName(history.getEvent().getEventName())
                .actionType(history.getActionType())
                .description(history.getDescription())
                .performedBy(history.getPerformedBy() != null ? history.getPerformedBy().getId() : null)
                .performedByName(history.getPerformedBy() != null ? history.getPerformedBy().getFullName() : null)
                .performedByEmail(history.getPerformedBy() != null ? history.getPerformedBy().getEmail() : null)
                .createdAt(history.getCreatedAt())
                .oldValues(history.getOldValues())
                .newValues(history.getNewValues())
                .build();
    }
}
