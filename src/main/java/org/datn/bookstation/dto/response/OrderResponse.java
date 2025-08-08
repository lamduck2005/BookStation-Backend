package org.datn.bookstation.dto.response;

import lombok.Getter;
import lombok.Setter;
import org.datn.bookstation.entity.enums.OrderStatus;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class OrderResponse {
    private Integer id;
    private String code;
    private Integer userId;
    private String userEmail;
    private String userName;
    private Integer staffId;
    private String staffName;
    private Integer addressId;
    private String addressDetail;
    private String recipientName;
    private String phoneNumber;
    private Long orderDate;
    private BigDecimal subtotal;
    private BigDecimal shippingFee;
    private BigDecimal discountAmount;
    private BigDecimal discountShipping;
    private BigDecimal voucherDiscountAmount; // ✅ THÊM: Tổng discount voucher áp dụng
    private BigDecimal totalAmount;
    private Integer regularVoucherCount;
    private Integer shippingVoucherCount;
    private Byte status;
    private OrderStatus orderStatus;
    private String orderStatusDisplay;
    private String orderType;
    private Long createdAt;
    private Long updatedAt;
    private Integer createdBy;
    private Integer updatedBy;
    private List<OrderDetailResponse> orderDetails;
    private List<VoucherResponse> vouchers;
    private String notes;
    private String cancelReason;
    
    // ✅ THÊM: Thông tin trạng thái có thể chuyển
    private List<StatusTransitionOption> availableTransitions;
    
    @Getter
    @Setter
    public static class StatusTransitionOption {
        private OrderStatus targetStatus;
        private String displayName;
        private String actionDescription;
        private Boolean requiresConfirmation;
        private String businessImpactNote;
    }
}
