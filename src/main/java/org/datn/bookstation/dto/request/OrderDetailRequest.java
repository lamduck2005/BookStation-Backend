package org.datn.bookstation.dto.request;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

@Getter
@Setter
public class OrderDetailRequest {
    
    @NotNull(message = "Book ID không được để trống")
    private Integer bookId;
    
    // ⚠️ DEPRECATED: flashSaleItemId không cần thiết nữa - backend sẽ tự động phát hiện flash sale
    // Chỉ giữ lại để tương thích với frontend cũ
    private Integer flashSaleItemId;
    
    @NotNull(message = "Số lượng không được để trống")
    @Positive(message = "Số lượng phải lớn hơn 0")
    private Integer quantity;
    
    @NotNull(message = "Đơn giá không được để trống")
    @Positive(message = "Đơn giá phải lớn hơn 0")
    private BigDecimal unitPrice;
}
