package org.datn.bookstation.dto.request;


import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class VoucherRepuest {
    private Integer id;
    private String code;
    private BigDecimal discountPercentage;
    private Instant startTime;
    private Instant endTime;
    private BigDecimal minOrderValue;
    private BigDecimal maxDiscountValue;
    private byte status;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

}
