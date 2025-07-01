package org.datn.bookstation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookResponse {
    private Integer id;
    private String bookName;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private Long publicationDate;
    private String categoryName;
    private Integer categoryId;
    private String supplierName;
    private Integer supplierId;
    private String bookCode;
    private Byte status;
    private Long createdAt;
    private Long updatedAt;
    
    // ✅ THÊM MỚI: Danh sách tác giả
    private List<AuthorResponse> authors;
}
