package org.datn.bookstation.controller;

import lombok.AllArgsConstructor;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.DropdownOptionResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.entity.Author;
import org.datn.bookstation.service.AuthorService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
@RequestMapping("/api/authors")
public class AuthorController {
    private final AuthorService authorService;

    @GetMapping
    public ApiResponse<List<Author>> getAll() {
        return authorService.getAll();
    }

    @GetMapping("/page")
    public ApiResponse<PaginationResponse<Author>> getPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Byte status) {
        return authorService.getAllAuthorPagination(page, size, name, status);
    }

    @GetMapping("/{id}")
    public ApiResponse<Author> getById(@PathVariable Integer id) {
        return authorService.getById(id);
    }

    @PostMapping
    public ApiResponse<Author> add(@RequestBody Author author) {
        return authorService.add(author);
    }

    @PutMapping("/{id}")
    public ApiResponse<Author> update(@PathVariable Integer id, @RequestBody Author author) {
        return authorService.update(author, id);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Author> delete(@PathVariable Integer id) {
        return authorService.delete(id);
    }

    @PatchMapping("/{id}/toggle-status")
    public ApiResponse<Author> toggleStatus(@PathVariable Integer id) {
        return authorService.toggleStatus(id);
    }

    @GetMapping("/dropdown")
    public ApiResponse<List<DropdownOptionResponse>> getDropdownAuthors() {
        ApiResponse<List<Author>> authorsResponse = authorService.getActiveAuthors();

        if (authorsResponse.getStatus() != 200) {
            return new ApiResponse<>(500, "Lỗi khi lấy danh sách tác giả", null);
        }

        List<DropdownOptionResponse> dropdown = authorsResponse.getData().stream()
                .map(author -> new DropdownOptionResponse(author.getId(), author.getAuthorName()))
                .collect(Collectors.toList());

        return new ApiResponse<>(HttpStatus.OK.value(), "Lấy danh sách tác giả thành công", dropdown);
    }
}
