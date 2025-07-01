package org.datn.bookstation.controller;

import lombok.AllArgsConstructor;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.DropdownOptionResponse;
import org.datn.bookstation.dto.response.ParentCategoryResponse;
import org.datn.bookstation.entity.Category;
import org.datn.bookstation.mapper.CategoryMap;
import org.datn.bookstation.repository.CategoryRepository;
import org.datn.bookstation.service.impl.CategoryServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
@RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryServiceImpl categoryService;
    private final CategoryRepository categoryRepository;
    private final CategoryMap categoryMapper;

    @GetMapping()
    public ResponseEntity<List<Category>> getAll() {
        return ResponseEntity.ok(categoryService.getAll());
    }

    @GetMapping("/parentcategories")
    public ResponseEntity<List<ParentCategoryResponse>> getAllParentCategory() {
        List<Category> categories = categoryService.getAll();
        return ResponseEntity.ok(categoryMapper.mapToCategoryTreeList(categories));
    }

    @PostMapping()
    public ResponseEntity<Category> add(@RequestBody Category category) {
        System.out.println(category.toString());
        return ResponseEntity.ok(categoryService.add(category));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Category> update(@PathVariable Integer id, @RequestBody Category category) {
        System.out.println(category);
        return ResponseEntity.ok(categoryService.update(category, id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Category> delete(@PathVariable Integer id) {
        return ResponseEntity.ok(categoryService.delete(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Category> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(categoryService.getById(id));
    }

    @GetMapping("/dropdown")
    public ResponseEntity<ApiResponse<List<DropdownOptionResponse>>> getDropdownCategories() {
        List<DropdownOptionResponse> dropdown = categoryService.getActiveCategories().stream()
            .map(category -> new DropdownOptionResponse(category.getId(), category.getCategoryName()))
            .collect(Collectors.toList());
        ApiResponse<List<DropdownOptionResponse>> response = new ApiResponse<>(HttpStatus.OK.value(), "Lấy danh sách danh mục thành công", dropdown);
        return ResponseEntity.ok(response);
    }
}
