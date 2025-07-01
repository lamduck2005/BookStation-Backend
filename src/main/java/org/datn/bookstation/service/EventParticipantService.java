package org.datn.bookstation.service;

import org.datn.bookstation.dto.request.EventParticipantRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.EventParticipantResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.entity.EventParticipant;
import org.datn.bookstation.entity.enums.ParticipantStatus;

import java.util.List;

public interface EventParticipantService {
    PaginationResponse<EventParticipantResponse> getAllWithPagination(int page, int size, Integer eventId, 
            Integer userId, ParticipantStatus status, Boolean isWinner, String userEmail);
    List<EventParticipant> getAll();
    List<EventParticipant> getByEventId(Integer eventId);
    List<EventParticipant> getByUserId(Integer userId);
    EventParticipant getById(Integer id);
    EventParticipant findByEventIdAndUserId(Integer eventId, Integer userId);
    ApiResponse<EventParticipant> add(EventParticipantRequest request);
    ApiResponse<EventParticipant> update(EventParticipantRequest request, Integer id);
    void delete(Integer id);
    ApiResponse<EventParticipant> joinEvent(Integer eventId, Integer userId);
    ApiResponse<EventParticipant> completeTask(Integer participantId);
}
