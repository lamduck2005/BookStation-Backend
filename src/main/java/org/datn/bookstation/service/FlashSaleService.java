package org.datn.bookstation.service;

import org.datn.bookstation.dto.request.FlashSaleRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.dto.response.FlashSaleResponse;
import org.datn.bookstation.dto.response.FlashSaleInfoResponse;
import org.datn.bookstation.entity.FlashSaleItem;
import java.util.Optional;

public interface FlashSaleService {
    ApiResponse<PaginationResponse<FlashSaleResponse>> getAllFlashSaleWithPagination(int page, int size);
    ApiResponse<PaginationResponse<FlashSaleResponse>> getAllWithFilter(int page, int size, String name, Long from, Long to, Byte status);
    ApiResponse<FlashSaleResponse> createFlashSale(FlashSaleRequest request);
    ApiResponse<FlashSaleResponse> updateFlashSale(FlashSaleRequest request, Integer id);
    ApiResponse<FlashSaleResponse> toggleStatus(Integer id);
    
    // Bổ sung methods hỗ trợ Cart (Simplified)
    /**
     * Tìm flash sale đang active cho một sách
     * Business rule: 1 sách chỉ có 1 flash sale active tại 1 thời điểm
     * @param bookId ID của sách
     * @return FlashSaleItem active hoặc empty nếu không có
     */
    Optional<FlashSaleItem> findActiveFlashSaleForBook(Long bookId);
    
    /**
     * Lấy thông tin flash sale đang active cho một sách
     * @param bookId ID của sách
     * @return FlashSaleInfoResponse hoặc null nếu không có
     */
    FlashSaleInfoResponse getActiveFlashSaleInfo(Long bookId);
    
    /**
     * Kiểm tra flash sale có còn hợp lệ không
     * @param flashSaleItemId ID của flash sale item
     * @return true nếu còn hợp lệ
     */
    boolean isFlashSaleValid(Long flashSaleItemId);
    
    /**
     * Kiểm tra flash sale có đủ stock không
     * @param flashSaleItemId ID của flash sale item
     * @param quantity Số lượng cần check
     * @return true nếu đủ stock
     */
    boolean hasEnoughStock(Long flashSaleItemId, Integer quantity);
    
    /**
     * Schedule flash sale expiration task khi tạo/update flash sale
     * Integration với FlashSaleExpirationScheduler
     * @param flashSaleId ID của flash sale
     * @param endTime Thời gian kết thúc (timestamp)
     */
    void scheduleFlashSaleExpiration(Integer flashSaleId, Long endTime);
    
    /**
     * Cancel scheduled expiration task (khi admin cancel/update flash sale)
     * @param flashSaleId ID của flash sale
     */
    void cancelFlashSaleExpirationSchedule(Integer flashSaleId);
    
    /**
     * ✅ NEW: Disable flash sale items instead of nullifying cart items
     * @param flashSaleId ID của flash sale
     * @return Số lượng items bị disable
     */
    int disableFlashSaleItems(Integer flashSaleId);
    
    /**
     * ✅ NEW: Enable flash sale items when flash sale is extended
     * @param flashSaleId ID của flash sale  
     * @return Số lượng items được enable
     */
    int enableFlashSaleItems(Integer flashSaleId);
    
    /**
     * ✅ AUTO-UPDATE: Cập nhật status của FlashSaleItems dựa trên thời gian
     * @return Số lượng items được update
     */
    int autoUpdateFlashSaleItemsStatus();
    
    /**
     * ✅ AUTO-UPDATE: Cập nhật status cho một flash sale cụ thể
     * @param flashSaleId ID của flash sale
     * @return Số lượng items được update
     */
    int autoUpdateFlashSaleItemsStatus(Integer flashSaleId);
}
