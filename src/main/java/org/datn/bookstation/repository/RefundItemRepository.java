package org.datn.bookstation.repository;

import org.datn.bookstation.entity.RefundItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RefundItemRepository extends JpaRepository<RefundItem, Integer> {
    
    // Tìm các item hoàn trả theo refund request
    List<RefundItem> findByRefundRequestId(Integer refundRequestId);
    
    // Tìm các item hoàn trả theo sách
    List<RefundItem> findByBookId(Integer bookId);
}
