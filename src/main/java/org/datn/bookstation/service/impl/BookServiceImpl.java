package org.datn.bookstation.service.impl;

import lombok.AllArgsConstructor;
import org.datn.bookstation.dto.request.BookCategoryRequest;
import org.datn.bookstation.dto.request.BookRequest;
import org.datn.bookstation.dto.request.BookSearchRequest;
import org.datn.bookstation.dto.request.TrendingRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.BookResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.dto.response.TrendingBookResponse;
import org.datn.bookstation.entity.Book;
import org.datn.bookstation.entity.Category;
import org.datn.bookstation.entity.Supplier;
import org.datn.bookstation.entity.Publisher;
import org.datn.bookstation.entity.Author;
import org.datn.bookstation.entity.AuthorBook;
import org.datn.bookstation.entity.AuthorBookId;
import org.datn.bookstation.mapper.BookCategoryMapper;
import org.datn.bookstation.mapper.BookMapper;
import org.datn.bookstation.mapper.BookResponseMapper;
import org.datn.bookstation.mapper.TrendingBookMapper;
import org.datn.bookstation.repository.BookRepository;
import org.datn.bookstation.repository.CategoryRepository;
import org.datn.bookstation.repository.SupplierRepository;
import org.datn.bookstation.repository.PublisherRepository;
import org.datn.bookstation.repository.AuthorRepository;
import org.datn.bookstation.repository.AuthorBookRepository;
import org.datn.bookstation.service.BookService;
import org.datn.bookstation.specification.BookSpecification;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class BookServiceImpl implements BookService {

//    private static final Object T = ;
    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final PublisherRepository publisherRepository;
    private final AuthorRepository authorRepository;
    private final AuthorBookRepository authorBookRepository;
    private final BookMapper bookMapper;
    private final BookResponseMapper bookResponseMapper;
    private final TrendingBookMapper trendingBookMapper;
    private final BookCategoryMapper bookCategoryMapper;

    @Override
    public PaginationResponse<BookResponse> getAllWithPagination(int page, int size, String bookName,
                                                                 Integer categoryId, Integer supplierId, Integer publisherId,
                                                                 BigDecimal minPrice, BigDecimal maxPrice,
                                                                 Byte status, String bookCode) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Specification<Book> specification = BookSpecification.filterBy(bookName, categoryId, supplierId, publisherId,
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
    public PaginationResponse<BookResponse> getAllWithPagination(int page, int size, String bookName, Integer parentId, Integer categoryId, Integer publisherId, BigDecimal minPrice, BigDecimal maxPrice) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Specification<Book> specification = BookSpecification.filterBy(bookName, parentId, categoryId, publisherId,
                minPrice, maxPrice);
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
    public List<Book> getBooksByPublisher(Integer publisherId) {
        return bookRepository.findByPublisherId(publisherId);
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

            Book book = bookMapper.toEntity(request);

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

            // Set publisher if provided
            if (request.getPublisherId() != null) {
                Publisher publisher = publisherRepository.findById(request.getPublisherId()).orElse(null);
                if (publisher == null) {
                    return new ApiResponse<>(404, "Không tìm thấy nhà xuất bản", null);
                }
                book.setPublisher(publisher);
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

            // Update new book detail fields
            if (request.getCoverImageUrl() != null) {
                existing.setCoverImageUrl(request.getCoverImageUrl());
            }
            if (request.getTranslator() != null) {
                existing.setTranslator(request.getTranslator());
            }
            if (request.getIsbn() != null) {
                existing.setIsbn(request.getIsbn());
            }
            if (request.getPageCount() != null) {
                existing.setPageCount(request.getPageCount());
            }
            if (request.getLanguage() != null) {
                existing.setLanguage(request.getLanguage());
            }
            if (request.getWeight() != null) {
                existing.setWeight(request.getWeight());
            }
            if (request.getDimensions() != null) {
                existing.setDimensions(request.getDimensions());
            }

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

            // Update publisher if provided
            if (request.getPublisherId() != null) {
                Publisher publisher = publisherRepository.findById(request.getPublisherId()).orElse(null);
                if (publisher == null) {
                    return new ApiResponse<>(404, "Không tìm thấy nhà xuất bản", null);
                }
                existing.setPublisher(publisher);
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

    // ❌ REMOVED: Old getTrendingBooks method - replaced by new TrendingRequest-based method

    // ❌ REMOVED: Old getTrendingBooksWithFallback method - replaced by new getDailyTrendingWithFallback

    /**
     * 🔥 NEW MAIN METHOD: Trending books với TrendingRequest
     * Hỗ trợ 2 loại: DAILY_TRENDING và HOT_DISCOUNT
     */
    @Override
    @Cacheable(value = "trending-books",
            key = "#request.type + '-' + #request.page + '-' + #request.size + '-' + " +
                    "(#request.isDailyTrending() ? 'no-category' : (#request.categoryId != null ? #request.categoryId : 'all')) + '-' + " +
                    "(#request.minPrice != null ? #request.minPrice : '0') + '-' + " +
                    "(#request.maxPrice != null ? #request.maxPrice : 'max')")
    public PaginationResponse<TrendingBookResponse> getTrendingBooks(TrendingRequest request) {
        try {
            // Validate request
            if (!request.isValidType()) {
                throw new IllegalArgumentException("Invalid trending type. Must be DAILY_TRENDING or HOT_DISCOUNT");
            }

            if (request.isHotDiscount()) {
                return getHotDiscountBooks(request);
            } else {
                return getDailyTrendingBooks(request);
            }

        } catch (Exception e) {
            System.err.println("Error getting trending books: " + e.getMessage());
            e.printStackTrace();
            return createEmptyPaginationResponse(request.getPage(), request.getSize());
        }
    }

    /**
     * 🔥 DAILY TRENDING: Xu hướng theo ngày (sales + reviews + recency)
     * ❌ KHÔNG sử dụng categoryId - lấy xu hướng tổng thể
     */
    private PaginationResponse<TrendingBookResponse> getDailyTrendingBooks(TrendingRequest request) {
        long currentTime = System.currentTimeMillis();
        long thirtyDaysAgo = currentTime - (30L * 24 * 60 * 60 * 1000);
        long sixtyDaysAgo = currentTime - (60L * 24 * 60 * 60 * 1000);

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());

        // 🔥 DAILY_TRENDING: Không filter theo category, lấy tổng thể
        Page<Object[]> trendingData = bookRepository.findTrendingBooksData(
                thirtyDaysAgo, sixtyDaysAgo, currentTime,
                null, request.getMinPrice(), request.getMaxPrice(), pageable);

        // 🔥 FALLBACK STRATEGY: Nếu không có đủ dữ liệu từ database thực tế
        if (trendingData.getTotalElements() < request.getSize()) {
            return getDailyTrendingWithFallback(request, trendingData, thirtyDaysAgo, sixtyDaysAgo, currentTime);
        }

        return mapTrendingDataToResponse(trendingData, request.getPage(), request.getSize());
    }

    /**
     * 🔥 HOT DISCOUNT: Sách hot giảm sốc (flash sale + discount cao)
     */
    private PaginationResponse<TrendingBookResponse> getHotDiscountBooks(TrendingRequest request) {
        long currentTime = System.currentTimeMillis();

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());

        // Lấy sách có flash sale hoặc discount cao
        Page<Object[]> hotDiscountData = bookRepository.findHotDiscountBooks(
                currentTime, request.getCategoryId(), request.getMinPrice(), request.getMaxPrice(),
                request.getMinDiscountPercentage(), request.getFlashSaleOnly(), pageable);

        // 🔥 FALLBACK: Nếu không có đủ sách giảm giá, lấy sách có giá tốt
        if (hotDiscountData.getTotalElements() < request.getSize()) {
            return getHotDiscountWithFallback(request, hotDiscountData, currentTime);
        }

        return mapTrendingDataToResponse(hotDiscountData, request.getPage(), request.getSize());
    }

    /**
     * 🔥 FALLBACK cho Daily Trending - Dựa trên dữ liệu thực tế
     */
    private PaginationResponse<TrendingBookResponse> getDailyTrendingWithFallback(
            TrendingRequest request, Page<Object[]> existingTrending,
            long thirtyDaysAgo, long sixtyDaysAgo, long currentTime) {

        List<TrendingBookResponse> allTrendingBooks = new ArrayList<>();

        // 1. Thêm trending thực sự (nếu có)
        if (!existingTrending.isEmpty()) {
            PaginationResponse<TrendingBookResponse> existingResponse =
                    mapTrendingDataToResponse(existingTrending, 0, existingTrending.getContent().size());
            allTrendingBooks.addAll(existingResponse.getContent());
        }

        // 2. Bổ sung từ sách thực tế trong database (DAILY_TRENDING không filter category)
        int needMore = request.getSize() - allTrendingBooks.size();
        if (needMore > 0) {
            List<Object[]> fallbackBooks = bookRepository.findFallbackTrendingBooks(
                    null, request.getMinPrice(), request.getMaxPrice(),
                    PageRequest.of(0, needMore * 2));

            // Lọc bỏ những sách đã có
            Set<Integer> existingBookIds = allTrendingBooks.stream()
                    .map(TrendingBookResponse::getId)
                    .collect(Collectors.toSet());

            Map<Integer, List<AuthorBook>> authorsMap = getAuthorsForBooks(
                    fallbackBooks.stream()
                            .map(data -> (Integer) data[0])
                            .filter(id -> !existingBookIds.contains(id))
                            .limit(needMore)
                            .collect(Collectors.toList())
            );

            int fallbackRank = allTrendingBooks.size() + 1;
            for (Object[] data : fallbackBooks) {
                Integer bookId = (Integer) data[0];
                if (!existingBookIds.contains(bookId) && allTrendingBooks.size() < request.getSize()) {
                    TrendingBookResponse book = trendingBookMapper.mapToFallbackTrendingBookResponse(
                            data, fallbackRank++, authorsMap);
                    allTrendingBooks.add(book);
                }
            }
        }

        // 3. Tính tổng số phần tử dựa trên database thực tế (DAILY_TRENDING không filter category)
        long totalElements = bookRepository.countActiveBooks(
                null, request.getMinPrice(), request.getMaxPrice());

        return PaginationResponse.<TrendingBookResponse>builder()
                .content(allTrendingBooks)
                .pageNumber(request.getPage())
                .pageSize(request.getSize())
                .totalElements(totalElements)
                .totalPages((int) Math.ceil((double) totalElements / request.getSize()))
                .build();
    }

    /**
     * 🔥 FALLBACK cho Hot Discount - Dựa trên dữ liệu thực tế
     */
    private PaginationResponse<TrendingBookResponse> getHotDiscountWithFallback(
            TrendingRequest request, Page<Object[]> existingDiscount, long currentTime) {

        List<TrendingBookResponse> allDiscountBooks = new ArrayList<>();

        // 1. Thêm sách giảm giá thực sự (nếu có)
        if (!existingDiscount.isEmpty()) {
            PaginationResponse<TrendingBookResponse> existingResponse =
                    mapTrendingDataToResponse(existingDiscount, 0, existingDiscount.getContent().size());
            allDiscountBooks.addAll(existingResponse.getContent());
        }

        // 2. Bổ sung từ sách có giá tốt trong database
        int needMore = request.getSize() - allDiscountBooks.size();
        if (needMore > 0) {
            List<Object[]> fallbackBooks = bookRepository.findGoodPriceBooks(
                    request.getCategoryId(), request.getMinPrice(), request.getMaxPrice(),
                    PageRequest.of(0, needMore * 2));

            Set<Integer> existingBookIds = allDiscountBooks.stream()
                    .map(TrendingBookResponse::getId)
                    .collect(Collectors.toSet());

            Map<Integer, List<AuthorBook>> authorsMap = getAuthorsForBooks(
                    fallbackBooks.stream()
                            .map(data -> (Integer) data[0])
                            .filter(id -> !existingBookIds.contains(id))
                            .limit(needMore)
                            .collect(Collectors.toList())
            );

            int fallbackRank = allDiscountBooks.size() + 1;
            for (Object[] data : fallbackBooks) {
                Integer bookId = (Integer) data[0];
                if (!existingBookIds.contains(bookId) && allDiscountBooks.size() < request.getSize()) {
                    TrendingBookResponse book = trendingBookMapper.mapToFallbackTrendingBookResponse(
                            data, fallbackRank++, authorsMap);
                    book.setTrendingScore(Math.min(book.getTrendingScore(), 4.0)); // Hot discount fallback score
                    allDiscountBooks.add(book);
                }
            }
        }

        // 3. Tính tổng số phần tử
        long totalElements = bookRepository.countActiveBooks(
                request.getCategoryId(), request.getMinPrice(), request.getMaxPrice());

        return PaginationResponse.<TrendingBookResponse>builder()
                .content(allDiscountBooks)
                .pageNumber(request.getPage())
                .pageSize(request.getSize())
                .totalElements(totalElements)
                .totalPages((int) Math.ceil((double) totalElements / request.getSize()))
                .build();
    }

    /**
     * Helper: Map trending data to response
     */
    private PaginationResponse<TrendingBookResponse> mapTrendingDataToResponse(
            Page<Object[]> trendingData, int page, int size) {

        Map<Integer, List<AuthorBook>> authorsMap = getAuthorsForBooks(
                trendingData.getContent().stream()
                        .map(data -> (Integer) data[0])
                        .collect(Collectors.toList())
        );

        List<TrendingBookResponse> trendingBooks = new ArrayList<>();
        int rank = page * size + 1;

        for (Object[] data : trendingData.getContent()) {
            TrendingBookResponse book = trendingBookMapper.mapToTrendingBookResponse(
                    data, rank++, authorsMap);
            trendingBooks.add(book);
        }

        return PaginationResponse.<TrendingBookResponse>builder()
                .content(trendingBooks)
                .pageNumber(trendingData.getNumber())
                .pageSize(trendingData.getSize())
                .totalElements(trendingData.getTotalElements())
                .totalPages(trendingData.getTotalPages())
                .build();
    }

    /**
     * Helper: Get authors for books
     */
    private Map<Integer, List<AuthorBook>> getAuthorsForBooks(List<Integer> bookIds) {
        if (bookIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<AuthorBook> authorBooks = authorBookRepository.findByBookIdsWithAuthor(bookIds);
        return authorBooks.stream()
                .collect(Collectors.groupingBy(ab -> ab.getBook().getId()));
    }

    /**
     * Helper: Create empty pagination response
     */
    private PaginationResponse<TrendingBookResponse> createEmptyPaginationResponse(int page, int size) {
        return PaginationResponse.<TrendingBookResponse>builder()
                .content(new ArrayList<>())
                .pageNumber(page)
                .pageSize(size)
                .totalElements(0L)
                .totalPages(0)
                .build();
    }

    /**
     * 🔥 DEPRECATED: Keep for backward compatibility
     * ❌ DAILY_TRENDING không sử dụng categoryId
     */
    @Override
    @Deprecated
    public PaginationResponse<TrendingBookResponse> getTrendingBooks(int page, int size, Integer categoryId,
                                                                     BigDecimal minPrice, BigDecimal maxPrice) {
        TrendingRequest request = new TrendingRequest();
        request.setType("DAILY_TRENDING");
        request.setPage(page);
        request.setSize(size);
        // ❌ DAILY_TRENDING không sử dụng categoryId nữa
        request.setMinPrice(minPrice);
        request.setMaxPrice(maxPrice);

        return getTrendingBooks(request);
    }

    @Override
    public ApiResponse<List<BookCategoryRequest>> getBooksByCategoryId(Integer id, String text) {
        Specification<Book> bookSpecification = BookSpecification.filterBy(id,text);
        List<Book> books = bookRepository.findAll(bookSpecification);


        return new ApiResponse<>(200, "Đã nhập được list search từ ", bookCategoryMapper.booksMapper(books));
    }

    @Override
    public ApiResponse<List<BookSearchRequest>> getBookByName(String text) {
        Specification<Book> bookSpecification = BookSpecification.filterBy(text);
        Pageable pageable = PageRequest.of(0, 5); // Trang đầu tiên (0), 5 bản ghi
        List<Book> books = bookRepository.findAll(bookSpecification, pageable).getContent();


        return new ApiResponse<>(200,"Lấy được books search rồi",bookCategoryMapper.bookSearchMapper(books));
    }


}
