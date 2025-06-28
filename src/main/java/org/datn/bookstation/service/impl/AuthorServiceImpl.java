package org.datn.bookstation.service.impl;

import lombok.AllArgsConstructor;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.entity.Author;
import org.datn.bookstation.repository.AuthorRepository;
import org.datn.bookstation.service.AuthorService;
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
}
