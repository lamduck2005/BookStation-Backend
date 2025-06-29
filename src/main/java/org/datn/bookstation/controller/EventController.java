package org.datn.bookstation.controller;

import lombok.AllArgsConstructor;
import org.datn.bookstation.dto.request.EventRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.EventResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.dto.response.DropdownOptionResponse;
import org.datn.bookstation.dto.response.EnumOptionResponse;
import org.datn.bookstation.entity.Event;
import org.datn.bookstation.entity.enums.EventStatus;
import org.datn.bookstation.entity.enums.EventType;
import org.datn.bookstation.mapper.EventResponseMapper;
import org.datn.bookstation.service.EventService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
@RequestMapping("/api/events")
public class EventController {
    private final EventService eventService;
    private final EventResponseMapper eventResponseMapper;

    @GetMapping
    public ResponseEntity<ApiResponse<PaginationResponse<EventResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) EventStatus status,
            @RequestParam(required = false) EventType eventType,
            @RequestParam(required = false) Long startDate,
            @RequestParam(required = false) Long endDate) {
        PaginationResponse<EventResponse> events = eventService.getAllWithPagination(page, size, name, categoryId, status, eventType, startDate, endDate);
        ApiResponse<PaginationResponse<EventResponse>> response = new ApiResponse<>(HttpStatus.OK.value(), "Thành công", events);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EventResponse>> getById(@PathVariable Integer id) {
        Event event = eventService.getById(id);
        if (event == null) {
            ApiResponse<EventResponse> response = new ApiResponse<>(HttpStatus.NOT_FOUND.value(), "Không tìm thấy", null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        // Sử dụng EventResponseMapper để có đầy đủ thông tin địa điểm, loại hình, quy định, trạng thái
        EventResponse eventResponse = eventResponseMapper.toResponse(event);
        ApiResponse<EventResponse> response = new ApiResponse<>(HttpStatus.OK.value(), "Thành công", eventResponse);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Event>> add(@RequestBody EventRequest eventRequest) {
        ApiResponse<Event> response = eventService.add(eventRequest);
        if (response.getStatus() == 404) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(404, response.getMessage(), null));
        }
        if (response.getStatus() == 400) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(400, response.getMessage(), null));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(201, "Tạo mới thành công", response.getData()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Event>> update(@PathVariable Integer id, @RequestBody EventRequest eventRequest) {
        ApiResponse<Event> response = eventService.update(eventRequest, id);
        if (response.getStatus() == 404) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(404, response.getMessage(), null));
        }
        if (response.getStatus() == 400) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(400, response.getMessage(), null));
        }
        return ResponseEntity.ok(new ApiResponse<>(200, "Cập nhật thành công", response.getData()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        eventService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<ApiResponse<Event>> toggleStatus(@PathVariable Integer id) {
        ApiResponse<Event> response = eventService.toggleStatus(id);
        if (response.getStatus() == 404) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(404, "Không tìm thấy", null));
        }
        return ResponseEntity.ok(new ApiResponse<>(200, "Cập nhật trạng thái thành công", response.getData()));
    }

    @GetMapping("/dropdown")
    public ResponseEntity<ApiResponse<List<DropdownOptionResponse>>> getDropdownEvents() {
        List<DropdownOptionResponse> dropdown = eventService.getActiveEvents().stream()
            .map(event -> new DropdownOptionResponse(event.getId(), event.getEventName()))
            .collect(Collectors.toList());
        ApiResponse<List<DropdownOptionResponse>> response = new ApiResponse<>(HttpStatus.OK.value(), "Lấy danh sách sự kiện thành công", dropdown);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<List<Event>>> getEventsByCategory(@PathVariable Integer categoryId) {
        List<Event> events = eventService.getEventsByCategory(categoryId);
        ApiResponse<List<Event>> response = new ApiResponse<>(HttpStatus.OK.value(), "Thành công", events);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/event-types")
    public ResponseEntity<ApiResponse<List<EnumOptionResponse>>> getEventTypes() {
        List<EnumOptionResponse> eventTypes = Arrays.stream(EventType.values())
            .map(type -> new EnumOptionResponse(type.name(), getEventTypeDisplayName(type)))
            .collect(Collectors.toList());
        ApiResponse<List<EnumOptionResponse>> response = new ApiResponse<>(
            HttpStatus.OK.value(), 
            "Lấy danh sách loại sự kiện thành công", 
            eventTypes
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/event-statuses")
    public ResponseEntity<ApiResponse<List<EnumOptionResponse>>> getEventStatuses() {
        List<EnumOptionResponse> eventStatuses = Arrays.stream(EventStatus.values())
            .map(status -> new EnumOptionResponse(status.name(), getEventStatusDisplayName(status)))
            .collect(Collectors.toList());
        ApiResponse<List<EnumOptionResponse>> response = new ApiResponse<>(
            HttpStatus.OK.value(), 
            "Lấy danh sách trạng thái sự kiện thành công", 
            eventStatuses
        );
        return ResponseEntity.ok(response);
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
