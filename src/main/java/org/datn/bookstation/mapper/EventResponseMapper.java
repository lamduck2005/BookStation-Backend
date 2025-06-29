package org.datn.bookstation.mapper;

import org.datn.bookstation.dto.response.EventResponse;
import org.datn.bookstation.entity.Event;
import org.datn.bookstation.entity.enums.EventStatus;
import org.datn.bookstation.entity.enums.EventType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class EventResponseMapper {
    public EventResponse toResponse(Event event) {
        if (event == null) return null;
        
        EventResponse response = new EventResponse();
        response.setId(event.getId());
        response.setName(event.getEventName());
        response.setDescription(event.getDescription());
        response.setCategoryId(event.getEventCategory() != null ? event.getEventCategory().getId() : null);
        response.setCategoryName(event.getEventCategory() != null ? event.getEventCategory().getCategoryName() : null);
        response.setEventType(event.getEventType() != null ? event.getEventType().name() : null);
        response.setEventTypeName(event.getEventType() != null ? getEventTypeDisplayName(event.getEventType()) : null);
        
        // Convert string to array for imageUrls and set backward compatible imageUrl
        List<String> imageUrls = new ArrayList<>();
        if (event.getImageUrl() != null && !event.getImageUrl().isEmpty()) {
            imageUrls = Arrays.asList(event.getImageUrl().split(","));
        }
        response.setImageUrls(imageUrls);
        response.setImageUrl(imageUrls.isEmpty() ? "" : imageUrls.get(0)); // First image for backward compatibility
        
        response.setStartDate(event.getStartDate());
        response.setEndDate(event.getEndDate());
        response.setMaxParticipants(event.getMaxParticipants());
        response.setCurrentParticipants(event.getCurrentParticipants());
        response.setStatus(event.getStatus() != null ? event.getStatus().name() : null); // Trả về string: "ONGOING", "COMPLETED"
        response.setStatusName(event.getStatus() != null ? getEventStatusDisplayName(event.getStatus()) : null);
        response.setLocation(event.getLocation()); // Địa điểm
        response.setIsOnline(event.getIsOnline()); // Có phải online không
        response.setRules(event.getRules()); // Quy định
        response.setCreatedAt(event.getCreatedAt());
        response.setUpdatedAt(event.getUpdatedAt());
        
        return response;
    }

    private String getEventTypeDisplayName(EventType eventType) {
        switch (eventType) {
            case BOOK_LAUNCH: return "Sự kiện ra mắt sách mới";
            case AUTHOR_MEET: return "Gặp gỡ tác giả";
            case READING_CHALLENGE: return "Thử thách đọc sách";
            case BOOK_FAIR: return "Hội chợ sách";
            case SEASONAL_EVENT: return "Sự kiện theo mùa";
            case PROMOTION: return "Sự kiện khuyến mãi";
            case CONTEST: return "Cuộc thi";
            case WORKSHOP: return "Hội thảo";
            case DAILY_CHECKIN: return "Điểm danh hàng ngày";
            case LOYALTY_PROGRAM: return "Chương trình khách hàng thân thiết";
            case POINT_EARNING: return "Sự kiện tích điểm";
            case OTHER: return "Khác";
            default: return eventType.name();
        }
    }

    private String getEventStatusDisplayName(EventStatus eventStatus) {
        switch (eventStatus) {
            case DRAFT: return "Bản nháp";
            case PUBLISHED: return "Đã công bố";
            case ONGOING: return "Đang diễn ra";
            case COMPLETED: return "Đã kết thúc";
            case CANCELLED: return "Đã hủy";
            default: return eventStatus.name();
        }
    }
}
