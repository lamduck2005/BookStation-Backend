package org.datn.bookstation.service;

import org.datn.bookstation.dto.request.EventRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.EventResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.entity.Event;
import org.datn.bookstation.entity.enums.EventStatus;

import java.util.List;

public interface EventService {
    PaginationResponse<EventResponse> getAllWithPagination(int page, int size, String name, 
            Integer categoryId, EventStatus status, Long startDate, Long endDate);
    List<Event> getAll();
    List<Event> getActiveEvents();
    Event getById(Integer id);
    ApiResponse<Event> add(EventRequest request);
    ApiResponse<Event> update(EventRequest request, Integer id);
    void delete(Integer id);
    ApiResponse<Event> toggleStatus(Integer id);
    List<Event> getEventsByCategory(Integer categoryId);
}
