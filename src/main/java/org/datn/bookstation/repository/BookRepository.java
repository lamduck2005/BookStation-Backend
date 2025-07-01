package org.datn.bookstation.repository;

import org.datn.bookstation.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Integer> {
    // Có thể bổ sung các phương thức tìm kiếm custom nếu cần
}
