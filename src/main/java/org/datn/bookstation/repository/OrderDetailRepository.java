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
}
