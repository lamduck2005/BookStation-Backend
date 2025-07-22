package org.datn.bookstation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DropdownOptionResponse {
    private Integer id;
    private String name;
    private BigDecimal normalPrice;
    private BigDecimal flashSalePrice;
    private Boolean isFlashSale;

    // Constructor cũ để backward compatibility
    public DropdownOptionResponse(Integer id, String name) {
        this.id = id;
        this.name = name;
        this.isFlashSale = false;
    }
}
