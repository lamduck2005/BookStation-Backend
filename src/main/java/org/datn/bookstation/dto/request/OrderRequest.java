package org.datn.bookstation.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.datn.bookstation.entity.enums.OrderStatus;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class OrderRequest {
    
    @NotNull(message = "User ID không được để trống")
    private Integer userId;
    
    private Integer staffId; // Optional - for staff processing
    
    @NotNull(message = "Address ID không được để trống")
    private Integer addressId;
    
    @NotNull(message = "Tổng tiền không được để trống")
    @Positive(message = "Tổng tiền phải lớn hơn 0")
    private BigDecimal totalAmount;
    
    private OrderStatus orderStatus = OrderStatus.PENDING;
    
    @NotNull(message = "Loại đơn hàng không được để trống")
    private String orderType;
    
    @NotEmpty(message = "Chi tiết đơn hàng không được để trống")
    private List<OrderDetailRequest> orderDetails;
    
    private List<Integer> voucherIds; // Optional vouchers applied
    
    private String notes; // Optional order notes
}
