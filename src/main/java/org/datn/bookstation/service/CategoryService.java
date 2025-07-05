package org.datn.bookstation.service;

import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.dto.response.ParentCategoryResponse;
import org.datn.bookstation.entity.Category;

import java.util.List;

public interface CategoryService {
    List<Category> getAll();

    Category add(Category category);

    Category getById(Integer id);

    Category update(Category category, Integer id);

    Category delete(Integer id);

    List<Category> getActiveCategories(); // For dropdown

    List<Category> getAllExceptById(Integer id);//localhost:8080/api/categories/except/1

    PaginationResponse<ParentCategoryResponse> getAllCategoryPagination(Integer page, Integer size, String name,
                                                                        Byte status);
}
