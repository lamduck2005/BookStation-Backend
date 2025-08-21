package org.datn.bookstation.service.impl.minigame;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.datn.bookstation.dto.request.minigame.RewardRequest;
import org.datn.bookstation.dto.response.minigame.RewardResponse;
import org.datn.bookstation.entity.*;
import org.datn.bookstation.entity.enums.RewardType;
import org.datn.bookstation.repository.*;
import org.datn.bookstation.service.RewardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
@Transactional
public class RewardServiceImpl implements RewardService {

    private final RewardRepository rewardRepository;
    private final CampaignRepository campaignRepository;
    private final VoucherRepository voucherRepository;
    private final BoxHistoryRepository boxHistoryRepository;

    @Override
    public List<RewardResponse> getRewardsByCampaign(Integer campaignId) {
        List<Reward> rewards = rewardRepository.findByCampaignIdOrderByProbabilityDesc(campaignId);
        return rewards.stream()
                .map(this::toRewardResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void createReward(RewardRequest request) {
        Campaign campaign = campaignRepository.findById(request.getCampaignId())
                .orElseThrow(() -> new RuntimeException("Chiến dịch không tồn tại"));

        Reward reward = new Reward();
        reward.setCampaign(campaign);
        reward.setType(request.getType());
        reward.setName(request.getName());
        reward.setDescription(request.getDescription());
        
        // Validate and set specific reward data based on type
        if (request.getType() == RewardType.VOUCHER) {
            if (request.getVoucherId() == null) {
                throw new RuntimeException("Voucher ID là bắt buộc cho phần thưởng voucher");
            }
            Voucher voucher = voucherRepository.findById(request.getVoucherId())
                    .orElseThrow(() -> new RuntimeException("Voucher không tồn tại"));
            
            // ✅ Validate voucher quantity availability
            if (voucher.getUsageLimit() != null && request.getQuantityTotal() > voucher.getUsageLimit()) {
                throw new RuntimeException("Số lượng phần thưởng (" + request.getQuantityTotal() + 
                                         ") không thể vượt quá số lượng có sẵn của voucher (" + 
                                         voucher.getUsageLimit() + ")");
            }
            
            // ✅ Check remaining usage of voucher
            int remainingVoucherUsage = voucher.getUsageLimit() != null ? 
                                       voucher.getUsageLimit() - (voucher.getUsedCount() != null ? voucher.getUsedCount() : 0) : 
                                       Integer.MAX_VALUE;
            if (request.getQuantityTotal() > remainingVoucherUsage) {
                throw new RuntimeException("Số lượng phần thưởng (" + request.getQuantityTotal() + 
                                         ") vượt quá số lượt sử dụng còn lại của voucher (" + 
                                         remainingVoucherUsage + ")");
            }
            
            reward.setVoucher(voucher);
        } else if (request.getType() == RewardType.POINTS) {
            if (request.getPointValue() == null || request.getPointValue() <= 0) {
                throw new RuntimeException("Giá trị điểm phải lớn hơn 0");
            }
            reward.setPointValue(request.getPointValue());
        }
        
        reward.setQuantityTotal(request.getQuantityTotal());
        reward.setQuantityRemaining(request.getQuantityTotal());
        reward.setProbability(request.getProbability());
        reward.setStatus(request.getStatus());
        reward.setCreatedBy(request.getCreatedBy());
        
        rewardRepository.save(reward);
        log.info("Created new reward: {} for campaign: {}", reward.getName(), campaign.getName());
    }

    @Override
    public void updateReward(RewardRequest request) {
        Reward reward = rewardRepository.findById(request.getId())
                .orElseThrow(() -> new RuntimeException("Phần thưởng không tồn tại"));

        reward.setType(request.getType());
        reward.setName(request.getName());
        reward.setDescription(request.getDescription());
        
        // Update specific reward data based on type
        if (request.getType() == RewardType.VOUCHER) {
            if (request.getVoucherId() == null) {
                throw new RuntimeException("Voucher ID là bắt buộc cho phần thưởng voucher");
            }
            Voucher voucher = voucherRepository.findById(request.getVoucherId())
                    .orElseThrow(() -> new RuntimeException("Voucher không tồn tại"));
            
            // ✅ Validate voucher quantity availability for update
            if (voucher.getUsageLimit() != null && request.getQuantityTotal() > voucher.getUsageLimit()) {
                throw new RuntimeException("Số lượng phần thưởng (" + request.getQuantityTotal() + 
                                         ") không thể vượt quá số lượng có sẵn của voucher (" + 
                                         voucher.getUsageLimit() + ")");
            }
            
            // ✅ Check remaining usage of voucher
            int remainingVoucherUsage = voucher.getUsageLimit() != null ? 
                                       voucher.getUsageLimit() - (voucher.getUsedCount() != null ? voucher.getUsedCount() : 0) : 
                                       Integer.MAX_VALUE;
            if (request.getQuantityTotal() > remainingVoucherUsage) {
                throw new RuntimeException("Số lượng phần thưởng (" + request.getQuantityTotal() + 
                                         ") vượt quá số lượt sử dụng còn lại của voucher (" + 
                                         remainingVoucherUsage + ")");
            }
            
            reward.setVoucher(voucher);
            reward.setPointValue(null); // Clear point value
        } else if (request.getType() == RewardType.POINTS) {
            if (request.getPointValue() == null || request.getPointValue() <= 0) {
                throw new RuntimeException("Giá trị điểm phải lớn hơn 0");
            }
            reward.setPointValue(request.getPointValue());
            reward.setVoucher(null); // Clear voucher
        } else { // NONE
            reward.setVoucher(null);
            reward.setPointValue(null);
        }
        
        // Update quantity remaining proportionally if total quantity changed
        int currentDistributed = reward.getQuantityTotal() - reward.getQuantityRemaining();
        reward.setQuantityTotal(request.getQuantityTotal());
        reward.setQuantityRemaining(Math.max(0, request.getQuantityTotal() - currentDistributed));
        
        reward.setProbability(request.getProbability());
        reward.setStatus(request.getStatus());
        reward.setUpdatedBy(request.getUpdatedBy());
        
        rewardRepository.save(reward);
        log.info("Updated reward: {}", reward.getName());
    }

    @Override
    public void updateStatus(Integer id, Byte status, Integer updatedBy) {
        Reward reward = rewardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Phần thưởng không tồn tại"));

        reward.setStatus(status);
        reward.setUpdatedBy(updatedBy);
        rewardRepository.save(reward);

        log.info("Updated reward status: {} -> {}", reward.getName(), status);
    }

    @Override
    public void deleteReward(Integer id) {
        Reward reward = rewardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Phần thưởng không tồn tại"));

        // ✅ Check if reward is being used in box_history
        List<BoxHistory> boxHistories = boxHistoryRepository.findByRewardId(id);
        
        if (!boxHistories.isEmpty()) {
            // ✅ Set reward_id to null in box_history before deleting reward
            for (BoxHistory history : boxHistories) {
                history.setReward(null);
                boxHistoryRepository.save(history);
            }
            log.info("Updated {} box history records to remove reward reference", boxHistories.size());
        }

        rewardRepository.delete(reward);
        log.info("Deleted reward: {}", reward.getName());
    }

    @Override
    public RewardResponse getRewardById(Integer id) {
        Reward reward = rewardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Phần thưởng không tồn tại"));

        return toRewardResponse(reward);
    }

    private RewardResponse toRewardResponse(Reward reward) {
        RewardResponse response = new RewardResponse();
        
        response.setId(reward.getId());
        response.setCampaignId(reward.getCampaign().getId());
        response.setType(reward.getType());
        response.setName(reward.getName());
        response.setDescription(reward.getDescription());
        
        // Voucher info (if applicable)
        if (reward.getVoucher() != null) {
            response.setVoucherId(reward.getVoucher().getId());
            response.setVoucherCode(reward.getVoucher().getCode());
            response.setVoucherName(reward.getVoucher().getName());
            response.setVoucherDescription(reward.getVoucher().getDescription());
        }
        
        response.setPointValue(reward.getPointValue());
        response.setQuantityTotal(reward.getQuantityTotal());
        response.setQuantityRemaining(reward.getQuantityRemaining());
        response.setProbability(reward.getProbability());
        response.setStatus(reward.getStatus());
        response.setCreatedAt(reward.getCreatedAt());
        response.setUpdatedAt(reward.getUpdatedAt());
        response.setCreatedBy(reward.getCreatedBy());
        response.setUpdatedBy(reward.getUpdatedBy());
        
        // Statistics
        response.setDistributedCount(reward.getQuantityTotal() - reward.getQuantityRemaining());
        if (reward.getQuantityTotal() > 0) {
            response.setDistributedPercentage(
                java.math.BigDecimal.valueOf((reward.getQuantityTotal() - reward.getQuantityRemaining()) * 100.0 / reward.getQuantityTotal())
            );
        } else {
            response.setDistributedPercentage(java.math.BigDecimal.ZERO);
        }
        
        return response;
    }
}
