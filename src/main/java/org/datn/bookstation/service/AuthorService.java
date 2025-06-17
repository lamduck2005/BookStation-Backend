package org.datn.bookstation.service;

import org.datn.bookstation.entity.Author;

import java.util.List;

public interface AuthorService {
    List<Author> getAll();
    Author getById(Integer id);
    Author add(Author author);
    Author update(Author author, Integer id);
    void delete(Integer id);
}
