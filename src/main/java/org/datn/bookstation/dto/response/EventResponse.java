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
    private String eventTypeName; // Loại hình hiển thị (VD: "Sự kiện ra mắt sách mới")
    private String imageUrl; // First image for backward compatibility
    private List<String> imageUrls; // Array of all images
    private Long startDate;
    private Long endDate;
    private Integer maxParticipants;
    private Integer currentParticipants;
    private BigDecimal entryFee;
    private String status; // Trạng thái gốc: "DRAFT", "PUBLISHED", "ONGOING", "COMPLETED", "CANCELLED"
    private String statusName; // Trạng thái hiển thị (VD: "Đang diễn ra")
    private String location; // Địa điểm
    private Boolean isOnline; // Có phải sự kiện online không
    private String rules; // Quy định
    private Long createdAt;
    private Long updatedAt;
}
