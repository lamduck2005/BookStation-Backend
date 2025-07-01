package org.datn.bookstation.repository;

import org.datn.bookstation.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Integer>, JpaSpecificationExecutor<Book> {
    boolean existsByBookName(String bookName);
    boolean existsByBookCode(String bookCode);
    
    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Book b WHERE UPPER(TRIM(b.bookName)) = UPPER(TRIM(:bookName))")
    boolean existsByBookNameIgnoreCase(@Param("bookName") String bookName);
    
    @Query("SELECT b FROM Book b WHERE b.category.id = :categoryId")
    List<Book> findByCategoryId(@Param("categoryId") Integer categoryId);
    
    @Query("SELECT b FROM Book b WHERE b.supplier.id = :supplierId")
    List<Book> findBySupplierId(@Param("supplierId") Integer supplierId);
    
    @Query("SELECT b FROM Book b WHERE b.status = 1 ORDER BY b.createdAt DESC")
    List<Book> findActiveBooks();
}
