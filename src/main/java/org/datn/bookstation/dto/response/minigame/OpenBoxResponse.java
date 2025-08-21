package org.datn.bookstation.dto.response.minigame;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.datn.bookstation.entity.enums.BoxOpenType;
import org.datn.bookstation.entity.enums.RewardType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpenBoxResponse {
    
    private boolean success;
    private String message;
    
    // Thông tin kết quả mở hộp
    private Integer historyId;
    private BoxOpenType openType;
    private Long openDate;
    private Integer pointsSpent;
    
    // Thông tin phần thưởng (nếu trúng)
    private boolean hasReward;
    private RewardType rewardType;
    private String rewardName;
    private String rewardDescription;
    private Integer rewardValue; // Điểm hoặc voucher value
    
    // Thông tin voucher (nếu trúng voucher)
    private Integer voucherId;
    private String voucherCode;
    private String voucherName;
    
    // Thông tin user sau khi mở
    private Integer userRemainingFreeOpens;
    private Integer userCurrentPoints;
    private Integer userTotalOpenedInCampaign;
    
    // Animation/UI data
    private String animationType; // "win", "lose", "big_win"
    private String rewardImage;
    
    // Constructor cho trường hợp không trúng
    public OpenBoxResponse(boolean success, String message, BoxOpenType openType, 
                          Integer userRemainingFreeOpens, Integer userCurrentPoints) {
        this.success = success;
        this.message = message;
        this.openType = openType;
        this.hasReward = false;
        this.userRemainingFreeOpens = userRemainingFreeOpens;
        this.userCurrentPoints = userCurrentPoints;
        this.animationType = "lose";
    }
}
