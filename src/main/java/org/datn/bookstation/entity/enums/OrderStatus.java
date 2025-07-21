package org.datn.bookstation.entity.enums;

public enum OrderStatus {
    PENDING,             // Chờ xử lý
    CONFIRMED,           // Đã xác nhận
    SHIPPED,             // Đang giao hàng
    DELIVERED,           // Đã giao hàng thành công
    DELIVERY_FAILED,     // Giao hàng thất bại
    CANCELED,            // Đã hủy
    REFUND_REQUESTED,    // Yêu cầu hoàn trả (chờ admin xem xét)
    REFUNDING,           // Đang hoàn tiền (admin đã chấp nhận)
    REFUNDED,            // Đã hoàn tiền hoàn tất
    RETURNED,            // Đã trả hàng về kho
    PARTIALLY_REFUNDED   // Hoàn tiền một phần
}