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
    
    private Integer flashSaleItemId; // Optional - for flash sale items
    
    @NotNull(message = "Số lượng không được để trống")
    @Positive(message = "Số lượng phải lớn hơn 0")
    private Integer quantity;
    
    @NotNull(message = "Đơn giá không được để trống")
    @Positive(message = "Đơn giá phải lớn hơn 0")
    private BigDecimal unitPrice;
}
