package org.datn.bookstation.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.datn.bookstation.entity.enums.OrderStatus;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
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
    
    @NotNull(message = "Phí vận chuyển không được để trống")
    private BigDecimal shippingFee = BigDecimal.ZERO;
    
    // Note: subtotal sẽ được tính tự động từ orderDetails
    // discountAmount và discountShipping sẽ được tính từ vouchers
    // totalAmount sẽ được tính: subtotal + shippingFee - discountAmount - discountShipping
    
    private OrderStatus orderStatus = OrderStatus.PENDING;
    
    @NotNull(message = "Loại đơn hàng không được để trống")
    private String orderType;
    
    @NotEmpty(message = "Chi tiết đơn hàng không được để trống")
    private List<OrderDetailRequest> orderDetails;
    
    private List<Integer> voucherIds; // Optional vouchers applied
    
    private String notes; // Optional order notes

    // ✅ BACKEND TỰ TÍNH TOÁN - Frontend không cần truyền
    // subtotal và totalAmount sẽ được backend tự tính dựa trên orderDetails và vouchers
    private BigDecimal subtotal; // Optional - backend sẽ tự tính từ orderDetails
    private BigDecimal totalAmount; // Optional - backend sẽ tự tính: subtotal + shipping - discounts
}
