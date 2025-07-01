package org.datn.bookstation.controller;

import lombok.AllArgsConstructor;
import org.datn.bookstation.dto.request.EventCategoryRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.EventCategoryResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.dto.response.DropdownOptionResponse;
import org.datn.bookstation.entity.EventCategory;
import org.datn.bookstation.service.EventCategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
@RequestMapping("/api/event-categories")
public class EventCategoryController {
    private final EventCategoryService eventCategoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<PaginationResponse<EventCategoryResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean isActive) {
        PaginationResponse<EventCategoryResponse> categories = eventCategoryService.getAllWithPagination(page, size, name, isActive);
        ApiResponse<PaginationResponse<EventCategoryResponse>> response = new ApiResponse<>(HttpStatus.OK.value(), "Thành công", categories);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EventCategory>> getById(@PathVariable Integer id) {
        EventCategory category = eventCategoryService.getById(id);
        if (category == null) {
            ApiResponse<EventCategory> response = new ApiResponse<>(HttpStatus.NOT_FOUND.value(), "Không tìm thấy", null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        ApiResponse<EventCategory> response = new ApiResponse<>(HttpStatus.OK.value(), "Thành công", category);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EventCategory>> add(@RequestBody EventCategoryRequest categoryRequest) {
        ApiResponse<EventCategory> response = eventCategoryService.add(categoryRequest);
        if (response.getStatus() == 400) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(400, "Tên danh mục đã tồn tại", null));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(201, "Tạo mới thành công", response.getData()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EventCategory>> update(@PathVariable Integer id, @RequestBody EventCategoryRequest categoryRequest) {
        ApiResponse<EventCategory> response = eventCategoryService.update(categoryRequest, id);
        if (response.getStatus() == 404) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(404, "Không tìm thấy", null));
        }
        if (response.getStatus() == 400) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(400, "Dữ liệu không hợp lệ", null));
        }
        return ResponseEntity.ok(new ApiResponse<>(200, "Cập nhật thành công", response.getData()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        eventCategoryService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<ApiResponse<EventCategory>> toggleStatus(@PathVariable Integer id) {
        ApiResponse<EventCategory> response = eventCategoryService.toggleStatus(id);
        if (response.getStatus() == 404) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(404, "Không tìm thấy", null));
        }
        return ResponseEntity.ok(new ApiResponse<>(200, "Cập nhật trạng thái thành công", response.getData()));
    }

    @GetMapping("/dropdown")
    public ResponseEntity<ApiResponse<List<DropdownOptionResponse>>> getDropdownCategories() {
        List<DropdownOptionResponse> dropdown = eventCategoryService.getActiveCategories().stream()
            .map(category -> new DropdownOptionResponse(category.getId(), category.getCategoryName()))
            .collect(Collectors.toList());
        ApiResponse<List<DropdownOptionResponse>> response = new ApiResponse<>(HttpStatus.OK.value(), "Lấy danh sách danh mục thành công", dropdown);
        return ResponseEntity.ok(response);
    }
}
