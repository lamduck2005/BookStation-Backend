package org.datn.bookstation.controller;

import lombok.RequiredArgsConstructor;
import org.datn.bookstation.dto.request.EventParticipantRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.EventParticipantResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.entity.EventParticipant;
import org.datn.bookstation.entity.enums.ParticipantStatus;
import org.datn.bookstation.service.EventParticipantService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import java.util.List;

@RestController
@RequestMapping("/api/admin/event-participants")
@RequiredArgsConstructor
@Validated
public class EventParticipantController {

    private final EventParticipantService eventParticipantService;

    @PostMapping
    public ResponseEntity<ApiResponse<EventParticipant>> create(
            @Valid @RequestBody EventParticipantRequest request) {
        ApiResponse<EventParticipant> response = eventParticipantService.add(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EventParticipant>> findById(@PathVariable Integer id) {
        try {
            EventParticipant participant = eventParticipantService.getById(id);
            if (participant == null) {
                ApiResponse<EventParticipant> apiResponse = new ApiResponse<>(
                        HttpStatus.NOT_FOUND.value(), "Event participant not found", null
                );
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiResponse);
            }
            ApiResponse<EventParticipant> apiResponse = new ApiResponse<>(
                    HttpStatus.OK.value(), "Event participant retrieved successfully", participant
            );
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            ApiResponse<EventParticipant> apiResponse = new ApiResponse<>(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error retrieving participant: " + e.getMessage(), null
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EventParticipant>> update(
            @PathVariable Integer id,
            @Valid @RequestBody EventParticipantRequest request) {
        ApiResponse<EventParticipant> response = eventParticipantService.update(request, id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        try {
            eventParticipantService.delete(id);
            ApiResponse<Void> apiResponse = new ApiResponse<>(
                    HttpStatus.OK.value(), "Event participant deleted successfully", null
            );
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            ApiResponse<Void> apiResponse = new ApiResponse<>(
                    HttpStatus.NOT_FOUND.value(), "Failed to delete event participant: " + e.getMessage(), null
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiResponse);
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PaginationResponse<EventParticipantResponse>>> findAll(
            @RequestParam(required = false) Integer eventId,
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) String participationStatus,
            @RequestParam(required = false) Boolean isWinner,
            @RequestParam(required = false) String userEmail,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {

        try {
            ParticipantStatus status = null;
            if (participationStatus != null) {
                try {
                    status = ParticipantStatus.valueOf(participationStatus.toUpperCase());
                } catch (IllegalArgumentException e) {
                    // Invalid status, ignore
                }
            }

            PaginationResponse<EventParticipantResponse> response = eventParticipantService.getAllWithPagination(
                    page, size, eventId, userId, status, isWinner, userEmail);

            ApiResponse<PaginationResponse<EventParticipantResponse>> apiResponse = new ApiResponse<>(
                    HttpStatus.OK.value(), "Event participants retrieved successfully", response
            );
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            ApiResponse<PaginationResponse<EventParticipantResponse>> apiResponse = new ApiResponse<>(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to retrieve event participants: " + e.getMessage(), null
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    // Business logic endpoints
    @PostMapping("/{eventId}/join")
    public ResponseEntity<ApiResponse<EventParticipant>> joinEvent(
            @PathVariable Integer eventId,
            @RequestParam Integer userId) {
        ApiResponse<EventParticipant> response = eventParticipantService.joinEvent(eventId, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{participantId}/complete")
    public ResponseEntity<ApiResponse<EventParticipant>> completeParticipation(
            @PathVariable Integer participantId) {
        ApiResponse<EventParticipant> response = eventParticipantService.completeTask(participantId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<ApiResponse<List<EventParticipant>>> findByEvent(
            @PathVariable Integer eventId) {
        try {
            List<EventParticipant> participants = eventParticipantService.getByEventId(eventId);
            ApiResponse<List<EventParticipant>> apiResponse = new ApiResponse<>(
                    HttpStatus.OK.value(), "Event participants retrieved successfully", participants
            );
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            ApiResponse<List<EventParticipant>> apiResponse = new ApiResponse<>(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to retrieve participants: " + e.getMessage(), null
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<EventParticipant>>> findByUser(
            @PathVariable Integer userId) {
        try {
            List<EventParticipant> participants = eventParticipantService.getByUserId(userId);
            ApiResponse<List<EventParticipant>> apiResponse = new ApiResponse<>(
                    HttpStatus.OK.value(), "User participations retrieved successfully", participants
            );
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            ApiResponse<List<EventParticipant>> apiResponse = new ApiResponse<>(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to retrieve participations: " + e.getMessage(), null
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }
}
