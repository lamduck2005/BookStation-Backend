package org.datn.bookstation.mapper;

import org.datn.bookstation.dto.response.AuthorResponse;
import org.datn.bookstation.dto.response.BookResponse;
import org.datn.bookstation.entity.Book;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class BookResponseMapper {
    
    public BookResponse toResponse(Book book) {
        if (book == null) return null;
        
        BookResponse response = new BookResponse();
        response.setId(book.getId());
        response.setBookName(book.getBookName());
        response.setDescription(book.getDescription());
        response.setPrice(book.getPrice());
        response.setStockQuantity(book.getStockQuantity());
        response.setPublicationDate(book.getPublicationDate());
        response.setBookCode(book.getBookCode());
        response.setStatus(book.getStatus());
        response.setCreatedAt(book.getCreatedAt());
        response.setUpdatedAt(book.getUpdatedAt());
        
        // Set new book detail fields
        response.setCoverImageUrl(book.getCoverImageUrl());
        response.setTranslator(book.getTranslator());
        response.setIsbn(book.getIsbn());
        response.setPageCount(book.getPageCount());
        response.setLanguage(book.getLanguage());
        response.setWeight(book.getWeight());
        response.setDimensions(book.getDimensions());
        
        // Set category info
        if (book.getCategory() != null) {
            response.setCategoryId(book.getCategory().getId());
            response.setCategoryName(book.getCategory().getCategoryName());
        }
        
        // Set supplier info
        if (book.getSupplier() != null) {
            response.setSupplierId(book.getSupplier().getId());
            response.setSupplierName(book.getSupplier().getSupplierName());
        }
        
        // Set publisher info
        if (book.getPublisher() != null) {
            response.setPublisherId(book.getPublisher().getId());
            response.setPublisherName(book.getPublisher().getPublisherName());
        }
        
        // Set authors info
        if (book.getAuthorBooks() != null && !book.getAuthorBooks().isEmpty()) {
            List<AuthorResponse> authors = book.getAuthorBooks().stream()
                .map(authorBook -> {
                    AuthorResponse authorResponse = new AuthorResponse();
                    authorResponse.setId(authorBook.getAuthor().getId());
                    authorResponse.setAuthorName(authorBook.getAuthor().getAuthorName());
                    authorResponse.setBiography(authorBook.getAuthor().getBiography());
                    authorResponse.setBirthDate(authorBook.getAuthor().getBirthDate());
                    authorResponse.setStatus(authorBook.getAuthor().getStatus());
                    return authorResponse;
                })
                .collect(Collectors.toList());
            response.setAuthors(authors);
        }
        
        return response;
    }
}
