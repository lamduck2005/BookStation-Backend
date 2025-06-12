package org.datn.bookstation.service;

import java.util.List;

public interface AuthorService {
    List<Author> getAll();
    Author getById(Integer id);
    Author add(Author author);
    Author update(Author author, Integer id);
    void delete(Integer id);
}
