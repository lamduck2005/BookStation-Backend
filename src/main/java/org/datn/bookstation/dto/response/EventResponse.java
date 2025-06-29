package org.datn.bookstation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventResponse {
    private Integer id;
    private String name;
    private String description;
    private Integer categoryId;
    private String categoryName;
    private String eventType;
    private String eventTypeName;
    private String imageUrl; // First image for backward compatibility
    private List<String> imageUrls; // Array of all images
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
