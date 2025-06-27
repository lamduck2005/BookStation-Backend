package org.datn.bookstation.controller;

import lombok.RequiredArgsConstructor;
import org.datn.bookstation.dto.request.EventGiftClaimRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.EventGiftClaimResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.service.EventGiftClaimService;
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
@RequestMapping("/api/admin/event-gift-claims")
@RequiredArgsConstructor
@Validated
public class EventGiftClaimController {

    private final EventGiftClaimService eventGiftClaimService;

    @PostMapping
    public ResponseEntity<ApiResponse<EventGiftClaimResponse>> create(
            @Valid @RequestBody EventGiftClaimRequest request) {
        try {
            EventGiftClaimResponse response = eventGiftClaimService.create(request);
            ApiResponse<EventGiftClaimResponse> apiResponse = new ApiResponse<>(
                    HttpStatus.CREATED.value(), "Event gift claim created successfully", response
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
        } catch (Exception e) {
            ApiResponse<EventGiftClaimResponse> apiResponse = new ApiResponse<>(
                    HttpStatus.BAD_REQUEST.value(), "Failed to create event gift claim: " + e.getMessage(), null
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EventGiftClaimResponse>> findById(@PathVariable Integer id) {
        try {
            EventGiftClaimResponse response = eventGiftClaimService.findById(id);
            ApiResponse<EventGiftClaimResponse> apiResponse = new ApiResponse<>(
                    HttpStatus.OK.value(), "Event gift claim retrieved successfully", response
            );
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            ApiResponse<EventGiftClaimResponse> apiResponse = new ApiResponse<>(
                    HttpStatus.NOT_FOUND.value(), "Event gift claim not found: " + e.getMessage(), null
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiResponse);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EventGiftClaimResponse>> update(
            @PathVariable Integer id,
            @Valid @RequestBody EventGiftClaimRequest request) {
        try {
            EventGiftClaimResponse response = eventGiftClaimService.update(id, request);
            ApiResponse<EventGiftClaimResponse> apiResponse = new ApiResponse<>(
                    HttpStatus.OK.value(), "Event gift claim updated successfully", response
            );
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            ApiResponse<EventGiftClaimResponse> apiResponse = new ApiResponse<>(
                    HttpStatus.BAD_REQUEST.value(), "Failed to update event gift claim: " + e.getMessage(), null
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        try {
            eventGiftClaimService.delete(id);
            ApiResponse<Void> apiResponse = new ApiResponse<>(
                    HttpStatus.OK.value(), "Event gift claim deleted successfully", null
            );
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            ApiResponse<Void> apiResponse = new ApiResponse<>(
                    HttpStatus.NOT_FOUND.value(), "Failed to delete event gift claim: " + e.getMessage(), null
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiResponse);
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PaginationResponse<EventGiftClaimResponse>>> findAll(
            @RequestParam(required = false) Integer eventId,
            @RequestParam(required = false) Integer eventParticipantId,
            @RequestParam(required = false) Integer eventGiftId,
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) String claimStatus,
            @RequestParam(required = false) String deliveryMethod,
            @RequestParam(required = false) String giftType,
            @RequestParam(required = false) Boolean autoDelivered,
            @RequestParam(required = false) Long claimedStartTime,
            @RequestParam(required = false) Long claimedEndTime,
            @RequestParam(required = false) Long completedStartTime,
            @RequestParam(required = false) Long completedEndTime,
            @RequestParam(required = false) Integer pickupStoreId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        try {
            Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
            Pageable pageable = PageRequest.of(page, size, sort);

            PaginationResponse<EventGiftClaimResponse> response = eventGiftClaimService.findAll(
                    eventId, eventParticipantId, eventGiftId, userId, claimStatus, deliveryMethod,
                    giftType, autoDelivered, claimedStartTime, claimedEndTime, completedStartTime,
                    completedEndTime, pickupStoreId, pageable);

            ApiResponse<PaginationResponse<EventGiftClaimResponse>> apiResponse = new ApiResponse<>(
                    HttpStatus.OK.value(), "Event gift claims retrieved successfully", response
            );
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            ApiResponse<PaginationResponse<EventGiftClaimResponse>> apiResponse = new ApiResponse<>(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to retrieve event gift claims: " + e.getMessage(), null
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    // Business logic endpoints
    @PostMapping("/claim-gift")
    public ResponseEntity<ApiResponse<EventGiftClaimResponse>> claimGift(
            @RequestParam Integer eventParticipantId,
            @RequestParam Integer eventGiftId,
            @RequestBody(required = false) EventGiftClaimRequest request) {
        try {
            if (request == null) {
                request = new EventGiftClaimRequest();
                request.setEventParticipantId(eventParticipantId);
                request.setEventGiftId(eventGiftId);
            }
            
            EventGiftClaimResponse response = eventGiftClaimService.claimGift(eventParticipantId, eventGiftId, request);
            ApiResponse<EventGiftClaimResponse> apiResponse = new ApiResponse<>(
                    HttpStatus.CREATED.value(), "Gift claimed successfully", response
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
        } catch (Exception e) {
            ApiResponse<EventGiftClaimResponse> apiResponse = new ApiResponse<>(
                    HttpStatus.BAD_REQUEST.value(), "Failed to claim gift: " + e.getMessage(), null
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
        }
    }

    @PostMapping("/{claimId}/approve")
    public ResponseEntity<ApiResponse<EventGiftClaimResponse>> approveClaim(
            @PathVariable Integer claimId,
            @RequestParam(required = false) String adminNotes) {
        try {
            EventGiftClaimResponse response = eventGiftClaimService.approveClaimByAdmin(claimId, adminNotes);
            ApiResponse<EventGiftClaimResponse> apiResponse = new ApiResponse<>(
                    HttpStatus.OK.value(), "Claim approved successfully", response
            );
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            ApiResponse<EventGiftClaimResponse> apiResponse = new ApiResponse<>(
                    HttpStatus.BAD_REQUEST.value(), "Failed to approve claim: " + e.getMessage(), null
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
        }
    }

    @PostMapping("/{claimId}/reject")
    public ResponseEntity<ApiResponse<EventGiftClaimResponse>> rejectClaim(
            @PathVariable Integer claimId,
            @RequestParam(required = false) String adminNotes) {
        try {
            EventGiftClaimResponse response = eventGiftClaimService.rejectClaimByAdmin(claimId, adminNotes);
            ApiResponse<EventGiftClaimResponse> apiResponse = new ApiResponse<>(
                    HttpStatus.OK.value(), "Claim rejected successfully", response
            );
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            ApiResponse<EventGiftClaimResponse> apiResponse = new ApiResponse<>(
                    HttpStatus.BAD_REQUEST.value(), "Failed to reject claim: " + e.getMessage(), null
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
        }
    }

    @PostMapping("/{claimId}/confirm-delivery")
    public ResponseEntity<ApiResponse<EventGiftClaimResponse>> confirmDelivery(
            @PathVariable Integer claimId,
            @RequestParam Integer staffId,
            @RequestParam(required = false) String notes) {
        try {
            EventGiftClaimResponse response = eventGiftClaimService.confirmDeliveryByStaff(claimId, staffId, notes);
            ApiResponse<EventGiftClaimResponse> apiResponse = new ApiResponse<>(
                    HttpStatus.OK.value(), "Delivery confirmed successfully", response
            );
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            ApiResponse<EventGiftClaimResponse> apiResponse = new ApiResponse<>(
                    HttpStatus.BAD_REQUEST.value(), "Failed to confirm delivery: " + e.getMessage(), null
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
        }
    }

    @PostMapping("/{claimId}/mark-auto-delivered")
    public ResponseEntity<ApiResponse<EventGiftClaimResponse>> markAutoDelivered(
            @PathVariable Integer claimId) {
        try {
            EventGiftClaimResponse response = eventGiftClaimService.markAsAutoDelivered(claimId);
            ApiResponse<EventGiftClaimResponse> apiResponse = new ApiResponse<>(
                    HttpStatus.OK.value(), "Marked as auto-delivered successfully", response
            );
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            ApiResponse<EventGiftClaimResponse> apiResponse = new ApiResponse<>(
                    HttpStatus.BAD_REQUEST.value(), "Failed to mark as auto-delivered: " + e.getMessage(), null
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
        }
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<PaginationResponse<EventGiftClaimResponse>>> findPendingClaims(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
            PaginationResponse<EventGiftClaimResponse> response = eventGiftClaimService.findPendingClaims(pageable);
            
            ApiResponse<PaginationResponse<EventGiftClaimResponse>> apiResponse = new ApiResponse<>(
                    HttpStatus.OK.value(), "Pending claims retrieved successfully", response
            );
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            ApiResponse<PaginationResponse<EventGiftClaimResponse>> apiResponse = new ApiResponse<>(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to retrieve pending claims: " + e.getMessage(), null
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<PaginationResponse<EventGiftClaimResponse>>> findClaimsByUser(
            @PathVariable Integer userId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
            PaginationResponse<EventGiftClaimResponse> response = eventGiftClaimService.findClaimsByUser(userId, pageable);
            
            ApiResponse<PaginationResponse<EventGiftClaimResponse>> apiResponse = new ApiResponse<>(
                    HttpStatus.OK.value(), "User claims retrieved successfully", response
            );
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            ApiResponse<PaginationResponse<EventGiftClaimResponse>> apiResponse = new ApiResponse<>(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to retrieve user claims: " + e.getMessage(), null
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<ApiResponse<PaginationResponse<EventGiftClaimResponse>>> findClaimsByEvent(
            @PathVariable Integer eventId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
            PaginationResponse<EventGiftClaimResponse> response = eventGiftClaimService.findClaimsByEvent(eventId, pageable);
            
            ApiResponse<PaginationResponse<EventGiftClaimResponse>> apiResponse = new ApiResponse<>(
                    HttpStatus.OK.value(), "Event claims retrieved successfully", response
            );
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            ApiResponse<PaginationResponse<EventGiftClaimResponse>> apiResponse = new ApiResponse<>(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to retrieve event claims: " + e.getMessage(), null
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }
}
