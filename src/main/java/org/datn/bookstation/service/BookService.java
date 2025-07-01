package org.datn.bookstation.service;

import org.datn.bookstation.dto.request.BookRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.BookResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.entity.Book;

import java.math.BigDecimal;
import java.util.List;

public interface BookService {
    PaginationResponse<BookResponse> getAllWithPagination(int page, int size, String bookName, 
                                                         Integer categoryId, Integer supplierId, 
                                                         BigDecimal minPrice, BigDecimal maxPrice, 
                                                         Byte status, String bookCode);
    List<Book> getAll();
    List<Book> getActiveBooks();
    List<Book> getBooksByCategory(Integer categoryId);
    List<Book> getBooksBySupplier(Integer supplierId);
    Book getById(Integer id);
    ApiResponse<Book> add(BookRequest request);
    ApiResponse<Book> update(BookRequest request, Integer id);
    void delete(Integer id);
    ApiResponse<Book> toggleStatus(Integer id);
}
