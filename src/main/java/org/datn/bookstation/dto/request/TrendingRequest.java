package org.datn.bookstation.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

/**
 * 🔥 TRENDING REQUEST DTO
 * Hỗ trợ 2 loại trending: DAILY_TRENDING và HOT_DISCOUNT
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrendingRequest {
    
    /**
     * Loại trending:
     * - "DAILY_TRENDING": Xu hướng theo ngày (dựa trên sales + reviews)
     * - "HOT_DISCOUNT": Sách hot giảm sốc (flash sale + discount cao)
     */
    @NotBlank(message = "Type không được để trống")
    private String type; // DAILY_TRENDING hoặc HOT_DISCOUNT
    
    /**
     * Pagination
     */
    @Min(value = 0, message = "Page phải >= 0")
    private int page = 0;
    
    @Min(value = 1, message = "Size phải >= 1")
    @Max(value = 50, message = "Size không được vượt quá 50")
    private int size = 10;
    
    /**
     * Filters
     */
    private Integer categoryId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    
    /**
     * 🔥 HOT_DISCOUNT specific filters
     */
    private Integer minDiscountPercentage; // Giảm giá tối thiểu (%)
    private Boolean flashSaleOnly = false; // Chỉ lấy flash sale
    
    /**
     * Validation methods
     */
    public boolean isDailyTrending() {
        return "DAILY_TRENDING".equals(type);
    }
    
    public boolean isHotDiscount() {
        return "HOT_DISCOUNT".equals(type);
    }
    
    public boolean isValidType() {
        return isDailyTrending() || isHotDiscount();
    }
}
