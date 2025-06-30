package org.datn.bookstation.mapper;

import org.datn.bookstation.dto.response.OrderResponse;
import org.datn.bookstation.dto.response.OrderDetailResponse;
import org.datn.bookstation.dto.response.VoucherResponse;
import org.datn.bookstation.entity.Order;
import org.datn.bookstation.entity.OrderDetail;
import org.datn.bookstation.entity.OrderVoucher;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderResponseMapper {
    
    public OrderResponse toResponse(Order order) {
        if (order == null) return null;
        
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setCode(order.getCode());
        response.setUserId(order.getUser() != null ? order.getUser().getId() : null);
        response.setUserEmail(order.getUser() != null ? order.getUser().getEmail() : null);
        response.setUserName(order.getUser() != null ? order.getUser().getFullName() : null);
        response.setStaffId(order.getStaff() != null ? order.getStaff().getId() : null);
        response.setStaffName(order.getStaff() != null ? order.getStaff().getFullName() : null);
        response.setAddressId(order.getAddress() != null ? order.getAddress().getId() : null);
        response.setAddressDetail(order.getAddress() != null ? order.getAddress().getAddressDetail() : null);
        response.setRecipientName(order.getAddress() != null ? order.getAddress().getRecipientName() : null);
        response.setPhoneNumber(order.getAddress() != null ? order.getAddress().getPhoneNumber() : null);
        response.setOrderDate(order.getOrderDate());
        response.setTotalAmount(order.getTotalAmount());
        response.setStatus(order.getStatus());
        response.setOrderStatus(order.getOrderStatus());
        response.setOrderStatusDisplay(getOrderStatusDisplayName(order.getOrderStatus()));
        response.setOrderType(order.getOrderType());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());
        response.setCreatedBy(order.getCreatedBy());
        response.setUpdatedBy(order.getUpdatedBy());
        
        return response;
    }
    
    public OrderResponse toResponseWithDetails(Order order, List<OrderDetail> orderDetails, List<OrderVoucher> orderVouchers) {
        OrderResponse response = toResponse(order);
        if (response == null) return null;
        
        // Map order details
        if (orderDetails != null) {
            List<OrderDetailResponse> detailResponses = orderDetails.stream()
                .map(this::toOrderDetailResponse)
                .collect(Collectors.toList());
            response.setOrderDetails(detailResponses);
        }
        
        // Map vouchers
        if (orderVouchers != null) {
            List<VoucherResponse> voucherResponses = orderVouchers.stream()
                .map(this::toVoucherResponse)
                .collect(Collectors.toList());
            response.setVouchers(voucherResponses);
        }
        
        return response;
    }
    
    private OrderDetailResponse toOrderDetailResponse(OrderDetail detail) {
        if (detail == null) return null;
        
        OrderDetailResponse response = new OrderDetailResponse();
        response.setOrderId(detail.getOrder() != null ? detail.getOrder().getId() : null);
        response.setBookId(detail.getBook() != null ? detail.getBook().getId() : null);
        response.setBookName(detail.getBook() != null ? detail.getBook().getBookName() : null);
        response.setBookCode(detail.getBook() != null ? detail.getBook().getBookCode() : null);
        response.setFlashSaleItemId(detail.getFlashSaleItem() != null ? detail.getFlashSaleItem().getId() : null);
        response.setFlashSalePrice(detail.getFlashSaleItem() != null ? detail.getFlashSaleItem().getDiscountPrice() : null);
        response.setQuantity(detail.getQuantity());
        response.setUnitPrice(detail.getUnitPrice());
        response.setTotalPrice(detail.getUnitPrice().multiply(java.math.BigDecimal.valueOf(detail.getQuantity())));
        response.setCreatedAt(detail.getCreatedAt());
        response.setUpdatedAt(detail.getUpdatedAt());
        response.setCreatedBy(detail.getCreatedBy());
        response.setUpdatedBy(detail.getUpdatedBy());
        response.setStatus(detail.getStatus());
        
        return response;
    }
    
    private VoucherResponse toVoucherResponse(OrderVoucher orderVoucher) {
        if (orderVoucher == null || orderVoucher.getVoucher() == null) return null;
        
        VoucherResponse response = new VoucherResponse();
        response.setId(orderVoucher.getVoucher().getId());
        response.setCode(orderVoucher.getVoucher().getCode());
        response.setDiscountPercentage(orderVoucher.getVoucher().getDiscountPercentage());
        response.setStartTime(orderVoucher.getVoucher().getStartTime());
        response.setEndTime(orderVoucher.getVoucher().getEndTime());
        response.setMinOrderValue(orderVoucher.getVoucher().getMinOrderValue());
        response.setMaxDiscountValue(orderVoucher.getVoucher().getMaxDiscountValue());
        response.setStatus(orderVoucher.getVoucher().getStatus());
        response.setCreatedAt(orderVoucher.getVoucher().getCreatedAt());
        response.setUpdatedAt(orderVoucher.getVoucher().getUpdatedAt());
        response.setCreatedBy(orderVoucher.getVoucher().getCreatedBy());
        response.setUpdatedBy(orderVoucher.getVoucher().getUpdatedBy());
        
        return response;
    }
    
    private String getOrderStatusDisplayName(org.datn.bookstation.entity.enums.OrderStatus orderStatus) {
        if (orderStatus == null) return null;
        switch (orderStatus) {
            case PENDING: return "Chờ xử lý";
            case CONFIRMED: return "Đã xác nhận";
            case SHIPPED: return "Đang giao hàng";
            case DELIVERED: return "Đã giao hàng";
            case CANCELED: return "Đã hủy";
            default: return orderStatus.name();
        }
    }
}
