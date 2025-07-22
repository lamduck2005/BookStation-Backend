package org.datn.bookstation.entity.enums;

public enum OrderStatus {
    PENDING,                        // Chờ xử lý
    CONFIRMED,                      // Đã xác nhận
    SHIPPED,                        // Đang giao hàng
    DELIVERED,                      // Đã giao hàng thành công
    DELIVERY_FAILED,                // Giao hàng thất bại
    CANCELED,                       // Đã hủy
    REFUND_REQUESTED,               // Yêu cầu hoàn trả (chờ admin xem xét)
    REFUNDING,                      // Đang hoàn tiền (admin đã chấp nhận, voucher đã hoàn)
    REFUNDED,                       // Đã hoàn tiền hoàn tất (khách đã nhận tiền)
    GOODS_RETURNED_TO_WAREHOUSE,    // Hàng đã trả về kho (stock được cộng lại)
    PARTIALLY_REFUNDED              // Hoàn tiền một phần
}