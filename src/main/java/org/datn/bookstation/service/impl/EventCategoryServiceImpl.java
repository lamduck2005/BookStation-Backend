package org.datn.bookstation.service.impl;

import lombok.AllArgsConstructor;
import org.datn.bookstation.dto.request.EventCategoryRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.EventCategoryResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.entity.EventCategory;
import org.datn.bookstation.mapper.EventCategoryMapper;
import org.datn.bookstation.mapper.EventCategoryResponseMapper;
import org.datn.bookstation.repository.EventCategoryRepository;
import org.datn.bookstation.service.EventCategoryService;
import org.datn.bookstation.specification.EventCategorySpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class EventCategoryServiceImpl implements EventCategoryService {
    private final EventCategoryRepository eventCategoryRepository;
    private final EventCategoryMapper eventCategoryMapper;
    private final EventCategoryResponseMapper eventCategoryResponseMapper;

    @Override
    public PaginationResponse<EventCategoryResponse> getAllWithPagination(int page, int size, String name, Boolean isActive) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Specification<EventCategory> specification = EventCategorySpecification.filterBy(name, isActive);
        Page<EventCategory> categoryPage = eventCategoryRepository.findAll(specification, pageable);
        
        List<EventCategoryResponse> categoryResponses = categoryPage.getContent().stream()
                .map(eventCategoryResponseMapper::toResponse)
                .collect(Collectors.toList());
                
        return PaginationResponse.<EventCategoryResponse>builder()
                .content(categoryResponses)
                .pageNumber(categoryPage.getNumber())
                .pageSize(categoryPage.getSize())
                .totalElements(categoryPage.getTotalElements())
                .totalPages(categoryPage.getTotalPages())
                .build();
    }

    @Override
    public List<EventCategory> getAll() {
        return eventCategoryRepository.findAll();
    }

    @Override
    public List<EventCategory> getActiveCategories() {
        return eventCategoryRepository.findByIsActiveTrue();
    }

    @Override
    public EventCategory getById(Integer id) {
        return eventCategoryRepository.findById(id).orElse(null);
    }

    @Override
    public ApiResponse<EventCategory> add(EventCategoryRequest request) {
        if (eventCategoryRepository.existsByCategoryName(request.getCategoryName())) {
            return new ApiResponse<>(400, "Tên danh mục đã tồn tại", null);
        }
        
        EventCategory category = eventCategoryMapper.toEventCategory(request);
        category.setCreatedAt(Instant.now().toEpochMilli());
        EventCategory saved = eventCategoryRepository.save(category);
        return new ApiResponse<>(201, "Tạo mới thành công", saved);
    }

    @Override
    public ApiResponse<EventCategory> update(EventCategoryRequest request, Integer id) {
        EventCategory existing = eventCategoryRepository.findById(id).orElse(null);
        if (existing == null) {
            return new ApiResponse<>(404, "Không tìm thấy danh mục", null);
        }
        
        // Check if name is being changed and already exists
        if (!existing.getCategoryName().equals(request.getCategoryName()) && 
            eventCategoryRepository.existsByCategoryName(request.getCategoryName())) {
            return new ApiResponse<>(400, "Tên danh mục đã tồn tại", null);
        }
        
        existing.setCategoryName(request.getCategoryName());
        existing.setDescription(request.getDescription());
        existing.setIconUrl(request.getIconUrl());
        existing.setIsActive(request.getIsActive());
        
        EventCategory saved = eventCategoryRepository.save(existing);
        return new ApiResponse<>(200, "Cập nhật thành công", saved);
    }

    @Override
    public void delete(Integer id) {
        eventCategoryRepository.deleteById(id);
    }

    @Override
    public ApiResponse<EventCategory> toggleStatus(Integer id) {
        EventCategory category = eventCategoryRepository.findById(id).orElse(null);
        if (category == null) {
            return new ApiResponse<>(404, "Không tìm thấy danh mục", null);
        }
        
        category.setIsActive(category.getIsActive() == null || !category.getIsActive());
        eventCategoryRepository.save(category);
        
        return new ApiResponse<>(200, "Cập nhật trạng thái thành công", category);
    }
}
