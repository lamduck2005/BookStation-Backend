package org.datn.bookstation.controller;

import lombok.AllArgsConstructor;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.dto.response.RankResponse;
import org.datn.bookstation.entity.Author;
import org.datn.bookstation.entity.Rank;
import org.datn.bookstation.service.AuthorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/authors")
public class AuthorController {
    private final AuthorService authorService;

//    @GetMapping
//    public ResponseEntity<ApiResponse<List<Author>>> getAll() {
//        return ResponseEntity.ok(authorService.getAll());
//    }

    @GetMapping
    public ResponseEntity<ApiResponse<PaginationResponse<Author>>> getPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Byte status
    ){
        PaginationResponse<Author> authors = authorService.getAllAuthorPagination(page, size, name, status);
        ApiResponse<PaginationResponse<Author>> response = new ApiResponse<>(HttpStatus.OK.value(), "Thành công", authors);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Author>> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(authorService.getById(id));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Author>> add(@RequestBody Author author) {
        System.out.println(author);
        return ResponseEntity.ok(authorService.add(author));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Author>> update(@PathVariable Integer id, @RequestBody Author author) {
        return ResponseEntity.ok(authorService.update(author, id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        authorService.delete(id);
        return ResponseEntity.noContent().build();
    }
    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<ApiResponse<Author>> toggleStatus(@PathVariable Integer id) {
        ApiResponse<Author> response = authorService.toggleStatus(id);
        if (response.getStatus() == 404) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(404, "Không tìm thấy", null));
        }
        return ResponseEntity.ok(new ApiResponse<>(200, "Cập nhật trạng thái thành công", response.getData()));
    }
}
