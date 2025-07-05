package org.datn.bookstation.service.impl;

import lombok.AllArgsConstructor;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.dto.response.ParentCategoryResponse;
import org.datn.bookstation.entity.Category;
import org.datn.bookstation.mapper.CategoryMap;
import org.datn.bookstation.repository.CategoryRepository;
import org.datn.bookstation.service.CategoryService;
import org.datn.bookstation.specification.CategorySpecification;


import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private CategoryRepository categoryRepository;
    private CategoryMap categoryMap;
    @Override
    public List<Category> getAll() {
        return categoryRepository.findAll();
    }

    // @Override
    // public List<ParentCategoryResponse> getAllParentCategory() {
    // return categoryRepository.getAllParentCategoryRequests();
    // }

    // @Override
    // public List<Object[]> findCategoryHierarchy() {
    // return categoryRepository.findCategoryHierarchy();
    // }

    @Override
    public Category add(Category category) {
        if (category.getParentCategory() != null
                && categoryRepository.getByParentCategoryIsNull(category.getId()) != null) {
            Category ParentCategory = categoryRepository.findById(category.getParentCategory().getId()).get();
            category.setParentCategory(ParentCategory);
        } else {
            category.setParentCategory(null);
        }
        category.setId(null);
        // category.setCreatedAt(Instant.now());
        category.setCreatedBy(1);

        return categoryRepository.save(category);
    }

    @Override
    public Category getById(Integer id) {
        return categoryRepository.findById(id).get();
    }

    @Override
    public Category update(Category category, Integer id) {
        try {
            Category categoryById = categoryRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            categoryById.setCategoryName(category.getCategoryName());
            categoryById.setDescription(category.getDescription());
            categoryById.setStatus(category.getStatus());
            // categoryById.setUpdatedAt(Instant.now());
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
            Category categoryById = categoryRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Category not found"));

            categoryRepository.delete(categoryById);
            return categoryById;
        } catch (Exception e) {
            throw new RuntimeException("delete category failed" + e.getMessage());
        }
    }

    @Override
    public List<Category> getActiveCategories() {
        return categoryRepository.findByStatus((byte) 1);
    }

    @Override
    public List<Category> getAllExceptById(Integer id) {
        return categoryRepository.getAllExceptByID(id);
    }

    @Override
    public PaginationResponse<ParentCategoryResponse> getAllCategoryPagination(Integer page, Integer size, String name,
                                                                               Byte status) {
        Specification<Category> spec = CategorySpecification.filterBy(name, status);
        List<Category> categoriesSpec = categoryRepository.findAll(spec);

        // Tạo cây danh mục
        List<ParentCategoryResponse> parentCategoryResponseList = categoryMap.mapToCategoryTreeList(categoriesSpec);

        // Phân trang thủ công
        int totalElements = parentCategoryResponseList.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);

        // Tính toán vị trí bắt đầu và kết thúc
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, totalElements);

        // Lấy phần tử theo trang
        List<ParentCategoryResponse> pagedCategories;
        if (startIndex >= totalElements) {
            pagedCategories = new ArrayList<>();
        } else {
            pagedCategories = parentCategoryResponseList.subList(startIndex, endIndex);
        }

        // Sử dụng builder pattern hoặc tạo object và set field
        return PaginationResponse.<ParentCategoryResponse>builder()
                .content(pagedCategories)
                .pageNumber(page)
                .pageSize(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .build();
    }
}
