package org.datn.bookstation.service.impl;

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

//    @Override
//    public List<ParentCategoryResponse> getAllParentCategory() {
//        return categoryRepository.getAllParentCategoryRequests();
//    }

//    @Override
//    public List<Object[]> findCategoryHierarchy() {
//        return categoryRepository.findCategoryHierarchy();
//    }

    @Override
    public Category add(Category category) {
        if (category.getParentCategory() != null && categoryRepository.getByParentCategoryIsNull(category.getId()) != null) {
            Category ParentCategory = categoryRepository.findById(category.getParentCategory().getId()).get();
            category.setParentCategory(ParentCategory);
        }else {
            category.setParentCategory(null);
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
    public Category update(Category category, Integer id) {
        try {
            Category categoryById = categoryRepository.findById(id).orElseThrow(() -> new RuntimeException("Category not found"));
            categoryById.setCategoryName(category.getCategoryName());
            categoryById.setDescription(category.getDescription());
            categoryById.setStatus(category.getStatus());
            categoryById.setUpdatedAt(Instant.now());
            categoryById.setUpdatedBy(1);
            if (category.getParentCategory() == null) {
                categoryById.setParentCategory(null);
                System.out.println("...");
            } else if (categoryRepository.getByParentCategoryIsNull(category.getParentCategory().getId()) != null) {
                categoryById.setParentCategory(category.getParentCategory());

            } else {
                return null;
            }
            categoryById.setId(id);
            System.out.println();
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
