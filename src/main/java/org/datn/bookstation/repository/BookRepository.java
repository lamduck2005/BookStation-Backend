package org.datn.bookstation.repository;

import org.datn.bookstation.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
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
    
    @Query("SELECT b FROM Book b WHERE b.publisher.id = :publisherId")
    List<Book> findByPublisherId(@Param("publisherId") Integer publisherId);
    
    @Query("SELECT b FROM Book b WHERE b.status = 1 ORDER BY b.createdAt DESC")
    List<Book> findActiveBooks();
    
    /**
     * Lấy dữ liệu cho trending books với thông tin thống kê
     * Bao gồm: thông tin cơ bản của sách, số lượng đã bán, số đơn hàng, rating trung bình, số review
     */
    @Query("""
        SELECT b.id as bookId,
               b.bookName as bookName,
               b.description as description, 
               b.price as price,
               b.stockQuantity as stockQuantity,
               b.bookCode as bookCode,
               b.publicationDate as publicationDate,
               b.createdAt as createdAt,
               b.updatedAt as updatedAt,
               b.category.id as categoryId,
               b.category.categoryName as categoryName,
               b.supplier.id as supplierId,
               b.supplier.supplierName as supplierName,
               COALESCE(salesData.soldCount, 0) as soldCount,
               COALESCE(salesData.orderCount, 0) as orderCount,
               COALESCE(reviewData.avgRating, 0.0) as avgRating,
               COALESCE(reviewData.reviewCount, 0) as reviewCount,
               CASE WHEN flashSale.id IS NOT NULL THEN true ELSE false END as isInFlashSale,
               flashSale.discountPrice as flashSalePrice,
               flashSale.stockQuantity as flashSaleStockQuantity
        FROM Book b
        LEFT JOIN (
            SELECT od.book.id as bookId,
                   SUM(od.quantity) as soldCount,
                   COUNT(DISTINCT od.order.id) as orderCount
            FROM OrderDetail od 
            WHERE od.order.createdAt >= :thirtyDaysAgo
                  AND od.order.orderStatus = 'COMPLETED'
            GROUP BY od.book.id
        ) salesData ON b.id = salesData.bookId
        LEFT JOIN (
            SELECT r.book.id as bookId,
                   AVG(CAST(r.rating as double)) as avgRating,
                   COUNT(r.id) as reviewCount
            FROM Review r 
            WHERE r.reviewStatus = 'APPROVED'
                  AND r.createdAt >= :sixtyDaysAgo
            GROUP BY r.book.id
        ) reviewData ON b.id = reviewData.bookId
        LEFT JOIN (
            SELECT fsi.book.id as bookId,
                   fsi.id as id,
                   fsi.discountPrice as discountPrice,
                   fsi.stockQuantity as stockQuantity
            FROM FlashSaleItem fsi
            JOIN FlashSale fs ON fsi.flashSale.id = fs.id
            WHERE fs.status = 1 
                  AND fsi.status = 1
                  AND fs.startTime <= :currentTime 
                  AND fs.endTime >= :currentTime
        ) flashSale ON b.id = flashSale.bookId
        WHERE b.status = 1 
              AND b.stockQuantity > 0
              AND (:categoryId IS NULL OR b.category.id = :categoryId)
              AND (:minPrice IS NULL OR b.price >= :minPrice)
              AND (:maxPrice IS NULL OR b.price <= :maxPrice)
        ORDER BY (
            (COALESCE(salesData.soldCount, 0) * 0.4) +
            (COALESCE(reviewData.avgRating, 0) * COALESCE(reviewData.reviewCount, 0) * 0.3) +
            (CASE WHEN b.createdAt >= :thirtyDaysAgo THEN 10 ELSE 0 END * 0.2) +
            (CASE WHEN flashSale.id IS NOT NULL THEN 10 ELSE 0 END * 0.1)
        ) DESC
        """)
    Page<Object[]> findTrendingBooksData(
        @Param("thirtyDaysAgo") Long thirtyDaysAgo,
        @Param("sixtyDaysAgo") Long sixtyDaysAgo, 
        @Param("currentTime") Long currentTime,
        @Param("categoryId") Integer categoryId,
        @Param("minPrice") BigDecimal minPrice,
        @Param("maxPrice") BigDecimal maxPrice,
        Pageable pageable
    );
    
    /**
     * Đếm tổng số sách đủ điều kiện trending
     */
    @Query("""
        SELECT COUNT(DISTINCT b.id)
        FROM Book b
        WHERE b.status = 1 
              AND b.stockQuantity > 0
              AND (:categoryId IS NULL OR b.category.id = :categoryId)
              AND (:minPrice IS NULL OR b.price >= :minPrice)
              AND (:maxPrice IS NULL OR b.price <= :maxPrice)
        """)
    Long countTrendingBooks(
        @Param("categoryId") Integer categoryId,
        @Param("minPrice") BigDecimal minPrice,
        @Param("maxPrice") BigDecimal maxPrice
    );

    /**
     *  FALLBACK: Lấy sách theo thuật toán dự phòng khi chưa có đủ dữ liệu trending
     * Ưu tiên: Sách mới → Giá tốt → Stock nhiều → Ngẫu nhiên
     */
    @Query("""
        SELECT b.id as bookId,
               b.bookName as bookName,
               b.description as description,
               b.price as price,
               b.stockQuantity as stockQuantity,
               b.bookCode as bookCode,
               b.publicationDate as publicationDate,
               b.createdAt as createdAt,
               b.updatedAt as updatedAt,
               b.category.id as categoryId,
               b.category.categoryName as categoryName,
               b.supplier.id as supplierId,
               b.supplier.supplierName as supplierName,
               0 as soldCount,
               0 as orderCount,
               0.0 as avgRating,
               0 as reviewCount,
               false as isInFlashSale,
               NULL as flashSalePrice,
               NULL as flashSaleStockQuantity
        FROM Book b
        WHERE b.status = 1 
              AND b.stockQuantity > 0
              AND (:categoryId IS NULL OR b.category.id = :categoryId)
              AND (:minPrice IS NULL OR b.price >= :minPrice)
              AND (:maxPrice IS NULL OR b.price <= :maxPrice)
        ORDER BY 
            b.createdAt DESC,
            b.price ASC,
            b.stockQuantity DESC,
            b.id DESC
        """)
    List<Object[]> findFallbackTrendingBooks(
        @Param("categoryId") Integer categoryId,
        @Param("minPrice") BigDecimal minPrice,
        @Param("maxPrice") BigDecimal maxPrice,
        Pageable pageable
    );

    /**
     * Đếm tổng số sách active
     */
    @Query("""
        SELECT COUNT(b.id)
        FROM Book b
        WHERE b.status = 1 
              AND b.stockQuantity > 0
              AND (:categoryId IS NULL OR b.category.id = :categoryId)
              AND (:minPrice IS NULL OR b.price >= :minPrice)
              AND (:maxPrice IS NULL OR b.price <= :maxPrice)
        """)
    Long countActiveBooks(
        @Param("categoryId") Integer categoryId,
        @Param("minPrice") BigDecimal minPrice,
        @Param("maxPrice") BigDecimal maxPrice
    );

    /**
     *  HOT DISCOUNT: Lấy sách hot giảm sốc (flash sale + discount cao)
     */
    @Query("""
        SELECT b.id as bookId,
               b.bookName as bookName,
               b.description as description,
               b.price as price,
               b.stockQuantity as stockQuantity,
               b.bookCode as bookCode,
               b.publicationDate as publicationDate,
               b.createdAt as createdAt,
               b.updatedAt as updatedAt,
               b.category.id as categoryId,
               b.category.categoryName as categoryName,
               b.supplier.id as supplierId,
               b.supplier.supplierName as supplierName,
               COALESCE(salesData.soldCount, 0) as soldCount,
               COALESCE(salesData.orderCount, 0) as orderCount,
               COALESCE(reviewData.avgRating, 0.0) as avgRating,
               COALESCE(reviewData.reviewCount, 0) as reviewCount,
               CASE WHEN flashSale.id IS NOT NULL THEN true ELSE false END as isInFlashSale,
               flashSale.discountPrice as flashSalePrice,
               flashSale.stockQuantity as flashSaleStockQuantity
        FROM Book b
        LEFT JOIN (
            SELECT od.book.id as bookId,
                   SUM(od.quantity) as soldCount,
                   COUNT(DISTINCT od.order.id) as orderCount
            FROM OrderDetail od 
            WHERE od.order.orderStatus = 'COMPLETED'
            GROUP BY od.book.id
        ) salesData ON b.id = salesData.bookId
        LEFT JOIN (
            SELECT r.book.id as bookId,
                   AVG(CAST(r.rating as double)) as avgRating,
                   COUNT(r.id) as reviewCount
            FROM Review r 
            WHERE r.reviewStatus = 'APPROVED'
            GROUP BY r.book.id
        ) reviewData ON b.id = reviewData.bookId
        LEFT JOIN (
            SELECT fsi.book.id as bookId,
                   fsi.id as id,
                   fsi.discountPrice as discountPrice,
                   fsi.stockQuantity as stockQuantity
            FROM FlashSaleItem fsi
            JOIN FlashSale fs ON fsi.flashSale.id = fs.id
            WHERE fs.status = 1 
                  AND fsi.status = 1
                  AND fs.startTime <= :currentTime 
                  AND fs.endTime >= :currentTime
        ) flashSale ON b.id = flashSale.bookId
        WHERE b.status = 1 
              AND b.stockQuantity > 0
              AND (:categoryId IS NULL OR b.category.id = :categoryId)
              AND (:minPrice IS NULL OR b.price >= :minPrice)
              AND (:maxPrice IS NULL OR b.price <= :maxPrice)
              AND (
                  (:flashSaleOnly = true AND flashSale.id IS NOT NULL) OR
                  (:flashSaleOnly = false AND (
                      flashSale.id IS NOT NULL OR 
                      (:minDiscountPercentage IS NULL OR 
                       ((b.price - COALESCE(flashSale.discountPrice, b.price)) / b.price * 100) >= :minDiscountPercentage)
                  ))
              )
        ORDER BY 
            CASE WHEN flashSale.id IS NOT NULL THEN 1 ELSE 2 END,
            ((b.price - COALESCE(flashSale.discountPrice, b.price)) / b.price * 100) DESC,
            COALESCE(salesData.soldCount, 0) DESC,
            COALESCE(reviewData.avgRating, 0) DESC
        """)
    Page<Object[]> findHotDiscountBooks(
        @Param("currentTime") Long currentTime,
        @Param("categoryId") Integer categoryId,
        @Param("minPrice") BigDecimal minPrice,
        @Param("maxPrice") BigDecimal maxPrice,
        @Param("minDiscountPercentage") Integer minDiscountPercentage,
        @Param("flashSaleOnly") Boolean flashSaleOnly,
        Pageable pageable
    );

    /**
     *  FALLBACK: Lấy sách có giá tốt (cho hot discount fallback)
     */
    @Query("""
        SELECT b.id as bookId,
               b.bookName as bookName,
               b.description as description,
               b.price as price,
               b.stockQuantity as stockQuantity,
               b.bookCode as bookCode,
               b.publicationDate as publicationDate,
               b.createdAt as createdAt,
               b.updatedAt as updatedAt,
               b.category.id as categoryId,
               b.category.categoryName as categoryName,
               b.supplier.id as supplierId,
               b.supplier.supplierName as supplierName,
               0 as soldCount,
               0 as orderCount,
               0.0 as avgRating,
               0 as reviewCount,
               false as isInFlashSale,
               NULL as flashSalePrice,
               NULL as flashSaleStockQuantity
        FROM Book b
        WHERE b.status = 1 
              AND b.stockQuantity > 0
              AND (:categoryId IS NULL OR b.category.id = :categoryId)
              AND (:minPrice IS NULL OR b.price >= :minPrice)
              AND (:maxPrice IS NULL OR b.price <= :maxPrice)
        ORDER BY 
            b.price ASC,
            b.stockQuantity DESC,
            b.createdAt DESC
        """)
    List<Object[]> findGoodPriceBooks(
        @Param("categoryId") Integer categoryId,
        @Param("minPrice") BigDecimal minPrice,
        @Param("maxPrice") BigDecimal maxPrice,
        Pageable pageable
    );
}
