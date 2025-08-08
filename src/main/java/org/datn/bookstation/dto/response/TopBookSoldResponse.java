package org.datn.bookstation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TopBookSoldResponse {
    private String bookName;
    private Long soldQuantity;
}