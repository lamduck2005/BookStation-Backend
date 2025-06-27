package org.datn.bookstation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventResponse {
    private Integer id;
    private String name;
    private String description;
    private Integer categoryId;
    private String categoryName;
    private Long startDate;
    private Long endDate;
    private Long registrationDeadline;
    private Integer maxParticipants;
    private Integer currentParticipants;
    private BigDecimal entryFee;
    private Byte status;
    private Long createdAt;
    private Long updatedAt;
}
