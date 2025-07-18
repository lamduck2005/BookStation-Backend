package org.datn.bookstation.mapper;

import org.datn.bookstation.dto.request.BookCategoryRequest;
import org.datn.bookstation.dto.request.BookSearchRequest;
import org.datn.bookstation.entity.Book;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BookCategoryMapper {
    public List<BookCategoryRequest> booksMapper(List<Book> books) {
        return books.stream()
                .map(book -> new BookCategoryRequest(
                        book.getId(),
                        book.getBookName(),
                        book.getDescription(),
                        book.getPrice(),
                        book.getCategory() != null ? book.getCategory().getId() : null))
                .toList();
    }

    public List<BookSearchRequest> bookSearchMapper(List<Book> books) {
        return books.stream()
                .map(book -> new BookSearchRequest(
                        book.getId(),
                        book.getBookName(),
                        book.getPrice(),
                        book.getCoverImageUrl()
                ))
                .toList();
    }
}
