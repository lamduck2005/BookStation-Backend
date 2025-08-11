package org.datn.bookstation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessingOrderResponse {
    
    // Thông tin cơ bản của đơn hàng
    private Integer orderId;
    private String orderCode;
    private String customerName;
    private String customerPhone;
    
    // Trạng thái đơn hàng
    private String orderStatus;
    private String orderStatusDisplay; // Tiếng Việt
    
    // Thông tin sản phẩm đang được xử lý
    private Integer bookId;
    private String bookName;
    private String bookCode;
    private Integer processingQuantity; // Số lượng đang được xử lý (chưa hoàn thành)
    private BigDecimal unitPrice;
    private BigDecimal totalAmount; // processingQuantity * unitPrice
    
    // Thông tin thời gian
    private Long orderCreatedAt;
    private String orderCreatedAtDisplay; // Format cho frontend
    
    // Thông tin hoàn hàng (nếu có)
    private String refundReason; // Lý do hoàn hàng (nếu đang trong quá trình hoàn)
    private String refundReasonDisplay;
    private String refundStatus;
    private String refundStatusDisplay;
    
    // Metadata hữu ích
    private String orderType; // ONLINE, COUNTER
    private String paymentMethod;
    private BigDecimal orderTotalAmount; // Tổng giá trị đơn hàng
}
