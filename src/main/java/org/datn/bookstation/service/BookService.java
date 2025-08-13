package org.datn.bookstation.service;

import org.datn.bookstation.dto.request.*;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.BookResponse;
import org.datn.bookstation.dto.response.BookStockResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.dto.response.PosBookItemResponse;
import org.datn.bookstation.dto.response.TopBookSoldResponse;
import org.datn.bookstation.dto.response.TrendingBookResponse;
import org.datn.bookstation.dto.response.BookPriceCalculationResponse;
import org.datn.bookstation.entity.Book;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

public interface BookService {
        PaginationResponse<BookResponse> getAllWithPagination(int page, int size, String bookName,
                        Integer categoryId, Integer supplierId, Integer publisherId,
                        BigDecimal minPrice, BigDecimal maxPrice,
                        Byte status, String bookCode);

        PaginationResponse<FlashSaleItemBookRequest> getAllWithPagination(int page, int size, String bookName,
                        Integer parentId, Integer categoryId, List<Integer> authorId, Integer publisherId,
                        BigDecimal minPrice,
                        BigDecimal maxPrice);

        List<Book> getAll();

        List<Book> getActiveBooks();

        List<Book> getBooksByCategory(Integer categoryId);

        List<Book> getBooksBySupplier(Integer supplierId);

        List<Book> getBooksByPublisher(Integer publisherId);

        Book getById(Integer id);

        ApiResponse<Book> add(BookRequest request);

        ApiResponse<Book> update(BookRequest request, Integer id);

        void delete(Integer id);

        ApiResponse<Book> toggleStatus(Integer id);

        // 🔥 Trending books API chỉ nhận TrendingRequest (type, page, size) ok
        PaginationResponse<TrendingBookResponse> getTrendingBooks(TrendingRequest request);

        ApiResponse<List<BookCategoryRequest>> getBooksByCategoryId(Integer id, String text);

        ApiResponse<List<BookSearchRequest>> getBookByName(String text);

        /**
         * 🔥 API tính giá sách cho Frontend
         * Tính giá sách sau khi áp dụng discount và so sánh với flash sale
         */
        BookPriceCalculationResponse calculateBookPrice(Book book, BookPriceCalculationRequest request);

        ApiResponse<List<BookFlashSalesRequest>> findActiveBooksWithStock();

        ApiResponse<List<BookFlashSalesRequest>> findActiveBooksForEdit();

        /**
         * Trả về danh sách sách cho dropdown với đầy đủ thông tin bổ sung
         */
        List<org.datn.bookstation.dto.response.DropdownOptionResponse> getDropdownOptionsWithDetails();

        /**
         * Trả về danh sách sách cho dropdown với tìm kiếm theo tên hoặc mã
         */
        List<org.datn.bookstation.dto.response.DropdownOptionResponse> getDropdownOptionsWithDetails(String search);

        ApiResponse<Long> getTotalSoldBooks();

        ApiResponse<Long> getTotalStockBooks();

        ApiResponse<BigDecimal> getTotalRevenue();

        ApiResponse<List<TopBookSoldResponse>> getTopBookSold(int limit);

        ApiResponse<List<BookStockResponse>> getAllBookStock();

        ApiResponse<PosBookItemResponse> getBookByIsbn(String isbn);
}
