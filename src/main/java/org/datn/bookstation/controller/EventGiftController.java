package org.datn.bookstation.controller;

import lombok.AllArgsConstructor;
import org.datn.bookstation.dto.request.EventGiftRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.EventGiftResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.entity.EventGift;
import org.datn.bookstation.service.EventGiftService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/event-gifts")
public class EventGiftController {
    private final EventGiftService eventGiftService;

    @GetMapping
    public ResponseEntity<ApiResponse<PaginationResponse<EventGiftResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String giftName,
            @RequestParam(required = false) Integer eventId,
            @RequestParam(required = false) String giftType,
            @RequestParam(required = false) Boolean isActive) {
        PaginationResponse<EventGiftResponse> gifts = eventGiftService.getAllWithPagination(page, size, giftName, eventId, giftType, isActive);
        ApiResponse<PaginationResponse<EventGiftResponse>> response = new ApiResponse<>(HttpStatus.OK.value(), "Thành công", gifts);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EventGift>> getById(@PathVariable Integer id) {
        EventGift gift = eventGiftService.getById(id);
        if (gift == null) {
            ApiResponse<EventGift> response = new ApiResponse<>(HttpStatus.NOT_FOUND.value(), "Không tìm thấy", null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        ApiResponse<EventGift> response = new ApiResponse<>(HttpStatus.OK.value(), "Thành công", gift);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EventGift>> add(@RequestBody EventGiftRequest giftRequest) {
        ApiResponse<EventGift> response = eventGiftService.add(giftRequest);
        if (response.getStatus() == 404) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(404, "Không tìm thấy", null));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(201, "Tạo mới thành công", response.getData()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EventGift>> update(@PathVariable Integer id, @RequestBody EventGiftRequest giftRequest) {
        ApiResponse<EventGift> response = eventGiftService.update(giftRequest, id);
        if (response.getStatus() == 404) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(404, "Không tìm thấy", null));
        }
        return ResponseEntity.ok(new ApiResponse<>(200, "Cập nhật thành công", response.getData()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        eventGiftService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<ApiResponse<EventGift>> toggleStatus(@PathVariable Integer id) {
        ApiResponse<EventGift> response = eventGiftService.toggleStatus(id);
        if (response.getStatus() == 404) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(404, "Không tìm thấy", null));
        }
        return ResponseEntity.ok(new ApiResponse<>(200, "Cập nhật trạng thái thành công", response.getData()));
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<ApiResponse<List<EventGift>>> getByEventId(@PathVariable Integer eventId) {
        List<EventGift> gifts = eventGiftService.getByEventId(eventId);
        ApiResponse<List<EventGift>> response = new ApiResponse<>(HttpStatus.OK.value(), "Thành công", gifts);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/event/{eventId}/active")
    public ResponseEntity<ApiResponse<List<EventGift>>> getActiveByEventId(@PathVariable Integer eventId) {
        List<EventGift> gifts = eventGiftService.getActiveByEventId(eventId);
        ApiResponse<List<EventGift>> response = new ApiResponse<>(HttpStatus.OK.value(), "Thành công", gifts);
        return ResponseEntity.ok(response);
    }
}
