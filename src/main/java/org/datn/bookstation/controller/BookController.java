package org.datn.bookstation.controller;

import lombok.AllArgsConstructor;
import org.datn.bookstation.dto.request.BookRequest;
import org.datn.bookstation.dto.request.TrendingRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.BookResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.dto.response.DropdownOptionResponse;
import org.datn.bookstation.dto.response.TrendingBookResponse;
import org.datn.bookstation.entity.Book;
import org.datn.bookstation.mapper.BookResponseMapper;
import org.datn.bookstation.service.BookService;
import org.datn.bookstation.util.DateTimeUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
@RequestMapping("/api/books")
public class BookController {
    
    private final BookService bookService;
    private final BookResponseMapper bookResponseMapper;

    @GetMapping
    public ResponseEntity<ApiResponse<PaginationResponse<BookResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String bookName,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) Integer supplierId,
            @RequestParam(required = false) Integer publisherId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Byte status,
            @RequestParam(required = false) String bookCode) {
        
        PaginationResponse<BookResponse> books = bookService.getAllWithPagination(
            page, size, bookName, categoryId, supplierId, publisherId, minPrice, maxPrice, status, bookCode);
        ApiResponse<PaginationResponse<BookResponse>> response = 
            new ApiResponse<>(HttpStatus.OK.value(), "Thành công", books);
        return ResponseEntity.ok(response);
    }

    /**
     * 🔥 API lấy danh sách sản phẩm xu hướng (POST)
     * Hỗ trợ 2 loại: DAILY_TRENDING và HOT_DISCOUNT
     * Tất cả parameters gửi trong request body để URL clean và dễ quản lý
     */
    @PostMapping("/trending")
    public ResponseEntity<ApiResponse<PaginationResponse<TrendingBookResponse>>> getTrendingBooks(
            @Valid @RequestBody TrendingRequest request) {
        
        PaginationResponse<TrendingBookResponse> trendingBooks = bookService.getTrendingBooks(request);
        
        String message = request.isDailyTrending() ? 
            "Lấy danh sách sản phẩm xu hướng theo ngày thành công" : 
            "Lấy danh sách sách hot giảm sốc thành công";
            
        ApiResponse<PaginationResponse<TrendingBookResponse>> response = 
            new ApiResponse<>(HttpStatus.OK.value(), message, trendingBooks);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookResponse>> getById(@PathVariable Integer id) {
        Book book = bookService.getById(id);
        if (book == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>(404, "Không tìm thấy sách", null));
        }
        
        BookResponse bookResponse = bookResponseMapper.toResponse(book);
        return ResponseEntity.ok(new ApiResponse<>(200, "Thành công", bookResponse));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BookResponse>> add(@Valid @RequestBody BookRequest bookRequest) {
        ApiResponse<Book> response = bookService.add(bookRequest);
        if (response.getStatus() == 404) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>(404, response.getMessage(), null));
        }
        if (response.getStatus() == 400) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(400, response.getMessage(), null));
        }
        
        BookResponse bookResponse = bookResponseMapper.toResponse(response.getData());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ApiResponse<>(201, "Tạo sách thành công", bookResponse));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BookResponse>> update(@PathVariable Integer id, 
                                                           @Valid @RequestBody BookRequest bookRequest) {
        ApiResponse<Book> response = bookService.update(bookRequest, id);
        if (response.getStatus() == 404) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>(404, response.getMessage(), null));
        }
        if (response.getStatus() == 400) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(400, response.getMessage(), null));
        }
        
        BookResponse bookResponse = bookResponseMapper.toResponse(response.getData());
        return ResponseEntity.ok(new ApiResponse<>(200, "Cập nhật sách thành công", bookResponse));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        bookService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<ApiResponse<BookResponse>> toggleStatus(@PathVariable Integer id) {
        ApiResponse<Book> response = bookService.toggleStatus(id);
        if (response.getStatus() == 404) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>(404, "Không tìm thấy sách", null));
        }
        
        BookResponse bookResponse = bookResponseMapper.toResponse(response.getData());
        return ResponseEntity.ok(new ApiResponse<>(200, "Cập nhật trạng thái thành công", bookResponse));
    }

    @GetMapping("/dropdown")
    public ResponseEntity<ApiResponse<List<DropdownOptionResponse>>> getDropdownBooks() {
        List<DropdownOptionResponse> dropdown = bookService.getActiveBooks().stream()
            .map(book -> new DropdownOptionResponse(book.getId(), book.getBookName()))
            .collect(Collectors.toList());
        ApiResponse<List<DropdownOptionResponse>> response = 
            new ApiResponse<>(HttpStatus.OK.value(), "Lấy danh sách sách thành công", dropdown);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<List<BookResponse>>> getBooksByCategory(@PathVariable Integer categoryId) {
        List<Book> books = bookService.getBooksByCategory(categoryId);
        List<BookResponse> bookResponses = books.stream()
            .map(bookResponseMapper::toResponse)
            .collect(Collectors.toList());
        ApiResponse<List<BookResponse>> response = 
            new ApiResponse<>(HttpStatus.OK.value(), "Thành công", bookResponses);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/supplier/{supplierId}")
    public ResponseEntity<ApiResponse<List<BookResponse>>> getBooksBySupplier(@PathVariable Integer supplierId) {
        List<Book> books = bookService.getBooksBySupplier(supplierId);
        List<BookResponse> bookResponses = books.stream()
            .map(bookResponseMapper::toResponse)
            .collect(Collectors.toList());
        ApiResponse<List<BookResponse>> response = 
            new ApiResponse<>(HttpStatus.OK.value(), "Thành công", bookResponses);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<BookResponse>>> getActiveBooks() {
        List<Book> books = bookService.getActiveBooks();
        List<BookResponse> bookResponses = books.stream()
            .map(bookResponseMapper::toResponse)
            .collect(Collectors.toList());
        ApiResponse<List<BookResponse>> response = 
            new ApiResponse<>(HttpStatus.OK.value(), "Thành công", bookResponses);
        return ResponseEntity.ok(response);
    }

    /**
     * Test endpoint để kiểm tra việc convert publicationDate
     * GET /api/books/test-publication-date
     */
    @GetMapping("/test-publication-date")
    public ResponseEntity<ApiResponse<Map<String, Object>>> testPublicationDate() {
        Map<String, Object> testData = new HashMap<>();
        
        // Test convert từ LocalDate sang timestamp
        LocalDate testDate = LocalDate.of(2010, 1, 1);
        Long timestamp = DateTimeUtil.dateToTimestamp(testDate);
        
        // Test convert từ timestamp về LocalDate
        LocalDate convertedBack = DateTimeUtil.timestampToDate(timestamp);
        
        testData.put("originalDate", testDate.toString());
        testData.put("timestamp", timestamp);
        testData.put("convertedBack", convertedBack.toString());
        testData.put("isEqual", testDate.equals(convertedBack));
        testData.put("currentTimestamp", DateTimeUtil.nowTimestamp());
        
        ApiResponse<Map<String, Object>> response = 
            new ApiResponse<>(HttpStatus.OK.value(), "Test publicationDate conversion thành công", testData);
        return ResponseEntity.ok(response);
    }

    /**
     * 🔥 ADMIN: Cache management endpoints
     */
    @GetMapping("/admin/cache/trending/stats")
    public ResponseEntity<ApiResponse<String>> getTrendingCacheStats() {
        // Note: Inject TrendingCacheService if needed
        ApiResponse<String> response = 
            new ApiResponse<>(HttpStatus.OK.value(), "Cache statistics", "Feature available when TrendingCacheService is injected");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/admin/cache/trending/invalidate")
    public ResponseEntity<ApiResponse<String>> invalidateTrendingCache() {
        // Note: Inject TrendingCacheService if needed
        ApiResponse<String> response = 
            new ApiResponse<>(HttpStatus.OK.value(), "Cache invalidated", "Feature available when TrendingCacheService is injected");
        return ResponseEntity.ok(response);
    }
}
