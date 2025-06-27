package org.datn.bookstation.mapper;

import org.datn.bookstation.dto.response.EventResponse;
import org.datn.bookstation.entity.Event;
import org.springframework.stereotype.Component;

@Component
public class EventResponseMapper {
    public EventResponse toResponse(Event event) {
        if (event == null) return null;
        
        EventResponse response = new EventResponse();
        response.setId(event.getId());
        response.setName(event.getEventName());
        response.setDescription(event.getDescription());
        response.setCategoryId(event.getEventCategory() != null ? event.getEventCategory().getId() : null);
        response.setCategoryName(event.getEventCategory() != null ? event.getEventCategory().getCategoryName() : null);
        response.setStartDate(event.getStartDate());
        response.setEndDate(event.getEndDate());
        response.setMaxParticipants(event.getMaxParticipants());
        response.setCurrentParticipants(event.getCurrentParticipants());
        response.setStatus(event.getStatus() != null ? (byte)event.getStatus().ordinal() : null);
        response.setCreatedAt(event.getCreatedAt());
        response.setUpdatedAt(event.getUpdatedAt());
        
        return response;
    }
}
