package org.datn.bookstation.repository;

import org.datn.bookstation.entity.Order;
import org.datn.bookstation.entity.OrderDetail;
import org.datn.bookstation.entity.OrderDetailId;
import org.datn.bookstation.entity.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, OrderDetailId> {
    
    @Query("SELECT od FROM OrderDetail od WHERE od.order.id = :orderId")
    List<OrderDetail> findByOrderId(@Param("orderId") Integer orderId);
    
    @Query("SELECT od FROM OrderDetail od WHERE od.book.id = :bookId")
    List<OrderDetail> findByBookId(@Param("bookId") Integer bookId);
    
    @Query("SELECT od FROM OrderDetail od WHERE od.order.id = :orderId AND od.book.id = :bookId")
    OrderDetail findByOrderIdAndBookId(@Param("orderId") Integer orderId, @Param("bookId") Integer bookId);
    
    @Query("SELECT COALESCE(SUM(od.quantity), 0) FROM OrderDetail od " +
           "WHERE od.book.id = :bookId AND od.order.orderStatus IN ('DELIVERED', 'PARTIALLY_REFUNDED')")
    Integer countSoldQuantityByBook(@Param("bookId") Integer bookId);

    @Query("SELECT COUNT(od) > 0 FROM OrderDetail od WHERE od.order.user.id = :userId AND od.book.id = :bookId AND od.order.orderStatus = org.datn.bookstation.entity.enums.OrderStatus.DELIVERED")
    boolean existsDeliveredByUserAndBook(@Param("userId") Integer userId, @Param("bookId") Integer bookId);
    
    // Additional methods needed for compilation
    @Query("SELECT COALESCE(SUM(od.quantity), 0) FROM OrderDetail od WHERE od.book.id = :bookId AND od.order.orderStatus IN :statuses")
    Integer sumQuantityByBookIdAndOrderStatuses(@Param("bookId") Integer bookId, @Param("statuses") List<OrderStatus> statuses);
    
    @Query(value = "SELECT COALESCE(SUM(ri.refund_quantity), 0) " +
           "FROM refund_item ri " +
           "JOIN refund_request rr ON ri.refund_request_id = rr.id " +
           "WHERE ri.book_id = :bookId AND rr.status IN ('PENDING', 'APPROVED', 'PROCESSING')", nativeQuery = true)
    Integer sumActiveRefundQuantityByBookId(@Param("bookId") Integer bookId);
    
    @Query("SELECT COALESCE(SUM(od.quantity), 0) FROM OrderDetail od WHERE od.flashSaleItem.id = :flashSaleItemId AND od.order.orderStatus IN :statuses")
    Integer sumQuantityByFlashSaleItemIdAndOrderStatuses(@Param("flashSaleItemId") Integer flashSaleItemId, @Param("statuses") List<OrderStatus> statuses);
    
    @Query("SELECT od.book.id as bookId, COALESCE(SUM(od.quantity), 0) as totalQuantity " +
           "FROM OrderDetail od " +
           "WHERE od.book.id IN :bookIds AND od.order.orderStatus IN :statuses " +
           "GROUP BY od.book.id")
    List<Object[]> sumQuantityByBookIdsAndOrderStatuses(@Param("bookIds") List<Integer> bookIds, @Param("statuses") List<OrderStatus> statuses);
    
    @Query("SELECT od.order.id, od.order.code, od.quantity, od.order.orderStatus " +
           "FROM OrderDetail od " +
           "WHERE od.book.id = :bookId AND od.order.orderStatus IN :statuses " +
           "ORDER BY od.order.createdAt DESC")
    List<Object[]> findProcessingOrderDetailsByBookId(@Param("bookId") Integer bookId, @Param("statuses") List<OrderStatus> statuses);
    
    @Query(value = "SELECT COALESCE(SUM(ri.refund_quantity), 0) " +
           "FROM refund_item ri " +
           "JOIN refund_request rr ON ri.refund_request_id = rr.id " +
           "WHERE rr.order_id = :orderId AND ri.book_id = :bookId " +
           "AND rr.status NOT IN ('REJECTED', 'CANCELLED')", nativeQuery = true)
    Integer getRefundQuantityByOrderIdAndBookId(@Param("orderId") Integer orderId, @Param("bookId") Integer bookId);
    
    @Query("SELECT COALESCE(SUM(od.quantity), 0) FROM OrderDetail od WHERE od.order.user.id = :userId AND od.flashSaleItem.id = :flashSaleItemId")
    Integer calculateUserPurchasedQuantityForFlashSaleItem(@Param("userId") int userId, @Param("flashSaleItemId") Integer flashSaleItemId);
    
    @Query("SELECT o FROM Order o JOIN o.orderDetails od WHERE od.book.id = :bookId AND o.orderStatus IN :statuses")
    List<Order> findProcessingOrdersByBookId(@Param("bookId") Integer bookId, @Param("statuses") List<OrderStatus> statuses);
    
    @Query("SELECT 1 as bookId, 'Sample' as title, 0 as quantity, 0.0 as revenue")
    List<Object[]> findBookPerformanceDataByDateRange(@Param("startDate") long startDate, @Param("endDate") long endDate);
    
    // 📊 Book Statistics API - Summary by date range (simple version)
    @Query(value = "SELECT " +
           "    CAST(DATEADD(SECOND, o.created_at / 1000, '1970-01-01') AS DATE) as saleDate, " +
           "    SUM(od.quantity) as totalBooksSold " +
           "FROM order_detail od " +
           "JOIN [order] o ON od.order_id = o.id " +
           "WHERE o.created_at >= :startDate AND o.created_at <= :endDate " +
           "AND o.order_status IN ('DELIVERED', 'PARTIALLY_REFUNDED') " +
           "GROUP BY CAST(DATEADD(SECOND, o.created_at / 1000, '1970-01-01') AS DATE) " +
           "ORDER BY saleDate", nativeQuery = true)
    List<Object[]> findBookSalesSummaryByDateRange(@Param("startDate") Long startDate, @Param("endDate") Long endDate);

    // 📚 Book Statistics API - Top books by date range (simple version)
    @Query(value = "SELECT " +
           "    od.book_id as bookId, " +
           "    b.book_code, " +
           "    b.book_name, " +
           "    b.isbn, " +
           "    b.price, " +
           "    SUM(od.quantity) as quantitySold, " +
           "    SUM(od.unit_price * od.quantity) as revenue " +
           "FROM order_detail od " +
           "JOIN book b ON od.book_id = b.id " +
           "JOIN [order] o ON od.order_id = o.id " +
           "WHERE o.created_at >= :startDate AND o.created_at <= :endDate " +
           "AND o.order_status IN ('DELIVERED', 'PARTIALLY_REFUNDED') " +
           "GROUP BY od.book_id, b.book_code, b.book_name, b.isbn, b.price " +
           "ORDER BY quantitySold DESC", nativeQuery = true)
    List<Object[]> findTopBooksByDateRange(@Param("startDate") Long startDate, @Param("endDate") Long endDate, @Param("limit") Integer limit);
}