package org.datn.bookstation.repository;

import org.datn.bookstation.entity.Book;
import org.datn.bookstation.entity.FlashSaleItem;
import org.datn.bookstation.dto.request.FlashSaleItemBookRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FlashSaleItemRepository
                extends JpaRepository<FlashSaleItem, Integer>, JpaSpecificationExecutor<FlashSaleItem> {

        @Query("SELECT fsi FROM FlashSaleItem fsi WHERE fsi.flashSale.id = :flashSaleId")
        List<FlashSaleItem> findByFlashSaleId(@Param("flashSaleId") Integer flashSaleId);

        @Query("SELECT fsi FROM FlashSaleItem fsi WHERE fsi.book.id = :bookId")
        List<FlashSaleItem> findByBookId(@Param("bookId") Integer bookId);

        @Query("SELECT fsi FROM FlashSaleItem fsi WHERE fsi.flashSale.id = :flashSaleId AND fsi.status = 1")
        List<FlashSaleItem> findActiveByFlashSaleId(@Param("flashSaleId") Integer flashSaleId);

    boolean existsByFlashSaleIdAndBookId(Integer flashSaleId, Integer bookId);
    boolean existsByFlashSaleIdAndBookIdAndIdNot(Integer flashSaleId, Integer bookId, Integer id);
    
    /**
     * Lấy thông tin flash sale hiện tại của sách (đang active và trong thời gian hiệu lực)
     */
    @Query("SELECT fsi FROM FlashSaleItem fsi " +
           "WHERE fsi.book.id = :bookId " +
           "AND fsi.status = 1 " +
           "AND fsi.flashSale.status = 1 " +
           "AND fsi.flashSale.startTime <= :currentTime " +
           "AND fsi.flashSale.endTime >= :currentTime " +
           "ORDER BY fsi.flashSale.startTime DESC")
    List<FlashSaleItem> findCurrentActiveFlashSaleByBookId(@Param("bookId") Integer bookId, @Param("currentTime") Long currentTime);
    
    /**
     * Đếm số lượng đã bán trong flash sale của một sách
     */
    @Query("SELECT COALESCE(SUM(od.quantity), 0) FROM OrderDetail od " +
           "WHERE od.flashSaleItem.id = :flashSaleItemId " +
           "AND od.order.orderStatus != 'CANCELLED'")
    Integer countSoldQuantityByFlashSaleItem(@Param("flashSaleItemId") Integer flashSaleItemId);
    
    // Bổ sung methods hỗ trợ Cart
    
    /**
     * Tìm flash sale đang active cho một sách (sử dụng Long timestamp)
     */
    @Query("SELECT fsi FROM FlashSaleItem fsi " +
           "WHERE fsi.book.id = :bookId " +
           "AND fsi.status = 1 " +
           "AND fsi.flashSale.status = 1 " +
           "AND fsi.flashSale.startTime <= :now " +
           "AND fsi.flashSale.endTime >= :now " +
           "ORDER BY fsi.discountPrice ASC")
    List<FlashSaleItem> findActiveFlashSalesByBookId(@Param("bookId") Long bookId, @Param("now") Long now);
    
    /**
     * Tìm flash sale item theo ID và kiểm tra còn active không
     */
    @Query("SELECT fsi FROM FlashSaleItem fsi " +
           "WHERE fsi.id = :id " +
           "AND fsi.status = 1 " +
           "AND fsi.flashSale.status = 1 " +
           "AND fsi.flashSale.startTime <= :now " +
           "AND fsi.flashSale.endTime >= :now")
    Optional<FlashSaleItem> findActiveFlashSaleItemById(@Param("id") Long id, @Param("now") Long now);
    
    /**
     * Tìm flash sale item theo ID
     */
    Optional<FlashSaleItem> findById(Long id);
    
    /**
     * ✅ FIX LAZY LOADING: Lấy tất cả FlashSaleItem với FlashSale được fetch sẵn
     */
    @Query("SELECT fsi FROM FlashSaleItem fsi JOIN FETCH fsi.flashSale")
    List<FlashSaleItem> findAllWithFlashSale();
    
    /**
     * ✅ FIX LAZY LOADING: Lấy FlashSaleItem theo flashSaleId với FlashSale được fetch sẵn
     */
    @Query("SELECT fsi FROM FlashSaleItem fsi JOIN FETCH fsi.flashSale WHERE fsi.flashSale.id = :flashSaleId")
    List<FlashSaleItem> findByFlashSaleIdWithFlashSale(@Param("flashSaleId") Integer flashSaleId);
    
    /**
     * ✅ ADMIN CẦN: Tìm Flash Sale đang active cho book (dùng cho BookResponseMapper)
     */
    @Query("SELECT fsi FROM FlashSaleItem fsi " +
           "WHERE fsi.book.id = :bookId " +
           "AND fsi.status = 1 " +
           "AND fsi.flashSale.status = 1 " +
           "AND fsi.flashSale.startTime <= :currentTime " +
           "AND fsi.flashSale.endTime >= :currentTime " +
           "ORDER BY fsi.discountPrice ASC")
    FlashSaleItem findActiveFlashSaleByBook(@Param("bookId") Integer bookId, @Param("currentTime") Long currentTime);
    
    /**
     * Helper method với current time tự động
     */
    default FlashSaleItem findActiveFlashSaleByBook(Integer bookId) {
        return findActiveFlashSaleByBook(bookId, System.currentTimeMillis());
    }

    
        /**
         * Lấy danh sách tất cả sách (Book) hiện đang trong flash‑sale còn hiệu lực.
         */
        @Query("""
                            SELECT new org.datn.bookstation.dto.request.FlashSaleItemBookRequest(
                                b.id,
                                b.bookName,
                                b.description,
                                b.price,
                                b.stockQuantity,
                                b.publicationDate,
                                b.bookCode,
                                b.status,
                                c.id,
                                c.categoryName,
                                b.coverImageUrl,
                                b.discountValue,
                                b.discountPercent,
                                b.discountActive,
                                fsi.id,
                                                        fsi.discountPrice,fsi.discountPercentage
                              
                            )
                            FROM FlashSaleItem fsi
                            JOIN fsi.book b
                            JOIN b.category c
                            WHERE fsi.status = 1
                              AND fsi.flashSale.status = 1
                              AND fsi.flashSale.startTime <= :now
                              AND fsi.flashSale.endTime >= :now
                        """)
        List<FlashSaleItemBookRequest> findAllBookFlashSaleDTO(@Param("now") Long now);

}
