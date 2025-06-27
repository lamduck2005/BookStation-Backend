package org.datn.bookstation.service;

import org.datn.bookstation.dto.request.EventCategoryRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.EventCategoryResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.entity.EventCategory;

import java.util.List;

public interface EventCategoryService {
    PaginationResponse<EventCategoryResponse> getAllWithPagination(int page, int size, String name, Boolean isActive);
    List<EventCategory> getAll();
    List<EventCategory> getActiveCategories();
    EventCategory getById(Integer id);
    ApiResponse<EventCategory> add(EventCategoryRequest request);
    ApiResponse<EventCategory> update(EventCategoryRequest request, Integer id);
    void delete(Integer id);
    ApiResponse<EventCategory> toggleStatus(Integer id);
}
