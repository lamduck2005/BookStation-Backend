package org.datn.bookstation.configuration;

import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.exception.FileUploadException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        ApiResponse<Map<String, String>> response = new ApiResponse<>(
            HttpStatus.BAD_REQUEST.value(),
            "Dữ liệu không hợp lệ",
            errors
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<String>> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex) {
        
        String message = "Dữ liệu JSON không hợp lệ";
        
        // Kiểm tra lỗi enum
        if (ex.getMessage().contains("Cannot deserialize value")) {
            if (ex.getMessage().contains("EventType")) {
                message = "Giá trị eventType không hợp lệ. Các giá trị cho phép: BOOK_LAUNCH, AUTHOR_MEET, READING_CHALLENGE, BOOK_FAIR, SEASONAL_EVENT, PROMOTION, CONTEST, WORKSHOP, DAILY_CHECKIN, LOYALTY_PROGRAM, POINT_EARNING, OTHER";
            } else if (ex.getMessage().contains("EventStatus")) {
                message = "Giá trị status không hợp lệ. Các giá trị cho phép: DRAFT, PUBLISHED, ONGOING, COMPLETED, CANCELLED";
            }
        }
        
        ApiResponse<String> response = new ApiResponse<>(
            HttpStatus.BAD_REQUEST.value(),
            message,
            null
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleGenericException(Exception ex) {
        ApiResponse<String> response = new ApiResponse<>(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Đã xảy ra lỗi hệ thống: " + ex.getMessage(),
            null
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(FileUploadException.class)
    public ResponseEntity<ApiResponse<String>> handleFileUploadException(FileUploadException ex) {
        ApiResponse<String> response = new ApiResponse<>(
            HttpStatus.BAD_REQUEST.value(),
            ex.getMessage(),
            null
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<String>> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
        ApiResponse<String> response = new ApiResponse<>(
            HttpStatus.BAD_REQUEST.value(),
            "File quá lớn. Kích thước tối đa là 5MB cho mỗi file.",
            null
        );
        return ResponseEntity.badRequest().body(response);
    }
}
