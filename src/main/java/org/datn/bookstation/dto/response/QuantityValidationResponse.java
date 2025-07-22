package org.datn.bookstation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuantityValidationResponse {
    
    private Boolean valid;
    private String message;
    private Integer availableQuantity;
    
    public static QuantityValidationResponse success(Integer availableQuantity) {
        return new QuantityValidationResponse(true, "Số lượng hợp lệ", availableQuantity);
    }
    
    public static QuantityValidationResponse failure(String message, Integer availableQuantity) {
        return new QuantityValidationResponse(false, message, availableQuantity);
    }
}
