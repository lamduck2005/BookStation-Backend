package org.datn.bookstation.service.impl;

import lombok.AllArgsConstructor;
import org.datn.bookstation.dto.request.EventRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.EventResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.entity.Event;
import org.datn.bookstation.entity.EventCategory;
import org.datn.bookstation.entity.enums.EventStatus;
import org.datn.bookstation.entity.enums.EventType;
import org.datn.bookstation.mapper.EventMapper;
import org.datn.bookstation.mapper.EventResponseMapper;
import org.datn.bookstation.repository.EventCategoryRepository;
import org.datn.bookstation.repository.EventRepository;
import org.datn.bookstation.service.EventService;
import org.datn.bookstation.specification.EventSpecification;
import org.datn.bookstation.validator.ImageUrlValidator;
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
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final EventCategoryRepository eventCategoryRepository;
    private final EventMapper eventMapper;
    private final EventResponseMapper eventResponseMapper;
    private final ImageUrlValidator imageUrlValidator;

    @Override
    public PaginationResponse<EventResponse> getAllWithPagination(int page, int size, String name, 
            Integer categoryId, EventStatus status, EventType eventType, Long startDate, Long endDate) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Specification<Event> specification = EventSpecification.filterBy(name, categoryId, status, eventType, startDate, endDate);
        Page<Event> eventPage = eventRepository.findAll(specification, pageable);
        
        List<EventResponse> eventResponses = eventPage.getContent().stream()
                .map(eventResponseMapper::toResponse)
                .collect(Collectors.toList());
                
        return PaginationResponse.<EventResponse>builder()
                .content(eventResponses)
                .pageNumber(eventPage.getNumber())
                .pageSize(eventPage.getSize())
                .totalElements(eventPage.getTotalElements())
                .totalPages(eventPage.getTotalPages())
                .build();
    }

    @Override
    public List<Event> getAll() {
        return eventRepository.findAll();
    }

    @Override
    public List<Event> getActiveEvents() {
        return eventRepository.findActiveEvents();
    }

    @Override
    public Event getById(Integer id) {
        return eventRepository.findById(id).orElse(null);
    }

    @Override
    public ApiResponse<Event> add(EventRequest request) {
        // Trim và normalize tên sự kiện
        String normalizedEventName = request.getEventName() != null ? request.getEventName().trim() : "";
        
        if (normalizedEventName.isEmpty()) {
            return new ApiResponse<>(400, "Tên sự kiện không được để trống", null);
        }
        
        if (eventRepository.existsByEventNameIgnoreCase(normalizedEventName)) {
            return new ApiResponse<>(400, "Tên sự kiện đã tồn tại", null);
        }
        
        Event event = eventMapper.toEvent(request);
        // Set lại tên đã normalize
        event.setEventName(normalizedEventName);
        
        // Validate image URLs
        try {
            imageUrlValidator.validate(request.getImageUrls());
        } catch (IllegalArgumentException e) {
            return new ApiResponse<>(400, e.getMessage(), null);
        }
        
        // Process image URLs - convert array to comma-separated string
        String imageUrlsString = "";
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            imageUrlsString = String.join(",", request.getImageUrls());
        } else if (request.getImageUrl() != null && !request.getImageUrl().isEmpty()) {
            // Backward compatibility - use single imageUrl if imageUrls is not provided
            imageUrlsString = request.getImageUrl();
        }
        event.setImageUrl(imageUrlsString);
        
        // Set category if provided
        if (request.getEventCategoryId() != null) {
            EventCategory category = eventCategoryRepository.findById(request.getEventCategoryId()).orElse(null);
            if (category == null) {
                return new ApiResponse<>(404, "Không tìm thấy danh mục sự kiện", null);
            }
            event.setEventCategory(category);
        }
        
        event.setCreatedAt(Instant.now().toEpochMilli());
        Event saved = eventRepository.save(event);
        return new ApiResponse<>(201, "Tạo mới thành công", saved);
    }

    @Override
    public ApiResponse<Event> update(EventRequest request, Integer id) {
        Event existing = eventRepository.findById(id).orElse(null);
        if (existing == null) {
            return new ApiResponse<>(404, "Không tìm thấy sự kiện", null);
        }
        
        // Trim và normalize tên sự kiện
        String normalizedEventName = request.getEventName() != null ? request.getEventName().trim() : "";
        
        if (normalizedEventName.isEmpty()) {
            return new ApiResponse<>(400, "Tên sự kiện không được để trống", null);
        }
        
        // Check if name is being changed and already exists
        String existingNormalizedName = existing.getEventName() != null ? existing.getEventName().trim() : "";
        if (!existingNormalizedName.equalsIgnoreCase(normalizedEventName) && 
            eventRepository.existsByEventNameIgnoreCase(normalizedEventName)) {
            return new ApiResponse<>(400, "Tên sự kiện đã tồn tại", null);
        }
        
        // Set category if provided
        if (request.getEventCategoryId() != null) {
            EventCategory category = eventCategoryRepository.findById(request.getEventCategoryId()).orElse(null);
            if (category == null) {
                return new ApiResponse<>(404, "Không tìm thấy danh mục sự kiện", null);
            }
            existing.setEventCategory(category);
        }
        
        existing.setEventName(normalizedEventName);
        existing.setDescription(request.getDescription());
        existing.setEventType(request.getEventType());
        existing.setStatus(request.getStatus());
        existing.setStartDate(request.getStartDate());
        existing.setEndDate(request.getEndDate());
        existing.setMaxParticipants(request.getMaxParticipants());
        
        // Validate image URLs
        try {
            imageUrlValidator.validate(request.getImageUrls());
        } catch (IllegalArgumentException e) {
            return new ApiResponse<>(400, e.getMessage(), null);
        }
        
        // Process image URLs - convert array to comma-separated string
        String imageUrlsString = "";
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            imageUrlsString = String.join(",", request.getImageUrls());
        } else if (request.getImageUrl() != null && !request.getImageUrl().isEmpty()) {
            // Backward compatibility - use single imageUrl if imageUrls is not provided
            imageUrlsString = request.getImageUrl();
        }
        existing.setImageUrl(imageUrlsString);
        
        existing.setLocation(request.getLocation());
        existing.setRules(request.getRules());
        existing.setIsOnline(request.getIsOnline());
        existing.setUpdatedAt(Instant.now().toEpochMilli());
        
        Event saved = eventRepository.save(existing);
        return new ApiResponse<>(200, "Cập nhật thành công", saved);
    }

    @Override
    public void delete(Integer id) {
        eventRepository.deleteById(id);
    }

    @Override
    public ApiResponse<Event> toggleStatus(Integer id) {
        Event event = eventRepository.findById(id).orElse(null);
        if (event == null) {
            return new ApiResponse<>(404, "Không tìm thấy sự kiện", null);
        }
        
        // Cycle through status: DRAFT -> PUBLISHED -> ONGOING -> COMPLETED -> CANCELLED -> DRAFT
        EventStatus currentStatus = event.getStatus();
        EventStatus newStatus;
        
        switch (currentStatus) {
            case DRAFT:
                newStatus = EventStatus.PUBLISHED;
                break;
            case PUBLISHED:
                newStatus = EventStatus.ONGOING;
                break;
            case ONGOING:
                newStatus = EventStatus.COMPLETED;
                break;
            case COMPLETED:
                newStatus = EventStatus.CANCELLED;
                break;
            case CANCELLED:
            default:
                newStatus = EventStatus.DRAFT;
                break;
        }
        
        event.setStatus(newStatus);
        event.setUpdatedAt(Instant.now().toEpochMilli());
        eventRepository.save(event);
        
        return new ApiResponse<>(200, "Cập nhật trạng thái thành công", event);
    }

    @Override
    public List<Event> getEventsByCategory(Integer categoryId) {
        return eventRepository.findByEventCategoryId(categoryId);
    }
}
