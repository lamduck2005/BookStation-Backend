package org.datn.bookstation.service;

import org.datn.bookstation.dto.request.EventHistoryRequest;
import org.datn.bookstation.dto.response.EventHistoryResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.springframework.data.domain.Pageable;

public interface EventHistoryService {
    
    EventHistoryResponse create(EventHistoryRequest request);
    
    EventHistoryResponse findById(Integer id);
    
    EventHistoryResponse update(Integer id, EventHistoryRequest request);
    
    void delete(Integer id);
    
    PaginationResponse<EventHistoryResponse> findAll(
            Integer eventId,
            String actionType,
            Integer performedBy,
            Long createdStartTime,
            Long createdEndTime,
            String descriptionKeyword,
            String eventName,
            String performedByName,
            String[] actionTypes,
            Pageable pageable
    );
    
    // Business logic methods
    EventHistoryResponse logAction(Integer eventId, String actionType, String description, Integer performedBy);
    
    EventHistoryResponse logActionWithValues(Integer eventId, String actionType, String description, 
                                           Integer performedBy, String oldValues, String newValues);
    
    PaginationResponse<EventHistoryResponse> findHistoryByEvent(Integer eventId, Pageable pageable);
    
    PaginationResponse<EventHistoryResponse> findHistoryByUser(Integer userId, Pageable pageable);
    
    PaginationResponse<EventHistoryResponse> findRecentHistory(Integer limit, Pageable pageable);
}
