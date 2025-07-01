package org.datn.bookstation.mapper;

import org.datn.bookstation.dto.response.EventCategoryResponse;
import org.datn.bookstation.entity.EventCategory;
import org.springframework.stereotype.Component;

@Component
public class EventCategoryResponseMapper {
    public EventCategoryResponse toResponse(EventCategory category) {
        if (category == null) return null;
        
        EventCategoryResponse response = new EventCategoryResponse();
        response.setId(category.getId());
        response.setName(category.getCategoryName());
        response.setDescription(category.getDescription());
        response.setStatus(category.getIsActive() != null && category.getIsActive() ? (byte)1 : (byte)0);
        response.setCreatedAt(category.getCreatedAt());
        response.setUpdatedAt(category.getUpdatedAt());
        
        return response;
    }
}
