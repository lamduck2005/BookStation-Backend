package org.datn.bookstation.dto.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PointRequest {
    private String email;
    private Long order; // nullable, có thể null hoặc id đơn hàng
    private Integer pointEarned;
    private BigDecimal minSpent;
    private Integer pointSpent;
    private String description;
    private Byte status;
}
