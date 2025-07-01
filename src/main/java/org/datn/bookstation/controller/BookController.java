package org.datn.bookstation.controller;

import org.datn.bookstation.entity.Book;
import org.datn.bookstation.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/books")
public class BookController {
    @Autowired
    private BookService bookService;

    // Lấy tất cả sách
    @GetMapping
    public List<Map<String, Object>> getAllBooks() {
        List<Book> books = bookService.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Book b : books) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", b.getId());
            map.put("book_code", b.getBookCode());
            map.put("book_name", b.getBookName());
            // TODO: Lấy danh sách tác giả nếu có quan hệ author, ở đây giả sử là 1 tác giả
            map.put("author_name", Collections.singletonList("Tác giả mẫu"));
            map.put("price", b.getPrice());
            map.put("stock_quantity", b.getStockQuantity());
            map.put("category_name", b.getCategory() != null ? b.getCategory().getCategoryName() : null);
            map.put("supplier_name", b.getSupplier() != null ? b.getSupplier().getSupplierName() : null);
            // TODO: flash_sale_name nếu có quan hệ, ở đây để rỗng
            map.put("flash_sale_name", "");
            map.put("publication_date", b.getPublicationDate());
            map.put("created_at", b.getCreatedAt());
            map.put("updated_at", b.getUpdatedAt());
            result.add(map);
        }
        return result;
    }

    // Lấy sách theo id
    @GetMapping("/{id}")
    public Map<String, Object> getBook(@PathVariable Integer id) {
        Optional<Book> bookOpt = bookService.findById(id);
        if (bookOpt.isPresent()) {
            Book b = bookOpt.get();
            Map<String, Object> map = new HashMap<>();
            map.put("id", b.getId());
            map.put("book_code", b.getBookCode());
            map.put("book_name", b.getBookName());
            map.put("author_name", Collections.singletonList("Tác giả mẫu"));
            map.put("price", b.getPrice());
            map.put("stock_quantity", b.getStockQuantity());
            map.put("category_name", b.getCategory() != null ? b.getCategory().getCategoryName() : null);
            map.put("supplier_name", b.getSupplier() != null ? b.getSupplier().getSupplierName() : null);
            map.put("flash_sale_name", "");
            map.put("publication_date", b.getPublicationDate());
            map.put("created_at", b.getCreatedAt());
            map.put("updated_at", b.getUpdatedAt());
            return map;
        }
        throw new RuntimeException("Book not found");
    }

    // Thêm sách mới
    @PostMapping
    public Map<String, Object> createBook(@RequestBody Map<String, Object> req) {
        Book book = new Book();
        book.setBookCode((String) req.get("book_code"));
        book.setBookName((String) req.get("book_name"));
        // TODO: set author nếu có
        book.setPrice(new BigDecimal(req.getOrDefault("price", 0).toString()));
        book.setStockQuantity((Integer) req.getOrDefault("stock_quantity", 0));
        // TODO: set category, supplier nếu có
        // book.setCategory(...);
        // book.setSupplier(...);
        book.setPublicationDate(null); // parse nếu có
        book.setCreatedAt(new Date().toInstant());
        book.setUpdatedAt(new Date().toInstant());
        Book saved = bookService.save(book);

        Map<String, Object> map = new HashMap<>();
        map.put("id", saved.getId());
        map.put("book_code", saved.getBookCode());
        map.put("book_name", saved.getBookName());
        map.put("author_name", Collections.singletonList("Tác giả mẫu"));
        map.put("price", saved.getPrice());
        map.put("stock_quantity", saved.getStockQuantity());
        map.put("category_name", saved.getCategory() != null ? saved.getCategory().getCategoryName() : null);
        map.put("supplier_name", saved.getSupplier() != null ? saved.getSupplier().getSupplierName() : null);
        map.put("flash_sale_name", "");
        map.put("publication_date", saved.getPublicationDate());
        map.put("created_at", saved.getCreatedAt());
        map.put("updated_at", saved.getUpdatedAt());
        return map;
    }

    // Cập nhật sách
    @PutMapping("/{id}")
    public Map<String, Object> updateBook(@PathVariable Integer id, @RequestBody Map<String, Object> req) {
        Optional<Book> bookOpt = bookService.findById(id);
        if (bookOpt.isPresent()) {
            Book book = bookOpt.get();
            book.setBookCode((String) req.get("book_code"));
            book.setBookName((String) req.get("book_name"));
            // TODO: set author nếu có
            book.setPrice(new BigDecimal(req.getOrDefault("price", 0).toString()));
            book.setStockQuantity((Integer) req.getOrDefault("stock_quantity", 0));
            // TODO: set category, supplier nếu có
            // book.setCategory(...);
            // book.setSupplier(...);
            book.setUpdatedAt(new Date().toInstant());
            Book saved = bookService.save(book);

            Map<String, Object> map = new HashMap<>();
            map.put("id", saved.getId());
            map.put("book_code", saved.getBookCode());
            map.put("book_name", saved.getBookName());
            map.put("author_name", Collections.singletonList("Tác giả mẫu"));
            map.put("price", saved.getPrice());
            map.put("stock_quantity", saved.getStockQuantity());
            map.put("category_name", saved.getCategory() != null ? saved.getCategory().getCategoryName() : null);
            map.put("supplier_name", saved.getSupplier() != null ? saved.getSupplier().getSupplierName() : null);
            map.put("flash_sale_name", "");
            map.put("publication_date", saved.getPublicationDate());
            map.put("created_at", saved.getCreatedAt());
            map.put("updated_at", saved.getUpdatedAt());
            return map;
        }
        throw new RuntimeException("Book not found");
    }

    // Xóa sách
    @DeleteMapping("/{id}")
    public void deleteBook(@PathVariable Integer id) {
        bookService.deleteById(id);
    }
}
