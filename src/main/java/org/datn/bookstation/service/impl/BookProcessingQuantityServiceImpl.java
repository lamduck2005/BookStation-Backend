package org.datn.bookstation.service.impl;

import lombok.AllArgsConstructor;
import org.datn.bookstation.entity.enums.OrderStatus;
import org.datn.bookstation.repository.BookRepository;
import org.datn.bookstation.repository.OrderDetailRepository;
import org.datn.bookstation.service.BookProcessingQuantityService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service tính processing quantity real-time từ database
 * Thay thế cho cột processingQuantity để đảm bảo độ chính xác 100%
 */
@Service
@AllArgsConstructor
public class BookProcessingQuantityServiceImpl implements BookProcessingQuantityService {
    
    private final OrderDetailRepository orderDetailRepository;
    private final BookRepository bookRepository;
    
    // ✅ FIXED: Các trạng thái đơn hàng được coi là "đang xử lý"  
    // LOẠI BỎ TẤT CẢ REFUND-related statuses vì sẽ tính riêng qua RefundItem
    private static final List<OrderStatus> PROCESSING_STATUSES = List.of(
        OrderStatus.PENDING,                        // Chờ xử lý
        OrderStatus.CONFIRMED,                      // Đã xác nhận  
        OrderStatus.SHIPPED,                        // Đang giao hàng
        OrderStatus.DELIVERY_FAILED,                // Giao hàng thất bại
        OrderStatus.REDELIVERING,                   // Đang giao lại
        OrderStatus.RETURNING_TO_WAREHOUSE          // Đang trả về kho
        // ❌ LOẠI BỎ TẤT CẢ: REFUND_REQUESTED, AWAITING_GOODS_RETURN, REFUNDING
        // ❌ KHÔNG BAO GỒM: DELIVERED, REFUNDED, PARTIALLY_REFUNDED, CANCELED, GOODS_RECEIVED_FROM_CUSTOMER, GOODS_RETURNED_TO_WAREHOUSE
    );
    
    @Override
    public Integer getProcessingQuantity(Integer bookId) {
        // 1. Tính từ các trạng thái đang xử lý bình thường (không bao gồm refund statuses)
        Integer processingQuantity = orderDetailRepository.sumQuantityByBookIdAndOrderStatuses(bookId, PROCESSING_STATUSES);
        
        // 2. ✅ Cộng CHÍNH XÁC số lượng RefundItem đang active (PENDING hoặc APPROVED)
        Integer activeRefundQuantity = orderDetailRepository.sumActiveRefundQuantityByBookId(bookId);
        
        int total = (processingQuantity != null ? processingQuantity : 0) + 
                   (activeRefundQuantity != null ? activeRefundQuantity : 0);
        
        return total;
    }
    
    @Override
    public Integer getFlashSaleProcessingQuantity(Integer flashSaleItemId) {
        return orderDetailRepository.sumQuantityByFlashSaleItemIdAndOrderStatuses(flashSaleItemId, PROCESSING_STATUSES);
    }
    
    @Override
    public Map<Integer, Integer> getProcessingQuantities(List<Integer> bookIds) {
        if (bookIds == null || bookIds.isEmpty()) {
            return new HashMap<>();
        }
        
        List<Object[]> results = orderDetailRepository.sumQuantityByBookIdsAndOrderStatuses(bookIds, PROCESSING_STATUSES);
        Map<Integer, Integer> processingMap = new HashMap<>();
        
        // Khởi tạo tất cả bookId với giá trị 0
        for (Integer bookId : bookIds) {
            processingMap.put(bookId, 0);
        }
        
        // Cập nhật với kết quả từ database
        for (Object[] result : results) {
            Integer bookId = (Integer) result[0];
            Long quantity = (Long) result[1];
            processingMap.put(bookId, quantity != null ? quantity.intValue() : 0);
        }
        
        return processingMap;
    }
    
    @Override
    public boolean hasAvailableStock(Integer bookId, Integer requestedQuantity) {
        var book = bookRepository.findById(bookId).orElse(null);
        if (book == null) {
            return false;
        }
        
        int currentStock = book.getStockQuantity();
        int processingQuantity = getProcessingQuantity(bookId);
        int availableStock = currentStock - processingQuantity;
        
        return availableStock >= requestedQuantity;
    }
}
