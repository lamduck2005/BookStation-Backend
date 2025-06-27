package org.datn.bookstation.controller;

import lombok.RequiredArgsConstructor;
import org.datn.bookstation.dto.request.EventHistoryRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.EventHistoryResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.service.EventHistoryService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

@RestController
@RequestMapping("/api/admin/event-history")
@RequiredArgsConstructor
@Validated
public class EventHistoryController {

    private final EventHistoryService eventHistoryService;

    @PostMapping
    public ResponseEntity<ApiResponse<EventHistoryResponse>> create(
            @Valid @RequestBody EventHistoryRequest request) {
        try {
            EventHistoryResponse response = eventHistoryService.create(request);
            ApiResponse<EventHistoryResponse> apiResponse = new ApiResponse<>(
                    HttpStatus.CREATED.value(), "Event history created successfully", response
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
        } catch (Exception e) {
            ApiResponse<EventHistoryResponse> apiResponse = new ApiResponse<>(
                    HttpStatus.BAD_REQUEST.value(), "Failed to create event history: " + e.getMessage(), null
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EventHistoryResponse>> findById(@PathVariable Integer id) {
        try {
            EventHistoryResponse response = eventHistoryService.findById(id);
            ApiResponse<EventHistoryResponse> apiResponse = new ApiResponse<>(
                    HttpStatus.OK.value(), "Event history retrieved successfully", response
            );
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            ApiResponse<EventHistoryResponse> apiResponse = new ApiResponse<>(
                    HttpStatus.NOT_FOUND.value(), "Event history not found: " + e.getMessage(), null
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiResponse);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EventHistoryResponse>> update(
            @PathVariable Integer id,
            @Valid @RequestBody EventHistoryRequest request) {
        try {
            EventHistoryResponse response = eventHistoryService.update(id, request);
            ApiResponse<EventHistoryResponse> apiResponse = new ApiResponse<>(
                    HttpStatus.OK.value(), "Event history updated successfully", response
            );
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            ApiResponse<EventHistoryResponse> apiResponse = new ApiResponse<>(
                    HttpStatus.BAD_REQUEST.value(), "Failed to update event history: " + e.getMessage(), null
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        try {
            eventHistoryService.delete(id);
            ApiResponse<Void> apiResponse = new ApiResponse<>(
                    HttpStatus.OK.value(), "Event history deleted successfully", null
            );
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            ApiResponse<Void> apiResponse = new ApiResponse<>(
                    HttpStatus.NOT_FOUND.value(), "Failed to delete event history: " + e.getMessage(), null
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiResponse);
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PaginationResponse<EventHistoryResponse>>> findAll(
            @RequestParam(required = false) Integer eventId,
            @RequestParam(required = false) String actionType,
            @RequestParam(required = false) Integer performedBy,
            @RequestParam(required = false) Long createdStartTime,
            @RequestParam(required = false) Long createdEndTime,
            @RequestParam(required = false) String descriptionKeyword,
            @RequestParam(required = false) String eventName,
            @RequestParam(required = false) String performedByName,
            @RequestParam(required = false) String[] actionTypes,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        try {
            Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
            Pageable pageable = PageRequest.of(page, size, sort);

            PaginationResponse<EventHistoryResponse> response = eventHistoryService.findAll(
                    eventId, actionType, performedBy, createdStartTime, createdEndTime,
                    descriptionKeyword, eventName, performedByName, actionTypes, pageable);

            ApiResponse<PaginationResponse<EventHistoryResponse>> apiResponse = new ApiResponse<>(
                    HttpStatus.OK.value(), "Event history retrieved successfully", response
            );
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            ApiResponse<PaginationResponse<EventHistoryResponse>> apiResponse = new ApiResponse<>(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to retrieve event history: " + e.getMessage(), null
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    // Business logic endpoints
    @PostMapping("/log-action")
    public ResponseEntity<ApiResponse<EventHistoryResponse>> logAction(
            @RequestParam Integer eventId,
            @RequestParam String actionType,
            @RequestParam String description,
            @RequestParam(required = false) Integer performedBy) {
        try {
            EventHistoryResponse response = eventHistoryService.logAction(eventId, actionType, description, performedBy);
            ApiResponse<EventHistoryResponse> apiResponse = new ApiResponse<>(
                    HttpStatus.CREATED.value(), "Action logged successfully", response
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
        } catch (Exception e) {
            ApiResponse<EventHistoryResponse> apiResponse = new ApiResponse<>(
                    HttpStatus.BAD_REQUEST.value(), "Failed to log action: " + e.getMessage(), null
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
        }
    }

    @PostMapping("/log-action-with-values")
    public ResponseEntity<ApiResponse<EventHistoryResponse>> logActionWithValues(
            @RequestParam Integer eventId,
            @RequestParam String actionType,
            @RequestParam String description,
            @RequestParam(required = false) Integer performedBy,
            @RequestParam(required = false) String oldValues,
            @RequestParam(required = false) String newValues) {
        try {
            EventHistoryResponse response = eventHistoryService.logActionWithValues(
                    eventId, actionType, description, performedBy, oldValues, newValues);
            ApiResponse<EventHistoryResponse> apiResponse = new ApiResponse<>(
                    HttpStatus.CREATED.value(), "Action with values logged successfully", response
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
        } catch (Exception e) {
            ApiResponse<EventHistoryResponse> apiResponse = new ApiResponse<>(
                    HttpStatus.BAD_REQUEST.value(), "Failed to log action with values: " + e.getMessage(), null
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
        }
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<ApiResponse<PaginationResponse<EventHistoryResponse>>> findHistoryByEvent(
            @PathVariable Integer eventId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
            PaginationResponse<EventHistoryResponse> response = eventHistoryService.findHistoryByEvent(eventId, pageable);
            
            ApiResponse<PaginationResponse<EventHistoryResponse>> apiResponse = new ApiResponse<>(
                    HttpStatus.OK.value(), "Event history retrieved successfully", response
            );
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            ApiResponse<PaginationResponse<EventHistoryResponse>> apiResponse = new ApiResponse<>(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to retrieve event history: " + e.getMessage(), null
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<PaginationResponse<EventHistoryResponse>>> findHistoryByUser(
            @PathVariable Integer userId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
            PaginationResponse<EventHistoryResponse> response = eventHistoryService.findHistoryByUser(userId, pageable);
            
            ApiResponse<PaginationResponse<EventHistoryResponse>> apiResponse = new ApiResponse<>(
                    HttpStatus.OK.value(), "User history retrieved successfully", response
            );
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            ApiResponse<PaginationResponse<EventHistoryResponse>> apiResponse = new ApiResponse<>(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to retrieve user history: " + e.getMessage(), null
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<PaginationResponse<EventHistoryResponse>>> findRecentHistory(
            @RequestParam(defaultValue = "20") Integer limit,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            PaginationResponse<EventHistoryResponse> response = eventHistoryService.findRecentHistory(limit, pageable);
            
            ApiResponse<PaginationResponse<EventHistoryResponse>> apiResponse = new ApiResponse<>(
                    HttpStatus.OK.value(), "Recent history retrieved successfully", response
            );
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            ApiResponse<PaginationResponse<EventHistoryResponse>> apiResponse = new ApiResponse<>(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to retrieve recent history: " + e.getMessage(), null
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }
}
