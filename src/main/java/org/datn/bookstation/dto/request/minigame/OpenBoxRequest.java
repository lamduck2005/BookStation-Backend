package org.datn.bookstation.dto.request.minigame;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.datn.bookstation.entity.enums.BoxOpenType;

@Data
public class OpenBoxRequest {
    
    @NotNull(message = "Campaign ID không được để trống")
    private Integer campaignId;
    
    @NotNull(message = "User ID không được để trống")
    private Integer userId;
    
    @NotNull(message = "Loại mở hộp không được để trống")
    private BoxOpenType openType; // FREE hoặc POINT
}
