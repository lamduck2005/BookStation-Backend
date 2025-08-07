package org.datn.bookstation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RevenueStatsResponse {
    private Integer year;
    private Integer month; // nullable nếu thống kê theo năm
    private Integer week; // nullable nếu không phải tuần
    private BigDecimal revenue;
}
