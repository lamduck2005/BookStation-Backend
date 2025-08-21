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
    // CHỈ LOẠI TRỪ những trạng thái đã hoàn tất HOÀN TOÀN
    private static final List<OrderStatus> PROCESSING_STATUSES = List.of(
        OrderStatus.PENDING,                        // Chờ xử lý
        OrderStatus.CONFIRMED,                      // Đã xác nhận  
        OrderStatus.SHIPPED,                        // Đang giao hàng
        OrderStatus.DELIVERY_FAILED,                // Giao hàng thất bại
        OrderStatus.REDELIVERING,                   // Đang giao lại
        OrderStatus.RETURNING_TO_WAREHOUSE,         // Đang trả về kho
        OrderStatus.REFUND_REQUESTED,               // Yêu cầu hoàn trả
        OrderStatus.AWAITING_GOODS_RETURN,          // Chờ hàng trả về
        OrderStatus.REFUNDING,                      // Đang hoàn trả
        OrderStatus.GOODS_RECEIVED_FROM_CUSTOMER,   // ✅ Đã nhận hàng từ khách (còn phải hoàn tiền)
        OrderStatus.GOODS_RETURNED_TO_WAREHOUSE     // ✅ Hàng đã về kho (còn phải hoàn tiền)
        // ❌ CHỈ LOẠI TRỪ: DELIVERED, REFUNDED, PARTIALLY_REFUNDED, CANCELED (đã hoàn tất hoàn toàn)
    );
    
    @Override
    public Integer getProcessingQuantity(Integer bookId) {
        // ✅ SỬ DỤNG CÙNG LOGIC NHU BookServiceImpl.calculateActualProcessingQuantity()
        // Lấy danh sách tất cả đơn hàng đang processing cho bookId này
        List<Object[]> processingOrders = orderDetailRepository.findProcessingOrderDetailsByBookId(bookId, PROCESSING_STATUSES);
        
        System.out.println("📊 DEBUG Book ID " + bookId + " - Found " + processingOrders.size() + " orders");
        
        int totalProcessingQuantity = 0;
        
        for (Object[] row : processingOrders) {
            Integer orderId = (Integer) row[0];
            Integer orderDetailQuantity = (Integer) row[2];
            OrderStatus orderStatus = (OrderStatus) row[3];
            
            // Lấy refund quantity cho đơn này
            Integer refundQuantity = orderDetailRepository.getRefundQuantityByOrderIdAndBookId(orderId, bookId);
            if (refundQuantity == 0) refundQuantity = null; // Convert 0 thành null để logic xử lý đúng
            
            System.out.println("🔍 Order " + orderId + ": qty=" + orderDetailQuantity + ", status=" + orderStatus + ", refundQty=" + refundQuantity);
            
            // Sử dụng cùng logic như BookServiceImpl.calculateActualProcessingQuantity()
            int processingQuantityForThisOrder;
            if (isRefundRelatedStatus(orderStatus) && refundQuantity != null && refundQuantity > 0) {
                // ✅ LOGIC MỚI: Phân biệt hoàn 1 phần vs hoàn toàn phần
                if (refundQuantity.equals(orderDetailQuantity)) {
                    // HOÀN TOÀN PHẦN: refund quantity = order quantity → hiển thị full order quantity  
                    processingQuantityForThisOrder = orderDetailQuantity;
                    System.out.println("🟢 Full refund case: using orderQty=" + processingQuantityForThisOrder);
                } else {
                    // HOÀN 1 PHẦN: refund quantity < order quantity → chỉ hiển thị refund quantity
                    processingQuantityForThisOrder = refundQuantity;
                    System.out.println("� Partial refund case: using refundQty=" + processingQuantityForThisOrder);
                }
            } else if (orderStatus == OrderStatus.REFUND_REQUESTED && (refundQuantity == null || refundQuantity == 0)) {
                // ✅ FIXED LOGIC: Refund_request tồn tại nhưng không có refund_item = Full refund
                // Trả về toàn bộ order quantity
                processingQuantityForThisOrder = orderDetailQuantity;
                System.out.println("🔄 Full refund case (no refund_item): using orderQty=" + processingQuantityForThisOrder);
            } else {
                // Đơn bình thường: tính full quantity
                processingQuantityForThisOrder = orderDetailQuantity;
                System.out.println("⚪ Normal case: using orderQty=" + processingQuantityForThisOrder);
            }
            
            totalProcessingQuantity += processingQuantityForThisOrder;
            System.out.println("📈 Running total: " + totalProcessingQuantity);
        }
        
        System.out.println("🎯 FINAL RESULT: " + totalProcessingQuantity);
        return totalProcessingQuantity;
    }
    
    /**
     * Kiểm tra trạng thái có liên quan đến hoàn trả không
     * (Copy từ BookServiceImpl để đảm bảo consistency)
     */
    private boolean isRefundRelatedStatus(OrderStatus status) {
        return status == OrderStatus.REFUND_REQUESTED ||
               status == OrderStatus.AWAITING_GOODS_RETURN ||
               status == OrderStatus.GOODS_RECEIVED_FROM_CUSTOMER ||
               status == OrderStatus.GOODS_RETURNED_TO_WAREHOUSE ||
               status == OrderStatus.REFUNDING;
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
