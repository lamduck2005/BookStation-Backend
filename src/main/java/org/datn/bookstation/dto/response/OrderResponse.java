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
    private BigDecimal totalAmount;
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
}
