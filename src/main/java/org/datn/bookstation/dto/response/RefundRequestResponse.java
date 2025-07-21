package org.datn.bookstation.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class RefundRequestResponse {
    
    private Integer id;
    private Integer orderId;
    private String orderCode;
    private Integer userId;
    private String userFullName;
    private String refundType;
    private String refundTypeDisplay;
    private String status;
    private String statusDisplay;
    private String reason;
    private String customerNote;
    private String adminNote;
    private List<String> evidenceImages;
    private List<String> evidenceVideos;
    private BigDecimal totalRefundAmount;
    private Integer approvedById;
    private String approvedByName;
    private Long createdAt;
    private Long updatedAt;
    private Long approvedAt;
    private Long completedAt;
    
    // Chi tiết sản phẩm hoàn trả
    private List<RefundItemResponse> refundItems;
    
    @Data
    public static class RefundItemResponse {
        private Integer id;
        private Integer bookId;
        private String bookName;
        private String bookCode;
        private Integer refundQuantity;
        private BigDecimal unitPrice;
        private BigDecimal totalAmount;
        private String reason;
        private Long createdAt;
    }
}
