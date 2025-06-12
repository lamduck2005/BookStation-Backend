package org.datn.bookstation.service.impl;

import lombok.AllArgsConstructor;
import org.datn.bookstation.entity.Author;
import org.datn.bookstation.repository.AuthorRepository;
import org.datn.bookstation.service.AuthorService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@AllArgsConstructor
public class AuthorServiceImpl implements AuthorService {
    private final AuthorRepository authorRepository;

    @Override
    public List<Author> getAll() {
        return authorRepository.findAll();
    }

    @Override
    public Author getById(Integer id) {
        return authorRepository.findById(id).get();
    }

    @Override
    public Author add(Author author) {
        try {
            author.setCreatedAt(Instant.now());
            author.setCreatedBy(1);
            author.setStatus("Hoạt Động");
            return authorRepository.save(author);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error adding author: " + e.getMessage());
            return null;
        }
    }

    @Override
    public Author update(Author author, Integer id) {
        try {
            Author authorToUpdate = authorRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Author not found"));
            author.setCreatedAt(authorToUpdate.getCreatedAt());
            author.setCreatedBy(authorToUpdate.getCreatedBy());
            author.setUpdatedAt(Instant.now());
            author.setUpdatedBy(1);
            author.setId(id);
            return authorRepository.save(author);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error updating author: " + e.getMessage());
            return null;
        }
    }

    @Override
    public void delete(Integer id) {
        authorRepository.deleteById(id);
    }
}
