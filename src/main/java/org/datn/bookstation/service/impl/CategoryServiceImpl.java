package org.datn.bookstation.service.impl;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.datn.bookstation.entity.Category;
import org.datn.bookstation.repository.CategoryRepository;
import org.datn.bookstation.service.CategoryService;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@AllArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private CategoryRepository categoryRepository;

    @Override
    public List<Category> getAll() {
        return categoryRepository.findAll();
    }

    @Override
    public Category add(Category category) {
        if (category.getParentCategory()!=null){
            Category ParentCategory = categoryRepository.findById(category.getParentCategory().getId()).get();
            category.setParentCategory(ParentCategory);
        }
        category.setId(null);
        category.setCreatedAt(Instant.now());
        category.setCreatedBy(1);
        category.setStatus("Hoạt Động");

        return categoryRepository.save(category);
    }

    @Override
    public Category getById(Integer id) {
        return categoryRepository.findById(id).get();
    }



    @Override
    public Category update( Category category,Integer id) {
        try {
            Category categoryById = categoryRepository.findById(id).orElseThrow(() -> new RuntimeException("Category not found"));
            categoryById.setCategoryName(category.getCategoryName());
            categoryById.setDescription(category.getDescription());
            categoryById.setStatus(category.getStatus());
            categoryById.setUpdatedAt(Instant.now());
            categoryById.setUpdatedBy(1);
            categoryById.setParentCategory(category.getParentCategory());
            categoryById.setId(id);
            return categoryRepository.save(categoryById);
        } catch (Exception e) {
            throw new RuntimeException("update category failed" + e.getMessage());
        }
    }

    @Override
    public Category delete(Integer id) {
        try {
            Category categoryById = categoryRepository.findById(id).orElseThrow(() -> new RuntimeException("Category not found"));

            categoryRepository.delete(categoryById);
            return categoryById;
        } catch (Exception e) {
            throw new RuntimeException("delete category failed" + e.getMessage());
        }
    }

}
