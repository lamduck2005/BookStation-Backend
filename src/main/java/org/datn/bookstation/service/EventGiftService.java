package org.datn.bookstation.service;

import org.datn.bookstation.dto.request.EventGiftRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.EventGiftResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.entity.EventGift;

import java.util.List;

public interface EventGiftService {
    PaginationResponse<EventGiftResponse> getAllWithPagination(int page, int size, String giftName, 
            Integer eventId, String giftType, Boolean isActive);
    List<EventGift> getAll();
    List<EventGift> getByEventId(Integer eventId);
    List<EventGift> getActiveByEventId(Integer eventId);
    EventGift getById(Integer id);
    ApiResponse<EventGift> add(EventGiftRequest request);
    ApiResponse<EventGift> update(EventGiftRequest request, Integer id);
    void delete(Integer id);
    ApiResponse<EventGift> toggleStatus(Integer id);
}
