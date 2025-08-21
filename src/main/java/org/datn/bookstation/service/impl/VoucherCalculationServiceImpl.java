package org.datn.bookstation.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
        log.debug("🎫 Found {} vouchers in database", vouchers.size());
        
        if (vouchers.size() != voucherIds.size()) {
            throw new RuntimeException("Một số voucher không tồn tại");
        }

        // Log each voucher details
        for (Voucher voucher : vouchers) {
            log.debug("🎫 Voucher {}: category={}, discountType={}, discountAmount={}, discountPercentage={}", 
                voucher.getCode(), voucher.getVoucherCategory(), voucher.getDiscountType(), 
                voucher.getDiscountAmount(), voucher.getDiscountPercentage());
        }

        // Validate voucher application
        validateVoucherApplication(order, vouchers, userId);

        VoucherCalculationResult result = new VoucherCalculationResult();
        List<VoucherApplicationDetail> appliedVouchers = new ArrayList<>();

        int regularCount = 0;
        int shippingCount = 0;

        for (Voucher voucher : vouchers) {
            log.debug("🎫 Processing voucher {}: category={}", voucher.getCode(), voucher.getVoucherCategory());
            
            if (voucher.getVoucherCategory() == VoucherCategory.SHIPPING) {
                shippingCount++;
                BigDecimal shippingDiscount = calculateSingleVoucherDiscount(voucher, order.getSubtotal(), order.getShippingFee());
                log.debug("🎫 Shipping voucher {} discount: {}", voucher.getCode(), shippingDiscount);
                result.setTotalShippingDiscount(result.getTotalShippingDiscount().add(shippingDiscount));
                appliedVouchers.add(new VoucherApplicationDetail(voucher.getId(), voucher.getVoucherCategory(), voucher.getDiscountType(), shippingDiscount));
            } else {
                regularCount++;
                BigDecimal productDiscount = calculateSingleVoucherDiscount(voucher, order.getSubtotal(), order.getShippingFee());
                log.debug("🎫 Normal voucher {} discount: {}", voucher.getCode(), productDiscount);
                result.setTotalProductDiscount(result.getTotalProductDiscount().add(productDiscount));
                appliedVouchers.add(new VoucherApplicationDetail(voucher.getId(), voucher.getVoucherCategory(), voucher.getDiscountType(), productDiscount));
            }
        }

        result.setRegularVoucherCount(regularCount);
        result.setShippingVoucherCount(shippingCount);
        result.setAppliedVouchers(appliedVouchers);

        log.debug("🎫 Final result: productDiscount={}, shippingDiscount={}, totalVouchers={}", 
            result.getTotalProductDiscount(), result.getTotalShippingDiscount(), appliedVouchers.size());

        return result;
    }

    @Override
    public void validateVoucherApplication(Order order, List<Voucher> vouchers, Integer userId) {
        long currentTime = System.currentTimeMillis();
        
        int regularVoucherCount = 0;
        int shippingVoucherCount = 0;

        // Calculate total potential discount to ensure it doesn't exceed order total
        BigDecimal totalPotentialDiscount = BigDecimal.ZERO;

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

            // Calculate potential discount for this voucher
            BigDecimal potentialDiscount = calculateSingleVoucherDiscount(voucher, order.getSubtotal(), order.getShippingFee());
            totalPotentialDiscount = totalPotentialDiscount.add(potentialDiscount);

            // Count voucher types by category
            if (voucher.getVoucherCategory() == VoucherCategory.SHIPPING) {
                shippingVoucherCount++;
            } else {
                regularVoucherCount++;
            }
        }

        // Check if total discount would make order total negative
        BigDecimal orderTotal = order.getSubtotal().add(order.getShippingFee() != null ? order.getShippingFee() : BigDecimal.ZERO);
        if (totalPotentialDiscount.compareTo(orderTotal) > 0) {
            log.warn("⚠️ Total voucher discount ({}) exceeds order total ({}) - will be capped at order total", 
                totalPotentialDiscount, orderTotal);
            // ✅ DON'T throw exception - just log warning and let calculation proceed
            // The actual calculation will cap the discount appropriately
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
        log.info("🎫 CALCULATING SINGLE VOUCHER: code={}, category={}, discountType={}, discountAmount={}, discountPercentage={}, orderSubtotal={}", 
            voucher.getCode(), voucher.getVoucherCategory(), voucher.getDiscountType(), 
            voucher.getDiscountAmount(), voucher.getDiscountPercentage(), orderSubtotal);
            
        BigDecimal discount = BigDecimal.ZERO;
        
        // ✅ NEW LOGIC: Use VoucherCategory to determine what to discount
        if (voucher.getVoucherCategory() == VoucherCategory.SHIPPING) {
            // Shipping voucher always discounts shipping fee
            discount = shippingFee != null ? shippingFee : BigDecimal.ZERO;
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
                    discount = voucher.getDiscountAmount() != null ? voucher.getDiscountAmount() : BigDecimal.ZERO;
                    break;
            }
            
            // ✅ FIX: Always cap normal voucher discount at order subtotal
            if (discount.compareTo(orderSubtotal) > 0) {
                log.info("🎫 Capping normal voucher {} discount from {} to {} (order subtotal)", 
                    voucher.getCode(), discount, orderSubtotal);
                discount = orderSubtotal;
            }
            
            // ✅ FIX: Only apply max discount limit if it's actually set and > 0
            if (voucher.getMaxDiscountValue() != null && 
                voucher.getMaxDiscountValue().compareTo(BigDecimal.ZERO) > 0 && 
                discount.compareTo(voucher.getMaxDiscountValue()) > 0) {
                log.info("🎫 Capping voucher {} discount from {} to {} (max discount limit)", 
                    voucher.getCode(), discount, voucher.getMaxDiscountValue());
                discount = voucher.getMaxDiscountValue();
            }
        }

        log.debug("🎫 Voucher {} final discount: {}", voucher.getCode(), discount);
        return discount;
    }

    @Override
    public boolean canUserUseVoucher(Integer userId, Integer voucherId) {
        Voucher voucher = voucherRepository.findById(voucherId).orElse(null);
        if (voucher == null || voucher.getUsageLimitPerUser() == null) return false;

        UserVoucher userVoucher = userVoucherRepository.findByUserIdAndVoucherId(userId, voucherId).orElse(null);
        
        if (userVoucher == null) {
            // First time using this voucher - check if user can receive more
            return true;
        }

        // ✅ NEW: Check available quantity (not used yet) against usage limit
        int availableQuantity = userVoucher.getQuantity() != null ? userVoucher.getQuantity() : 0;
        int usedCount = userVoucher.getUsedCount() != null ? userVoucher.getUsedCount() : 0;
        
        log.debug("🎫 canUserUseVoucher: userId={}, voucherId={}, availableQuantity={}, usedCount={}, usageLimitPerUser={}", 
            userId, voucherId, availableQuantity, usedCount, voucher.getUsageLimitPerUser());
        
        return availableQuantity > 0; // Can use if user has available vouchers
    }

    @Override
    public void updateVoucherUsage(List<Integer> voucherIds, Integer userId) {
        for (Integer voucherId : voucherIds) {
            // Update voucher global used count
            Voucher voucher = voucherRepository.findById(voucherId).orElse(null);
            if (voucher != null) {
                voucher.setUsedCount((voucher.getUsedCount() != null ? voucher.getUsedCount() : 0) + 1);
                voucherRepository.save(voucher);
            }

            // ✅ NEW: Update user voucher usage - decrease quantity, increase usedCount
            UserVoucher userVoucher = userVoucherRepository.findByUserIdAndVoucherId(userId, voucherId).orElse(null);
            
            if (userVoucher != null) {
                // Decrease available quantity and increase used count
                int currentQuantity = userVoucher.getQuantity() != null ? userVoucher.getQuantity() : 0;
                int currentUsedCount = userVoucher.getUsedCount() != null ? userVoucher.getUsedCount() : 0;
                
                if (currentQuantity > 0) {
                    userVoucher.setQuantity(currentQuantity - 1);
                    userVoucher.setUsedCount(currentUsedCount + 1);
                    userVoucherRepository.save(userVoucher);
                    
                    log.info("🎫 Updated voucher usage for user {} voucher {}: quantity {} -> {}, usedCount {} -> {}", 
                        userId, voucherId, currentQuantity, currentQuantity - 1, currentUsedCount, currentUsedCount + 1);
                } else {
                    log.warn("⚠️ Attempted to use voucher {} but user {} has no available quantity", voucherId, userId);
                }
            } else {
                log.warn("⚠️ Attempted to use voucher {} but user {} has no voucher record", voucherId, userId);
            }
        }
    }
}
