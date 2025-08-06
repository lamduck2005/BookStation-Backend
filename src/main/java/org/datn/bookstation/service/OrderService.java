package org.datn.bookstation.service;

import org.datn.bookstation.dto.request.OrderRequest;
import org.datn.bookstation.dto.request.OrderDetailRefundRequest;
import org.datn.bookstation.dto.request.RefundRequestDto;
import org.datn.bookstation.dto.request.AdminRefundDecisionDto;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.OrderResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.entity.Order;
import org.datn.bookstation.entity.enums.OrderStatus;

import java.util.List;
import java.util.Optional;

public interface OrderService {
    Optional<Integer> findIdByCode(String code);
    
    PaginationResponse<OrderResponse> getAllWithPagination(int page, int size, String code, 
            Integer userId, OrderStatus orderStatus, String orderType, Long startDate, Long endDate);
    
    List<Order> getAll();
    
    Order getById(Integer id);
    
    OrderResponse getByIdWithDetails(Integer id);
    
    ApiResponse<OrderResponse> create(OrderRequest request);
    
    ApiResponse<OrderResponse> update(OrderRequest request, Integer id);
    
    ApiResponse<OrderResponse> updateStatus(Integer id, OrderStatus newStatus, Integer staffId);
    
    void delete(Integer id);
    
    List<OrderResponse> getOrdersByUser(Integer userId);
    
    // ✅ THÊM MỚI: API lấy đơn hàng của user có phân trang
    PaginationResponse<OrderResponse> getOrdersByUserWithPagination(Integer userId, int page, int size);
    
    List<OrderResponse> getOrdersByStatus(OrderStatus status);
    
    // ✅ THÊM MỚI: API lấy danh sách đơn hàng đang xử lý theo book ID
    List<OrderResponse> getProcessingOrdersByBookId(Integer bookId);
    
    ApiResponse<OrderResponse> cancelOrder(Integer id, String reason, Integer userId);
    
    /**
     * ✅ THÊM MỚI: Hoàn trả đơn hàng một phần
     * @param orderId ID đơn hàng
     * @param userId ID người thực hiện
     * @param reason Lý do hoàn trả
     * @param refundDetails Danh sách sản phẩm và số lượng hoàn trả
     * @return Đơn hàng đã được cập nhật
     */
    ApiResponse<OrderResponse> partialRefund(Integer orderId, Integer userId, String reason, List<OrderDetailRefundRequest> refundDetails);
    
    /**
     * ✅ THÊM MỚI: Hoàn trả đơn hàng toàn bộ
     * @param orderId ID đơn hàng  
     * @param userId ID người thực hiện
     * @param reason Lý do hoàn trả
     * @return Đơn hàng đã được cập nhật
     */
    ApiResponse<OrderResponse> fullRefund(Integer orderId, Integer userId, String reason);
    
    /**
     * ✅ THÊM MỚI: Khách hàng gửi yêu cầu hoàn trả (chuyển sang REFUND_REQUESTED)
     * @param orderId ID đơn hàng
     * @param refundRequest Thông tin yêu cầu hoàn trả
     * @return Đơn hàng đã được cập nhật
     */
    ApiResponse<OrderResponse> requestRefund(Integer orderId, RefundRequestDto refundRequest);
    
    /**
     * ✅ THÊM MỚI: Admin chấp nhận yêu cầu hoàn trả
     * @param decision Quyết định của admin
     * @return Đơn hàng đã được cập nhật
     */
    ApiResponse<OrderResponse> approveRefundRequest(AdminRefundDecisionDto decision);
    
    /**
     * ✅ THÊM MỚI: Admin từ chối yêu cầu hoàn trả
     * @param decision Quyết định của admin
     * @return Đơn hàng đã được cập nhật
     */
    ApiResponse<OrderResponse> rejectRefundRequest(AdminRefundDecisionDto decision);

    /**
     * Lấy chi tiết đơn hàng theo id (phục vụ hoàn hàng)
     */
    OrderResponse getOrderDetailById(Integer id);
}
