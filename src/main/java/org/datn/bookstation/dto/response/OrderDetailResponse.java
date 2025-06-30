package org.datn.bookstation.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class OrderDetailResponse {
    private Integer orderId;
    private Integer bookId;
    private String bookName;
    private String bookCode;
    private String bookImageUrl;
    private Integer flashSaleItemId;
    private BigDecimal flashSalePrice;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice; // quantity * unitPrice
    private Long createdAt;
    private Long updatedAt;
    private Integer createdBy;
    private Integer updatedBy;
    private Byte status;
}
