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
    
    // ✅ ADMIN CẦN: Đếm tổng số lượng đã bán của một book
    @Query("SELECT COALESCE(SUM(od.quantity), 0) FROM OrderDetail od " +
           "JOIN od.order o WHERE od.book.id = :bookId AND o.status IN (2, 3, 4)")
    Integer countSoldQuantityByBook(@Param("bookId") Integer bookId);
}
