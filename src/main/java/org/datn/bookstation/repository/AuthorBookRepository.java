package org.datn.bookstation.repository;

import org.datn.bookstation.entity.AuthorBook;
import org.datn.bookstation.entity.AuthorBookId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthorBookRepository extends JpaRepository<AuthorBook, AuthorBookId> {
}
