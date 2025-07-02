package org.datn.bookstation.controller;

import lombok.AllArgsConstructor;
import org.datn.bookstation.dto.request.PublisherRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.dto.response.DropdownOptionResponse;
import org.datn.bookstation.service.PublisherService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller để quản lý Nhà xuất bản
 */
@RestController
@RequestMapping("/api/publishers")
@AllArgsConstructor
public class PublisherController {
    
    private final PublisherService publisherService;

    @GetMapping
    public ResponseEntity<ApiResponse<PaginationResponse<PublisherRequest>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String status) {
        PaginationResponse<PublisherRequest> publishers = publisherService.getAllWithPagination(page, size, name, email, status);
        ApiResponse<PaginationResponse<PublisherRequest>> response = new ApiResponse<>(HttpStatus.OK.value(), "Thành công", publishers);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<String>> add(@Valid @RequestBody PublisherRequest request) {
        try {
            publisherService.addPublisher(request);
            ApiResponse<String> response = new ApiResponse<>(201, "Tạo nhà xuất bản thành công", null);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>(500, "Lỗi khi tạo nhà xuất bản: " + e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> update(@PathVariable Integer id, @Valid @RequestBody PublisherRequest request) {
        try {
            request.setId(id); // Set ID for update
            publisherService.editPublisher(request);
            ApiResponse<String> response = new ApiResponse<>(200, "Cập nhật nhà xuất bản thành công", null);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            ApiResponse<String> response = new ApiResponse<>(404, e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>(500, "Lỗi khi cập nhật nhà xuất bản: " + e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> delete(@PathVariable Integer id) {
        try {
            publisherService.deletePublisher(id);
            ApiResponse<String> response = new ApiResponse<>(200, "Xóa nhà xuất bản thành công", null);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            ApiResponse<String> response = new ApiResponse<>(404, e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>(500, "Lỗi khi xóa nhà xuất bản: " + e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<String>> updateStatus(@PathVariable Integer id, 
                                                           @RequestParam byte status,
                                                           @RequestParam(defaultValue = "1") String updatedBy) {
        try {
            publisherService.upStatus(id, status, updatedBy);
            ApiResponse<String> response = new ApiResponse<>(200, "Cập nhật trạng thái thành công", null);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            ApiResponse<String> response = new ApiResponse<>(404, e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>(500, "Lỗi khi cập nhật trạng thái: " + e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/dropdown")
    public ResponseEntity<ApiResponse<List<DropdownOptionResponse>>> getDropdownPublishers() {
        List<DropdownOptionResponse> dropdown = publisherService.getActivePublishers().stream()
            .map(publisher -> new DropdownOptionResponse(publisher.getId(), publisher.getPublisherName()))
            .collect(Collectors.toList());
        ApiResponse<List<DropdownOptionResponse>> response = new ApiResponse<>(HttpStatus.OK.value(), "Lấy danh sách nhà xuất bản thành công", dropdown);
        return ResponseEntity.ok(response);
    }
}
