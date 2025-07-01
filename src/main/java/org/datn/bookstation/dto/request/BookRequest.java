package org.datn.bookstation.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotEmpty;
import java.math.BigDecimal;
import java.util.List;

@Data
public class BookRequest {
    private String bookName;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private Long publicationDate;
    private Integer categoryId;
    private Integer supplierId;
    private String bookCode;
    private Byte status;
    
    // ✅ THÊM MỚI: Danh sách ID tác giả
    @NotEmpty(message = "Sách phải có ít nhất một tác giả")
    private List<Integer> authorIds;
}
