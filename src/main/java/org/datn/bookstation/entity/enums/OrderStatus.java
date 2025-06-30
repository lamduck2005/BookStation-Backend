package org.datn.bookstation.entity.enums;

public enum OrderStatus {
    PENDING,             // Chờ xử lý
    CONFIRMED,           // Đã xác nhận
    SHIPPED,             // Đang giao hàng
    DELIVERED,           // Đã giao hàng
    CANCELED,            // Đã hủy
    REFUNDING,           // Đang hoàn tiền
    REFUNDED,            // Đã hoàn tiền  
    RETURNED,            // Đã trả hàng
    PARTIALLY_REFUNDED   // Hoàn tiền một phần
}