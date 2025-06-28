package org.datn.bookstation.dto.request;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Data
@Getter
@Setter
public class VoucherRepuest {
    private Integer id;
    private String code;
    private BigDecimal discountPercentage;
    private Long startTime;        // Đổi từ Instant sang Long
    private Long endTime;          // Đổi từ Instant sang Long
    private BigDecimal minOrderValue;
    private BigDecimal maxDiscountValue;
    private byte status;
    private Long createdAt;
    private Long updatedAt;
    private String createdBy;
    private String updatedBy;
}