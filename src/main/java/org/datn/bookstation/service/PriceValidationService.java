package org.datn.bookstation.service;

import org.datn.bookstation.dto.request.OrderDetailRequest;
import org.datn.bookstation.dto.response.ApiResponse;

import java.util.List;

/**
 * Service validation giá sản phẩm khi đặt hàng
 */
public interface PriceValidationService {
    
    /**
     * Validate giá sản phẩm từ frontend với giá backend hiện tại
     * @param orderDetails Danh sách chi tiết đơn hàng từ frontend
     * @return ApiResponse chứa thông tin validation
     */
    ApiResponse<String> validateProductPrices(List<OrderDetailRequest> orderDetails);
    
    /**
     * Validate một sản phẩm cụ thể
     * @param orderDetail Chi tiết đơn hàng
     * @return Thông báo lỗi (null nếu hợp lệ)
     */
    String validateSingleProductPrice(OrderDetailRequest orderDetail);
}
