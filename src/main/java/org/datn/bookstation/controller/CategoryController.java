package org.datn.bookstation.controller;

import lombok.AllArgsConstructor;
import org.datn.bookstation.entity.Category;
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
    @GetMapping()
    public ResponseEntity<List<Category>> getAll(){
        return ResponseEntity.ok(categoryService.getAll());
    }
    @PostMapping()
    public ResponseEntity<Category> add(@RequestBody Category category){
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
