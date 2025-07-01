package org.datn.bookstation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventCategoryResponse {
    private Integer id;
    private String name;
    private String description;
    private Byte status;
    private Long createdAt;
    private Long updatedAt;
}
