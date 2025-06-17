package org.datn.bookstation.service;

import org.datn.bookstation.entity.Category;

import java.util.List;

public interface CategoryService {
    List<Category> getAll();
    Category add(Category category);
    Category getById(Integer id);
    Category update(Category category, Integer id);
    Category delete(Integer id);
}
