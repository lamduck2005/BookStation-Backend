package org.datn.bookstation.service.impl;

import lombok.AllArgsConstructor;
import org.datn.bookstation.entity.Order;
import org.datn.bookstation.entity.UserVoucher;
import org.datn.bookstation.entity.Voucher;
import org.datn.bookstation.entity.enums.VoucherCategory;
import org.datn.bookstation.entity.enums.DiscountType;
import org.datn.bookstation.repository.UserVoucherRepository;
import org.datn.bookstation.repository.VoucherRepository;
import org.datn.bookstation.service.VoucherCalculationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional
public class VoucherCalculationServiceImpl implements VoucherCalculationService {

    private final VoucherRepository voucherRepository;
    private final UserVoucherRepository userVoucherRepository;

    @Override
    public VoucherCalculationResult calculateVoucherDiscount(Order order, List<Integer> voucherIds, Integer userId) {
        if (voucherIds == null || voucherIds.isEmpty()) {
            return new VoucherCalculationResult();
        }

        // Validate maximum 2 vouchers
        if (voucherIds.size() > 2) {
            throw new RuntimeException("Chỉ được áp dụng tối đa 2 voucher trên 1 đơn hàng");
        }

        // Get vouchers
        List<Voucher> vouchers = voucherRepository.findAllById(voucherIds);
        if (vouchers.size() != voucherIds.size()) {
            throw new RuntimeException("Một số voucher không tồn tại");
        }

        // Validate voucher application
        validateVoucherApplication(order, vouchers, userId);

        VoucherCalculationResult result = new VoucherCalculationResult();
        List<VoucherApplicationDetail> appliedVouchers = new ArrayList<>();

        int regularCount = 0;
        int shippingCount = 0;

        for (Voucher voucher : vouchers) {
            if (voucher.getVoucherCategory() == VoucherCategory.SHIPPING) {
                shippingCount++;
                BigDecimal shippingDiscount = calculateSingleVoucherDiscount(voucher, order.getSubtotal(), order.getShippingFee());
                result.setTotalShippingDiscount(result.getTotalShippingDiscount().add(shippingDiscount));
                appliedVouchers.add(new VoucherApplicationDetail(voucher.getId(), voucher.getVoucherCategory(), voucher.getDiscountType(), shippingDiscount));
            } else {
                regularCount++;
                BigDecimal productDiscount = calculateSingleVoucherDiscount(voucher, order.getSubtotal(), order.getShippingFee());
                result.setTotalProductDiscount(result.getTotalProductDiscount().add(productDiscount));
                appliedVouchers.add(new VoucherApplicationDetail(voucher.getId(), voucher.getVoucherCategory(), voucher.getDiscountType(), productDiscount));
            }
        }

        result.setRegularVoucherCount(regularCount);
        result.setShippingVoucherCount(shippingCount);
        result.setAppliedVouchers(appliedVouchers);

        return result;
    }

    @Override
    public void validateVoucherApplication(Order order, List<Voucher> vouchers, Integer userId) {
        long currentTime = System.currentTimeMillis();
        
        int regularVoucherCount = 0;
        int shippingVoucherCount = 0;

        for (Voucher voucher : vouchers) {
            // Check voucher validity
            if (voucher.getStatus() != 1) {
                throw new RuntimeException("Voucher " + voucher.getCode() + " đã bị vô hiệu hóa");
            }

            // Check time validity
            if (currentTime < voucher.getStartTime() || currentTime > voucher.getEndTime()) {
                throw new RuntimeException("Voucher " + voucher.getCode() + " đã hết hạn hoặc chưa có hiệu lực");
            }

            // Check usage limit
            if (voucher.getUsageLimit() != null && voucher.getUsedCount() >= voucher.getUsageLimit()) {
                throw new RuntimeException("Voucher " + voucher.getCode() + " đã hết lượt sử dụng");
            }

            // Check minimum order value
            if (voucher.getMinOrderValue() != null && order.getSubtotal().compareTo(voucher.getMinOrderValue()) < 0) {
                throw new RuntimeException("Đơn hàng chưa đạt giá trị tối thiểu để sử dụng voucher " + voucher.getCode());
            }

            // Check user usage limit
            if (!canUserUseVoucher(userId, voucher.getId())) {
                throw new RuntimeException("Bạn đã sử dụng hết lượt cho voucher " + voucher.getCode());
            }

            // Count voucher types by category
            if (voucher.getVoucherCategory() == VoucherCategory.SHIPPING) {
                shippingVoucherCount++;
            } else {
                regularVoucherCount++;
            }
        }

        // ✅ NEW: Check if shipping voucher is used for counter sales
        if (shippingVoucherCount > 0 && "COUNTER".equals(order.getOrderType())) {
            throw new RuntimeException("Không thể áp dụng voucher giảm phí ship cho đơn hàng tại quầy vì không có phí vận chuyển");
        }

        // Validate voucher type limits
        if (regularVoucherCount > 1) {
            throw new RuntimeException("Chỉ được sử dụng tối đa 1 voucher thường trên 1 đơn hàng");
        }
        if (shippingVoucherCount > 1) {
            throw new RuntimeException("Chỉ được sử dụng tối đa 1 voucher freeship trên 1 đơn hàng");
        }
    }    @Override
    public BigDecimal calculateSingleVoucherDiscount(Voucher voucher, BigDecimal orderSubtotal, BigDecimal shippingFee) {
        BigDecimal discount = BigDecimal.ZERO;
        
        // ✅ NEW LOGIC: Use VoucherCategory to determine what to discount
        if (voucher.getVoucherCategory() == VoucherCategory.SHIPPING) {
            // Shipping voucher always discounts shipping fee
            discount = shippingFee;
            if (voucher.getMaxDiscountValue() != null && discount.compareTo(voucher.getMaxDiscountValue()) > 0) {
                discount = voucher.getMaxDiscountValue();
            }
        } else {
            // Normal voucher discounts product based on discount type
            switch (voucher.getDiscountType()) {
                case PERCENTAGE:
                    discount = orderSubtotal.multiply(voucher.getDiscountPercentage())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                    break;
                    
                case FIXED_AMOUNT:
                    discount = voucher.getDiscountAmount();
                    // Don't exceed order subtotal
                    if (discount.compareTo(orderSubtotal) > 0) {
                        discount = orderSubtotal;
                    }
                    break;
            }
        }
        
        // Apply max discount limit for normal vouchers
        if (voucher.getVoucherCategory() != VoucherCategory.SHIPPING && 
            voucher.getMaxDiscountValue() != null && 
            discount.compareTo(voucher.getMaxDiscountValue()) > 0) {
            discount = voucher.getMaxDiscountValue();
        }

        return discount;
    }

    @Override
    public boolean canUserUseVoucher(Integer userId, Integer voucherId) {
        Voucher voucher = voucherRepository.findById(voucherId).orElse(null);
        if (voucher == null) return false;

        UserVoucher userVoucher = userVoucherRepository.findByUserIdAndVoucherId(userId, voucherId).orElse(null);
        
        if (userVoucher == null) {
            // First time using this voucher
            return true;
        }

        // Check if user has reached usage limit for this voucher
        return userVoucher.getUsedCount() < voucher.getUsageLimitPerUser();
    }

    @Override
    public void updateVoucherUsage(List<Integer> voucherIds, Integer userId) {
        for (Integer voucherId : voucherIds) {
            // Update voucher used count
            Voucher voucher = voucherRepository.findById(voucherId).orElse(null);
            if (voucher != null) {
                voucher.setUsedCount(voucher.getUsedCount() + 1);
                voucherRepository.save(voucher);
            }

            // Update user voucher usage
            UserVoucher userVoucher = userVoucherRepository.findByUserIdAndVoucherId(userId, voucherId)
                .orElse(new UserVoucher());
            
            if (userVoucher.getId() == null) {
                // Create new user voucher record
                userVoucher.setUser(new org.datn.bookstation.entity.User() {{ setId(userId); }});
                userVoucher.setVoucher(voucher);
                userVoucher.setUsedCount(1);
            } else {
                // Update existing record
                userVoucher.setUsedCount(userVoucher.getUsedCount() + 1);
            }
            
            userVoucherRepository.save(userVoucher);
        }
    }
}
