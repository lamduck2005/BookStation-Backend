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
    
    // ✅ THÊM MỚI: Nhà xuất bản
    private Integer publisherId;
    
    // ✅ THÊM MỚI: Ảnh bìa sách
    private String coverImageUrl;
    
    // ✅ THÊM MỚI: Người dịch
    private String translator;
    
    // ✅ THÊM MỚI: ISBN
    private String isbn;
    
    // ✅ THÊM MỚI: Số trang
    private Integer pageCount;
    
    // ✅ THÊM MỚI: Ngôn ngữ
    private String language;
    
    // ✅ THÊM MỚI: Cân nặng (gram)
    private Integer weight;
    
    // ✅ THÊM MỚI: Kích thước (dài x rộng x cao) cm
    private String dimensions;
}
