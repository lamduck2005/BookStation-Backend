package org.datn.bookstation.mapper;

import org.datn.bookstation.dto.response.EventParticipantResponse;
import org.datn.bookstation.entity.EventParticipant;
import org.springframework.stereotype.Component;

@Component
public class EventParticipantResponseMapper {
    public EventParticipantResponse toResponse(EventParticipant participant) {
        if (participant == null) return null;
        
        EventParticipantResponse response = new EventParticipantResponse();
        response.setId(participant.getId());
        response.setEventId(participant.getEvent() != null ? participant.getEvent().getId() : null);
        response.setEventName(participant.getEvent() != null ? participant.getEvent().getEventName() : null);
        response.setUserId(participant.getUser() != null ? participant.getUser().getId() : null);
        response.setUserEmail(participant.getUser() != null ? participant.getUser().getEmail() : null);
        response.setUserName(participant.getUser() != null ? participant.getUser().getFullName() : null);
        response.setJoinedAt(participant.getJoinedAt());
        response.setIsWinner(participant.getIsWinner());
        response.setCompletionStatus(participant.getCompletionStatus());
        response.setNotes(participant.getNotes());
        
        return response;
    }
}
