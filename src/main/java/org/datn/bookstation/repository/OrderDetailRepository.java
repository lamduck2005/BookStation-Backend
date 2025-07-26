package org.datn.bookstation.repository;

import org.datn.bookstation.entity.OrderDetail;
import org.datn.bookstation.entity.OrderDetailId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, OrderDetailId> {
    
    @Query("SELECT od FROM OrderDetail od WHERE od.order.id = :orderId")
    List<OrderDetail> findByOrderId(@Param("orderId") Integer orderId);
    
    @Query("SELECT od FROM OrderDetail od WHERE od.book.id = :bookId")
    List<OrderDetail> findByBookId(@Param("bookId") Integer bookId);
    
    // ✅ THÊM MỚI: Tìm order detail cụ thể theo orderId và bookId
    @Query("SELECT od FROM OrderDetail od WHERE od.order.id = :orderId AND od.book.id = :bookId")
    OrderDetail findByOrderIdAndBookId(@Param("orderId") Integer orderId, @Param("bookId") Integer bookId);
    
    // ✅ ADMIN CẦN: Đếm tổng số lượng đã bán của một book
    @Query("SELECT COALESCE(SUM(od.quantity), 0) FROM OrderDetail od " +
           "JOIN od.order o WHERE od.book.id = :bookId AND o.status IN (2, 3, 4)")
    Integer countSoldQuantityByBook(@Param("bookId") Integer bookId);

    @Query("SELECT COUNT(od) > 0 FROM OrderDetail od WHERE od.order.user.id = :userId AND od.book.id = :bookId AND od.order.orderStatus = org.datn.bookstation.entity.enums.OrderStatus.DELIVERED")
    boolean existsDeliveredByUserAndBook(@Param("userId") Integer userId, @Param("bookId") Integer bookId);
    
    /**
     * ✅ Tính số lượng flash sale item mà user đã mua thực sự
     * DELIVERED - GOODS_RECEIVED_FROM_CUSTOMER/GOODS_RETURNED_TO_WAREHOUSE
     */
    @Query("SELECT COALESCE(" +
           "(SELECT SUM(delivered.quantity) FROM OrderDetail delivered " +
           " WHERE delivered.flashSaleItem.id = :flashSaleItemId " +
           " AND delivered.order.user.id = :userId " +
           " AND delivered.order.orderStatus = 'DELIVERED') - " +
           "COALESCE((SELECT SUM(refunded.quantity) FROM OrderDetail refunded " +
           " WHERE refunded.flashSaleItem.id = :flashSaleItemId " +
           " AND refunded.order.user.id = :userId " +
           " AND refunded.order.orderStatus IN ('GOODS_RECEIVED_FROM_CUSTOMER', 'GOODS_RETURNED_TO_WAREHOUSE')), 0), 0)")
    Integer calculateUserPurchasedQuantityForFlashSaleItem(@Param("flashSaleItemId") Integer flashSaleItemId, @Param("userId") Integer userId);
}
