package org.datn.bookstation.service;

import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.entity.Author;

import java.util.List;

public interface AuthorService {
   ApiResponse<List<Author>> getAll();
   ApiResponse<List<Author>> search();
    ApiResponse<Author> getById(Integer id);
    ApiResponse<Author> add(Author author);
    ApiResponse<Author> update(Author author, Integer id);
    ApiResponse<Author> delete(Integer id);
    ApiResponse<Author> toggleStatus(Integer id);
    PaginationResponse<Author> getAllAuthorPagination(Integer page,Integer size,String name, Byte status);
    List<Author> getActiveAuthors(); // For dropdown
}
