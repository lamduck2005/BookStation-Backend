package org.datn.bookstation.controller;

import lombok.AllArgsConstructor;
import org.datn.bookstation.dto.response.ParentCategoryResponse;
import org.datn.bookstation.entity.Category;
import org.datn.bookstation.mapper.CategoryMap;
import org.datn.bookstation.repository.CategoryRepository;
import org.datn.bookstation.service.impl.CategoryServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryServiceImpl categoryService;
    private final CategoryRepository categoryRepository;
    private final CategoryMap categoryMapper;
    @GetMapping()
    public ResponseEntity<List<Category>> getAll(){
        return ResponseEntity.ok(categoryService.getAll());
    }
//    @GetMapping("/findCategoryHierarchy")
//    public ResponseEntity<List<Object[]>> findCategoryHierarchy(){
//        return ResponseEntity.ok(categoryService.findCategoryHierarchy());
//    }
        @GetMapping("/parentcategories")
    public ResponseEntity<List<ParentCategoryResponse>> getAllParentCategory(){
        List<Category> categories = categoryService.getAll();
        return ResponseEntity.ok(categoryMapper.mapToCategoryTreeList(categories));
    }

    @PostMapping()
    public ResponseEntity<Category> add(@RequestBody Category category){
        System.out.println(category.toString());
        return ResponseEntity.ok(categoryService.add(category));
    }
    @PutMapping("/{id}")
    public ResponseEntity<Category> update(@PathVariable Integer id,@RequestBody Category category){
        return ResponseEntity.ok(categoryService.update(category, id));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Category> delete(@PathVariable Integer id){
        return ResponseEntity.ok(categoryService.delete(id));
    }
    @GetMapping("/{id}")
    public ResponseEntity<Category> getById(@PathVariable Integer id){
        return ResponseEntity.ok(categoryService.getById(id));
    }
}
