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
    // Bao gồm cả GOODS_RECEIVED_FROM_CUSTOMER vì đây là hàng cần trả về kho
    private static final List<OrderStatus> PROCESSING_STATUSES = List.of(
        OrderStatus.PENDING,
        OrderStatus.CONFIRMED, 
        OrderStatus.SHIPPED,
        OrderStatus.GOODS_RECEIVED_FROM_CUSTOMER  // ✅ THÊM: Hàng đã nhận từ khách, cần xử lý trả về kho
    );
    
    @Override
    public Integer getProcessingQuantity(Integer bookId) {
        return orderDetailRepository.sumQuantityByBookIdAndOrderStatuses(bookId, PROCESSING_STATUSES);
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
