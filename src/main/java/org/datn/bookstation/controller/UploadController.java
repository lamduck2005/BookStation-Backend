package org.datn.bookstation.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.datn.bookstation.dto.request.DeleteImageRequest;
import org.datn.bookstation.dto.response.DeleteResponse;
import org.datn.bookstation.dto.response.MultipleUploadResponse;
import org.datn.bookstation.dto.response.SingleUploadResponse;
import org.datn.bookstation.exception.FileUploadException;
import org.datn.bookstation.service.FileUploadService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/upload")
@CrossOrigin(origins = "http://localhost:5173")
public class UploadController {

    private final FileUploadService fileUploadService;

    @PostMapping("/event-images")
    public ResponseEntity<?> uploadEventImages(@RequestParam("images") MultipartFile[] files) {
        try {
            List<String> urls = fileUploadService.saveEventImages(files);
            MultipleUploadResponse response = new MultipleUploadResponse(true, urls, "Upload successful");
            return ResponseEntity.ok(response);
            
        } catch (FileUploadException e) {
            log.error("Upload error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage(), e.getErrorCode()));
        } catch (Exception e) {
            log.error("Unexpected error during upload: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Internal server error", "INTERNAL_ERROR"));
        }
    }

    @PostMapping("/event-image")
    public ResponseEntity<?> uploadEventImage(@RequestParam("image") MultipartFile file) {
        try {
            String url = fileUploadService.saveEventImage(file);
            SingleUploadResponse response = new SingleUploadResponse(true, url, "Upload successful");
            return ResponseEntity.ok(response);
            
        } catch (FileUploadException e) {
            log.error("Upload error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage(), e.getErrorCode()));
        } catch (Exception e) {
            log.error("Unexpected error during upload: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Internal server error", "INTERNAL_ERROR"));
        }
    }

    @DeleteMapping("/event-image")
    public ResponseEntity<?> deleteEventImage(@RequestBody DeleteImageRequest request) {
        try {
            boolean deleted = fileUploadService.deleteImage(request.getImageUrl());
            if (deleted) {
                DeleteResponse response = new DeleteResponse(true, "Image deleted successfully");
                return ResponseEntity.ok(response);
            } else {
                DeleteResponse response = new DeleteResponse(false, "Image not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
        } catch (FileUploadException e) {
            log.error("Delete error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage(), e.getErrorCode()));
        } catch (Exception e) {
            log.error("Unexpected error during delete: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Internal server error", "INTERNAL_ERROR"));
        }
    }

    // Inner class for error responses
    private static class ErrorResponse {
        private final String error;
        private final String code;

        public ErrorResponse(String error, String code) {
            this.error = error;
            this.code = code;
        }

        public String getError() {
            return error;
        }

        public String getCode() {
            return code;
        }
    }
}
