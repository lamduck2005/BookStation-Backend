package org.datn.bookstation.service.impl;

import lombok.AllArgsConstructor;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.entity.Author;
import org.datn.bookstation.entity.Rank;
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
        return new ApiResponse<>(200,"GetAll thành công",authorRepository.findAll());
    }

    @Override
    public ApiResponse<List<Author>> search() {
        return null;
    }

    @Override
    public ApiResponse<Author> getById(Integer id) {
        Author authorById = authorRepository.findById(id).orElse(null);
        if (authorById== null){
            return new ApiResponse<>(404,"Không tìm thấy ", null);
        }
        return new ApiResponse<Author>(200,"Đã tim thấy đối tượng id này",authorRepository.findById(id).get());
    }

    @Override
    public ApiResponse<Author> add(Author author) {
        try {
            // author.setCreatedAt(Instant.now());
            author.setCreatedBy(1);
            return new ApiResponse<>(200,"thêm thành công",authorRepository.save(author));
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(404,"thêm thất bại"+e.getMessage(),null);
        }
    }

    @Override
    public ApiResponse<Author> update(Author author, Integer id) {
        try {
            Author authorToUpdate = authorRepository.findById(id)
                    .orElse(null);
            if (authorToUpdate==null){
                return new ApiResponse<>(404,"Không tồn tại id này",null);
            }
            author.setCreatedAt(authorToUpdate.getCreatedAt());
            author.setCreatedBy(authorToUpdate.getCreatedBy());
            // author.setUpdatedAt(Instant.now());
            author.setUpdatedBy(1);
            author.setId(id);
            return new ApiResponse<>(200,"Thêm thành công",authorRepository.save(author));
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(404,"update thất bai"+e.getMessage(),null);
        }
    }

    @Override
    public ApiResponse<Author> delete(Integer id) {
        Author authorById = authorRepository.findById(id).orElse(null);
        if (authorById== null){
            return new ApiResponse<>(404,"Đối tượng này không tồn tại",null);
        }
        authorRepository.deleteById(id);
        return new ApiResponse<>(200,"Xóa thành công",authorById);
    }

    @Override
    public ApiResponse<Author> toggleStatus(Integer id) {

        return null;
    }

    @Override
    public PaginationResponse<Author> getAllAuthorPagination(Integer page, Integer size, String name, Byte status) {
        Pageable pageable = PageRequest.of(page, size);
        Specification<Author> spec = AuthorSpecification.filterBy(name,status);
        Page<Author> authorPage = authorRepository.findAll(spec,pageable);
        List<Author> authors = authorPage.stream().toList();
        return new PaginationResponse<>(
                authors,
                authorPage.getNumber(),
                authorPage.getSize(),
                authorPage.getTotalElements(),
                authorPage.getTotalPages()
        );
    }

    @Override
    public List<Author> getActiveAuthors() {
        return authorRepository.findByStatus((byte) 1);
    }
}
