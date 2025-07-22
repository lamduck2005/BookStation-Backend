package org.datn.bookstation.service.impl;

import lombok.AllArgsConstructor;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.entity.Author;
import org.datn.bookstation.repository.AuthorRepository;
import org.datn.bookstation.service.AuthorService;
import org.datn.bookstation.specification.AuthorSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class AuthorServiceImpl implements AuthorService {
    private final AuthorRepository authorRepository;

    @Override
    public ApiResponse<List<Author>> getAll() {
        try {
            List<Author> authors = authorRepository.findAll();
            return new ApiResponse<>(200, "Lấy danh sách tác giả thành công", authors);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Lỗi khi lấy danh sách tác giả: " + e.getMessage(), null);
        }
    }



    @Override
    public ApiResponse<Author> getById(Integer id) {
        try {
            Author author = authorRepository.findById(id).orElse(null);
            if (author == null) {
                return new ApiResponse<>(404, "Không tìm thấy tác giả với ID: " + id, null);
            }
            return new ApiResponse<>(200, "Lấy thông tin tác giả thành công", author);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Lỗi khi lấy thông tin tác giả: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<Author> add(Author author) {
        try {
            author.setCreatedBy(1); // Hoặc lấy từ user hiện tại
            Author savedAuthor = authorRepository.save(author);
            return new ApiResponse<>(201, "Thêm tác giả thành công", savedAuthor);
        } catch (Exception e) {
            return new ApiResponse<>(400, "Thêm tác giả thất bại: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<Author> update(Author author, Integer id) {
        try {
            Author authorToUpdate = authorRepository.findById(id).orElse(null);
            if (authorToUpdate == null) {
                return new ApiResponse<>(404, "Không tìm thấy tác giả với ID: " + id, null);
            }

            // Giữ lại thông tin gốc
            author.setId(id);
            author.setCreatedAt(authorToUpdate.getCreatedAt());
            author.setCreatedBy(authorToUpdate.getCreatedBy());
            author.setUpdatedBy(1); // Hoặc lấy từ user hiện tại

            Author updatedAuthor = authorRepository.save(author);
            return new ApiResponse<>(200, "Cập nhật tác giả thành công", updatedAuthor);
        } catch (Exception e) {
            return new ApiResponse<>(400, "Cập nhật tác giả thất bại: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<Author> delete(Integer id) {
        try {
            Author author = authorRepository.findById(id).orElse(null);
            if (author == null) {
                return new ApiResponse<>(404, "Không tìm thấy tác giả với ID: " + id, null);
            }
            authorRepository.deleteById(id);
            return new ApiResponse<>(200, "Xóa tác giả thành công", author);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Xóa tác giả thất bại: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<Author> toggleStatus(Integer id) {
        try {
            Author author = authorRepository.findById(id).orElse(null);
            if (author == null) {
                return new ApiResponse<>(404, "Không tìm thấy tác giả với ID: " + id, null);
            }

            if (author.getStatus() == null) {
                author.setStatus((byte) 1);
            } else {
                author.setStatus((byte) (author.getStatus() == 1 ? 0 : 1));
            }
            author.setUpdatedBy(1); // Hoặc lấy từ user hiện tại

            Author updatedAuthor = authorRepository.save(author);
            return new ApiResponse<>(200, "Cập nhật trạng thái tác giả thành công", updatedAuthor);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Cập nhật trạng thái thất bại: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<PaginationResponse<Author>> getAllAuthorPagination(Integer page, Integer size, String name,
            Byte status) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Specification<Author> spec = AuthorSpecification.filterBy(name, status);
            Page<Author> authorPage = authorRepository.findAll(spec, pageable);

            List<Author> authors = authorPage.getContent();

            PaginationResponse<Author> paginationResponse = PaginationResponse.<Author>builder()
                    .content(authors)
                    .pageNumber(authorPage.getNumber())
                    .pageSize(authorPage.getSize())
                    .totalElements(authorPage.getTotalElements())
                    .totalPages(authorPage.getTotalPages())
                    .build();

            return new ApiResponse<>(200, "Lấy danh sách tác giả phân trang thành công", paginationResponse);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Lỗi khi lấy danh sách phân trang: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<List<Author>> getActiveAuthors() {
        try {
            List<Author> activeAuthors = authorRepository.findByStatus((byte) 1);
            return new ApiResponse<>(200, "Lấy danh sách tác giả đang hoạt động thành công", activeAuthors);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Lỗi khi lấy danh sách tác giả hoạt động: " + e.getMessage(), null);
        }
    }
}
