package org.datn.bookstation.dto.request;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FlashSaleItemBookRequest {
     private Integer bookId;
    private String bookName;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private Long publicationDate;
    private String bookCode;
    private Byte status;
    private Integer categoryId;
    private String categoryName;
    private String coverImageUrl;
    private BigDecimal discountValue;
    private Integer discountPercent;
    private Boolean discountActive;
    private Integer flashSaleItemId;
    private  BigDecimal discountPrice;
    private BigDecimal discountPercentage;


}
