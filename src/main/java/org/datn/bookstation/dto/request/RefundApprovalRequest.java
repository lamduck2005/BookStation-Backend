package org.datn.bookstation.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefundApprovalRequest {
    
    @NotBlank(message = "Trạng thái phê duyệt không được để trống")
    private String status; // "APPROVED" hoặc "REJECTED"
    
    private String adminNote; // Ghi chú từ admin
}
