package org.datn.bookstation.mapper;

import org.datn.bookstation.dto.request.EventHistoryRequest;
import org.datn.bookstation.entity.Event;
import org.datn.bookstation.entity.EventHistory;
import org.datn.bookstation.entity.User;
import org.springframework.stereotype.Component;

@Component
public class EventHistoryMapper {

    public EventHistory toEntity(EventHistoryRequest request, Event event) {
        EventHistory history = new EventHistory();
        history.setEvent(event);
        history.setActionType(request.getActionType());
        history.setDescription(request.getDescription());
        history.setOldValues(request.getOldValues());
        history.setNewValues(request.getNewValues());
        
        // Set performed by user if provided
        if (request.getPerformedBy() != null) {
            User user = new User();
            user.setId(request.getPerformedBy());
            history.setPerformedBy(user);
        }
        
        return history;
    }

    public void updateEntity(EventHistory existingHistory, EventHistoryRequest request) {
        existingHistory.setActionType(request.getActionType());
        existingHistory.setDescription(request.getDescription());
        existingHistory.setOldValues(request.getOldValues());
        existingHistory.setNewValues(request.getNewValues());
        
        // Update performed by user if provided
        if (request.getPerformedBy() != null) {
            User user = new User();
            user.setId(request.getPerformedBy());
            existingHistory.setPerformedBy(user);
        } else {
            existingHistory.setPerformedBy(null);
        }
    }
}
