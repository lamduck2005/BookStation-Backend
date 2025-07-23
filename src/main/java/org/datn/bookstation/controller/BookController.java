package org.datn.bookstation.controller;

import lombok.AllArgsConstructor;
import org.datn.bookstation.dto.request.BookCategoryRequest;
import org.datn.bookstation.dto.request.BookRequest;
import org.datn.bookstation.dto.request.FlashSaleItemBookRequest;
import org.datn.bookstation.dto.request.TrendingRequest;
import org.datn.bookstation.dto.request.QuantityValidationRequest;
import org.datn.bookstation.dto.request.BookPriceCalculationRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.BookDetailResponse;
import org.datn.bookstation.dto.response.BookResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.dto.response.DropdownOptionResponse;
import org.datn.bookstation.dto.response.TrendingBookResponse;
import org.datn.bookstation.dto.response.QuantityValidationResponse;
import org.datn.bookstation.dto.response.BookPriceCalculationResponse;
import org.datn.bookstation.entity.Book;
import org.datn.bookstation.entity.FlashSaleItem;
import org.datn.bookstation.mapper.BookResponseMapper;
import org.datn.bookstation.mapper.BookDetailResponseMapper;
import org.datn.bookstation.service.BookService;
import org.datn.bookstation.service.TrendingCacheService;
import org.datn.bookstation.service.FlashSaleItemService;
import org.datn.bookstation.repository.FlashSaleItemRepository;
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
    private final BookDetailResponseMapper bookDetailResponseMapper;
    private final TrendingCacheService trendingCacheService;
    private final FlashSaleItemService flashSaleItemService;
    private final FlashSaleItemRepository flashSaleItemRepository;
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
    @GetMapping("/client")
    public ResponseEntity<ApiResponse<PaginationResponse<BookResponse>>> getAllClient(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String bookName,
            @RequestParam(required = false) Integer parentCategoryId,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) Integer publisherId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {

        PaginationResponse<BookResponse> books = bookService.getAllWithPagination(
                page, size, bookName,parentCategoryId, categoryId, publisherId, minPrice, maxPrice);
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
        // Bỏ toàn bộ filter, chỉ giữ lại type, page, size
        TrendingRequest cleanRequest = new TrendingRequest();
        cleanRequest.setType(request.getType());
        cleanRequest.setPage(request.getPage());
        cleanRequest.setSize(request.getSize());
        // Các trường filter khác sẽ bị bỏ qua

        PaginationResponse<TrendingBookResponse> trendingBooks = bookService.getTrendingBooks(cleanRequest);
        String message = cleanRequest.isDailyTrending() ?
            "Lấy danh sách sản phẩm xu hướng theo ngày thành công" :
            "Lấy danh sách sách hot giảm sốc thành công";
        ApiResponse<PaginationResponse<TrendingBookResponse>> response =
            new ApiResponse<>(HttpStatus.OK.value(), message, trendingBooks);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookDetailResponse>> getById(@PathVariable Integer id) {
        Book book = bookService.getById(id);
        if (book == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>(404, "Không tìm thấy sách", null));
        }
        
        BookDetailResponse bookDetailResponse = bookDetailResponseMapper.toDetailResponse(book);
        return ResponseEntity.ok(new ApiResponse<>(200, "Thành công", bookDetailResponse));
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
            .map(this::mapToDropdownResponse)
            .collect(Collectors.toList());
        
        ApiResponse<List<DropdownOptionResponse>> response = 
            new ApiResponse<>(HttpStatus.OK.value(), "Lấy danh sách sách thành công", dropdown);
        return ResponseEntity.ok(response);
    }

    /**
     * API validate số lượng sản phẩm khi đặt hàng
     * POST /api/books/validate-quantity
     * Đã cải tiến để hỗ trợ validate flash sale items
     */
    @PostMapping("/validate-quantity")
    public ResponseEntity<ApiResponse<QuantityValidationResponse>> validateQuantity(
            @Valid @RequestBody QuantityValidationRequest request) {
        
        Book book = bookService.getById(request.getBookId());
        if (book == null) {
            QuantityValidationResponse response = QuantityValidationResponse
                .failure("Không tìm thấy sách", 0);
            return ResponseEntity.ok(new ApiResponse<>(200, "Validate thất bại", response));
        }
        
        // Kiểm tra xem sách có đang trong flash sale không
        FlashSaleItem activeFlashSale = flashSaleItemRepository.findActiveFlashSaleByBook(book.getId());
        
        if (activeFlashSale != null) {
            // Nếu là flash sale, validate theo flash sale stock và giới hạn mua
            int flashSaleStock = activeFlashSale.getStockQuantity();
            Integer maxPurchasePerUser = activeFlashSale.getMaxPurchasePerUser();
            
            // Validate số lượng không vượt quá stock flash sale
            if (request.getQuantity() > flashSaleStock) {
                QuantityValidationResponse response = QuantityValidationResponse.flashSaleFailure(
                    "Flash sale chỉ còn " + flashSaleStock + " sản phẩm", 
                    book.getStockQuantity(), flashSaleStock, maxPurchasePerUser);
                return ResponseEntity.ok(new ApiResponse<>(200, "Validate flash sale thất bại", response));
            }
            
            // Validate giới hạn mua per user (nếu có)
            if (maxPurchasePerUser != null && request.getQuantity() > maxPurchasePerUser) {
                QuantityValidationResponse response = QuantityValidationResponse.flashSaleFailure(
                    "Mỗi khách hàng chỉ được mua tối đa " + maxPurchasePerUser + " sản phẩm flash sale", 
                    book.getStockQuantity(), flashSaleStock, maxPurchasePerUser);
                return ResponseEntity.ok(new ApiResponse<>(200, "Validate giới hạn mua thất bại", response));
            }
            
            // Flash sale thành công
            QuantityValidationResponse response = QuantityValidationResponse.flashSaleSuccess(
                book.getStockQuantity(), flashSaleStock, maxPurchasePerUser);
            response.setMessage("Có thể mua " + request.getQuantity() + " sản phẩm với giá flash sale");
            return ResponseEntity.ok(new ApiResponse<>(200, "Validate flash sale thành công", response));
            
        } else {
            // Không phải flash sale, validate theo stock thông thường
            int availableQuantity = book.getStockQuantity();
            boolean isValid = request.getQuantity() > 0 && request.getQuantity() <= availableQuantity;
            
            QuantityValidationResponse response = isValid 
                ? QuantityValidationResponse.success(availableQuantity)
                : QuantityValidationResponse.failure(
                    "Số lượng không hợp lệ, tồn kho hiện tại: " + availableQuantity, 
                    availableQuantity);
            
            return ResponseEntity.ok(new ApiResponse<>(200, "Validate thành công", response));
        }
    }

    /**
     * Helper method để map Book entity sang DropdownOptionResponse
     */
    private DropdownOptionResponse mapToDropdownResponse(Book book) {
        // Tính giá bình thường (ưu tiên discount nếu có)
        BigDecimal normalPrice = calculateNormalPrice(book);
        
        // Kiểm tra flash sale
        FlashSaleItem flashSale = flashSaleItemRepository.findActiveFlashSaleByBook(book.getId());
        BigDecimal flashSalePrice = null;
        boolean isFlashSale = false;
        
        if (flashSale != null) {
            flashSalePrice = flashSale.getDiscountPrice();
            isFlashSale = true;
        }
        
        return new DropdownOptionResponse(
            book.getId(),
            book.getBookName(),
            normalPrice,
            flashSalePrice,
            isFlashSale
        );
    }

    /**
     * Helper method để tính giá bình thường (đã bao gồm discount nếu có)
     */
    private BigDecimal calculateNormalPrice(Book book) {
        if (book.getDiscountActive() != null && book.getDiscountActive()) {
            if (book.getDiscountValue() != null) {
                return book.getPrice().subtract(book.getDiscountValue());
            } else if (book.getDiscountPercent() != null) {
                BigDecimal discountAmount = book.getPrice()
                    .multiply(BigDecimal.valueOf(book.getDiscountPercent()))
                    .divide(BigDecimal.valueOf(100));
                return book.getPrice().subtract(discountAmount);
            }
        }
        return book.getPrice();
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
        String stats = trendingCacheService.getCacheStatistics();
        ApiResponse<String> response = 
            new ApiResponse<>(HttpStatus.OK.value(), "Cache statistics", stats);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/admin/cache/trending/invalidate")
    public ResponseEntity<ApiResponse<String>> invalidateTrendingCache() {
        trendingCacheService.invalidateAllTrendingCache();
        ApiResponse<String> response = 
            new ApiResponse<>(HttpStatus.OK.value(), "Cache invalidated successfully", "All trending cache has been cleared");
        return ResponseEntity.ok(response);
    }
    @GetMapping("/bycategoryid/{id}")
    public ResponseEntity<ApiResponse<List<BookCategoryRequest>>> bookByCategoryId(
            @PathVariable("id") Integer id,
            @RequestParam(name = "text", required = false) String text) {
        if (id==null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(bookService.getBooksByCategoryId(id, text));
    }
    @GetMapping("/flashsalebook")
    public ResponseEntity<ApiResponse<List<FlashSaleItemBookRequest>>> findAllBooksInActiveFlashSale(){
        return ResponseEntity.ok(flashSaleItemService.findAllBooksInActiveFlashSale());
    }

    /**
     * 🔥 API tính giá sách cho Frontend
     * POST /api/books/calculate-price
     */
    @PostMapping("/calculate-price")
    public ResponseEntity<ApiResponse<BookPriceCalculationResponse>> calculateBookPrice(
            @Valid @RequestBody BookPriceCalculationRequest request) {
        
        Book book = bookService.getById(request.getBookId());
        if (book == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>(404, "Không tìm thấy sách", null));
        }
        
        BookPriceCalculationResponse response = bookService.calculateBookPrice(book, request);
        return ResponseEntity.ok(new ApiResponse<>(200, "Tính giá thành công", response));
    }
}

