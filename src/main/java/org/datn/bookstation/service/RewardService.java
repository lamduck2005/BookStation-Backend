package org.datn.bookstation.service;

import org.datn.bookstation.dto.request.minigame.RewardRequest;
import org.datn.bookstation.dto.response.minigame.RewardResponse;

import java.util.List;

public interface RewardService {
    
    /**
     * Lấy danh sách phần thưởng của chiến dịch
     */
    List<RewardResponse> getRewardsByCampaign(Integer campaignId);
    
    /**
     * Tạo phần thưởng mới
     */
    void createReward(RewardRequest request);
    
    /**
     * Cập nhật phần thưởng
     */
    void updateReward(RewardRequest request);
    
    /**
     * Cập nhật trạng thái phần thưởng
     */
    void updateStatus(Integer id, Byte status, Integer updatedBy);
    
    /**
     * Xóa phần thưởng
     */
    void deleteReward(Integer id);
    
    /**
     * Lấy phần thưởng theo ID
     */
    RewardResponse getRewardById(Integer id);
}
