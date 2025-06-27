package org.datn.bookstation.dto.request;

import lombok.Data;

@Data
public class EventCategoryRequest {
    private String categoryName;
    private String description;
    private String iconUrl;
    private Boolean isActive;
}
