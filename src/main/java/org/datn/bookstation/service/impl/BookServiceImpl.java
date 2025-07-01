package org.datn.bookstation.service.impl;

import lombok.AllArgsConstructor;
import org.datn.bookstation.dto.request.BookRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.BookResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.entity.Book;
import org.datn.bookstation.entity.Category;
import org.datn.bookstation.entity.Supplier;
import org.datn.bookstation.entity.Author;
import org.datn.bookstation.entity.AuthorBook;
import org.datn.bookstation.entity.AuthorBookId;
import org.datn.bookstation.mapper.BookMapper;
import org.datn.bookstation.mapper.BookResponseMapper;
import org.datn.bookstation.repository.BookRepository;
import org.datn.bookstation.repository.CategoryRepository;
import org.datn.bookstation.repository.SupplierRepository;
import org.datn.bookstation.repository.AuthorRepository;
import org.datn.bookstation.repository.AuthorBookRepository;
import org.datn.bookstation.service.BookService;
import org.datn.bookstation.specification.BookSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class BookServiceImpl implements BookService {
    
    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final AuthorRepository authorRepository;
    private final AuthorBookRepository authorBookRepository;
    private final BookMapper bookMapper;
    private final BookResponseMapper bookResponseMapper;

    @Override
    public PaginationResponse<BookResponse> getAllWithPagination(int page, int size, String bookName, 
                                                                Integer categoryId, Integer supplierId, 
                                                                BigDecimal minPrice, BigDecimal maxPrice, 
                                                                Byte status, String bookCode) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Specification<Book> specification = BookSpecification.filterBy(bookName, categoryId, supplierId, 
                                                                       minPrice, maxPrice, status, bookCode);
        Page<Book> bookPage = bookRepository.findAll(specification, pageable);
        
        List<BookResponse> bookResponses = bookPage.getContent().stream()
                .map(bookResponseMapper::toResponse)
                .collect(Collectors.toList());
                
        return PaginationResponse.<BookResponse>builder()
                .content(bookResponses)
                .pageNumber(bookPage.getNumber())
                .pageSize(bookPage.getSize())
                .totalElements(bookPage.getTotalElements())
                .totalPages(bookPage.getTotalPages())
                .build();
    }

    @Override
    public List<Book> getAll() {
        return bookRepository.findAll();
    }

    @Override
    public List<Book> getActiveBooks() {
        return bookRepository.findActiveBooks();
    }
    
    @Override
    public List<Book> getBooksByCategory(Integer categoryId) {
        return bookRepository.findByCategoryId(categoryId);
    }
    
    @Override
    public List<Book> getBooksBySupplier(Integer supplierId) {
        return bookRepository.findBySupplierId(supplierId);
    }

    @Override
    public Book getById(Integer id) {
        return bookRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public ApiResponse<Book> add(BookRequest request) {
        try {
            // Validate book name uniqueness
            if (bookRepository.existsByBookNameIgnoreCase(request.getBookName())) {
                return new ApiResponse<>(400, "Tên sách đã tồn tại", null);
            }
            
            // Validate book code uniqueness
            if (request.getBookCode() != null && bookRepository.existsByBookCode(request.getBookCode())) {
                return new ApiResponse<>(400, "Mã sách đã tồn tại", null);
            }
            
            // ✅ THÊM: Validate authors - Bắt buộc phải có ít nhất 1 tác giả
            if (request.getAuthorIds() == null || request.getAuthorIds().isEmpty()) {
                return new ApiResponse<>(400, "Sách phải có ít nhất một tác giả", null);
            }
            
            // Validate all authors exist
            List<Author> authors = authorRepository.findAllById(request.getAuthorIds());
            if (authors.size() != request.getAuthorIds().size()) {
                return new ApiResponse<>(404, "Một hoặc nhiều tác giả không tồn tại", null);
            }
            
            Book book = bookMapper.toBook(request);
            
            // Set category if provided
            if (request.getCategoryId() != null) {
                Category category = categoryRepository.findById(request.getCategoryId()).orElse(null);
                if (category == null) {
                    return new ApiResponse<>(404, "Không tìm thấy danh mục", null);
                }
                book.setCategory(category);
            }
            
            // Set supplier if provided
            if (request.getSupplierId() != null) {
                Supplier supplier = supplierRepository.findById(request.getSupplierId()).orElse(null);
                if (supplier == null) {
                    return new ApiResponse<>(404, "Không tìm thấy nhà cung cấp", null);
                }
                book.setSupplier(supplier);
            }
            
            // Generate book code if not provided
            if (request.getBookCode() == null || request.getBookCode().isEmpty()) {
                book.setBookCode("BOOK" + System.currentTimeMillis());
            }
            
            book.setCreatedBy(1); // Default created by system user
            book.setStatus((byte) 1); // Active by default
            
            // ✅ THÊM: Save book first to get ID
            Book savedBook = bookRepository.save(book);
            
            // ✅ THÊM: Create AuthorBook relationships
            for (Author author : authors) {
                AuthorBook authorBook = new AuthorBook();
                AuthorBookId id = new AuthorBookId();
                id.setBookId(savedBook.getId());
                id.setAuthorId(author.getId());
                authorBook.setId(id);
                authorBook.setBook(savedBook);
                authorBook.setAuthor(author);
                authorBookRepository.save(authorBook);
            }
            
            return new ApiResponse<>(201, "Tạo sách thành công", savedBook);
            
        } catch (Exception e) {
            return new ApiResponse<>(500, "Lỗi khi tạo sách: " + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<Book> update(BookRequest request, Integer id) {
        try {
            Book existing = bookRepository.findById(id).orElse(null);
            if (existing == null) {
                return new ApiResponse<>(404, "Không tìm thấy sách", null);
            }
            
            // Validate book name uniqueness (excluding current book)
            if (!existing.getBookName().equalsIgnoreCase(request.getBookName()) && 
                bookRepository.existsByBookNameIgnoreCase(request.getBookName())) {
                return new ApiResponse<>(400, "Tên sách đã tồn tại", null);
            }
            
            // Validate book code uniqueness (excluding current book)
            if (request.getBookCode() != null && 
                !existing.getBookCode().equals(request.getBookCode()) &&
                bookRepository.existsByBookCode(request.getBookCode())) {
                return new ApiResponse<>(400, "Mã sách đã tồn tại", null);
            }
            
            // ✅ THÊM: Validate authors if provided
            if (request.getAuthorIds() != null && !request.getAuthorIds().isEmpty()) {
                List<Author> authors = authorRepository.findAllById(request.getAuthorIds());
                if (authors.size() != request.getAuthorIds().size()) {
                    return new ApiResponse<>(404, "Một hoặc nhiều tác giả không tồn tại", null);
                }
                
                // Delete existing author relationships
                authorBookRepository.deleteByBookId(id);
                
                // Create new author relationships
                for (Author author : authors) {
                    AuthorBook authorBook = new AuthorBook();
                    AuthorBookId authorBookId = new AuthorBookId();
                    authorBookId.setBookId(id);
                    authorBookId.setAuthorId(author.getId());
                    authorBook.setId(authorBookId);
                    authorBook.setBook(existing);
                    authorBook.setAuthor(author);
                    authorBookRepository.save(authorBook);
                }
            }
            
            // Update basic fields
            existing.setBookName(request.getBookName());
            existing.setDescription(request.getDescription());
            existing.setPrice(request.getPrice());
            existing.setStockQuantity(request.getStockQuantity());
            existing.setPublicationDate(request.getPublicationDate());
            
            if (request.getBookCode() != null) {
                existing.setBookCode(request.getBookCode());
            }
            
            if (request.getStatus() != null) {
                existing.setStatus(request.getStatus());
            }
            
            // Update category if provided
            if (request.getCategoryId() != null) {
                Category category = categoryRepository.findById(request.getCategoryId()).orElse(null);
                if (category == null) {
                    return new ApiResponse<>(404, "Không tìm thấy danh mục", null);
                }
                existing.setCategory(category);
            }
            
            // Update supplier if provided
            if (request.getSupplierId() != null) {
                Supplier supplier = supplierRepository.findById(request.getSupplierId()).orElse(null);
                if (supplier == null) {
                    return new ApiResponse<>(404, "Không tìm thấy nhà cung cấp", null);
                }
                existing.setSupplier(supplier);
            }
            
            existing.setUpdatedBy(1); // Default updated by system user
            existing.setUpdatedAt(Instant.now().toEpochMilli());
            
            Book saved = bookRepository.save(existing);
            return new ApiResponse<>(200, "Cập nhật sách thành công", saved);
            
        } catch (Exception e) {
            return new ApiResponse<>(500, "Lỗi khi cập nhật sách: " + e.getMessage(), null);
        }
    }

    @Override
    public void delete(Integer id) {
        bookRepository.deleteById(id);
    }

    @Override
    public ApiResponse<Book> toggleStatus(Integer id) {
        try {
            Book existing = bookRepository.findById(id).orElse(null);
            if (existing == null) {
                return new ApiResponse<>(404, "Không tìm thấy sách", null);
            }
            
            // Toggle status: 1 (active) <-> 0 (inactive)
            existing.setStatus(existing.getStatus() == 1 ? (byte) 0 : (byte) 1);
            existing.setUpdatedBy(1); // Default updated by system user
            existing.setUpdatedAt(Instant.now().toEpochMilli());
            
            Book saved = bookRepository.save(existing);
            return new ApiResponse<>(200, "Cập nhật trạng thái thành công", saved);
            
        } catch (Exception e) {
            return new ApiResponse<>(500, "Lỗi khi cập nhật trạng thái: " + e.getMessage(), null);
        }
    }
}
