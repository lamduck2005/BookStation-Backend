package org.datn.bookstation.controller;

import lombok.AllArgsConstructor;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.DropdownOptionResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.dto.response.ParentCategoryResponse;
import org.datn.bookstation.entity.Category;
import org.datn.bookstation.service.CategoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
@RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping
    public ApiResponse<List<Category>> getAll() {
        return categoryService.getAll();
    }

    @GetMapping("/parentcategories")
    public ApiResponse<PaginationResponse<ParentCategoryResponse>> getAllWithPagination(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Byte status) {
        return categoryService.getAllCategoryPagination(page, size, name, status);
    }

    @GetMapping("/{id}")
    public ApiResponse<Category> getById(@PathVariable Integer id) {
        return categoryService.getById(id);
    }

    @GetMapping("/except/{id}")
    public ApiResponse<List<Category>> getAllExceptById(@PathVariable Integer id) {
        return categoryService.getAllExceptById(id);
    }

    @GetMapping("/dropdown")
    public ApiResponse<List<DropdownOptionResponse>> getDropdownCategories() {
        ApiResponse<List<Category>> categoriesResponse = categoryService.getActiveCategories();

        if (categoriesResponse.getStatus() != 200) {
            return new ApiResponse<>(500, "Lỗi khi lấy danh sách danh mục", null);
        }

        List<DropdownOptionResponse> dropdown = categoriesResponse.getData().stream()
                .map(category -> new DropdownOptionResponse(category.getId(), category.getCategoryName()))
                .collect(Collectors.toList());

        return new ApiResponse<>(200, "Lấy danh sách danh mục thành công", dropdown);
    }

    @PostMapping
    public ApiResponse<Category> add(@RequestBody Category category) {
        return categoryService.add(category);
    }

    @PutMapping("/{id}")
    public ApiResponse<Category> update(@PathVariable Integer id, @RequestBody Category category) {
        return categoryService.update(category, id);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Category> delete(@PathVariable Integer id) {
        return categoryService.delete(id);
    }

    @PatchMapping("/{id}/toggle-status")
    public ApiResponse<Category> toggleStatus(@PathVariable Integer id) {
        return categoryService.toggleStatus(id);
    }
    @GetMapping("/fiter")
    public ApiResponse<List<ParentCategoryResponse>> getAllCategoriesForUser(
            ) {
        return categoryService.getAllCategoryPagination();
    }
}
