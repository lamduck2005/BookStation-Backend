package org.datn.bookstation.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class VoucherResponse {
    private Integer id;
    private String code;
    private BigDecimal discountPercentage;
    private Long startTime;
    private Long endTime;
    private BigDecimal minOrderValue;
    private BigDecimal maxDiscountValue;
    private Byte status;
    private Long createdAt;
    private Long updatedAt;
    private String createdBy;
    private String updatedBy;
}
