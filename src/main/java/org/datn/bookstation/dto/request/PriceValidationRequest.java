package org.datn.bookstation.dto.request;

import lombok.Data;

@Data
public class PriceValidationRequest {
    private Integer bookId;
    private java.math.BigDecimal frontendPrice;
}
