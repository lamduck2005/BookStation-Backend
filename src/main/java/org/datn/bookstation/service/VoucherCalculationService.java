package org.datn.bookstation.service;

import org.datn.bookstation.entity.Order;
import org.datn.bookstation.entity.Voucher;
import org.datn.bookstation.entity.enums.VoucherType;

import java.math.BigDecimal;
import java.util.List;

public interface VoucherCalculationService {
    
    /**
     * Validate và tính toán giảm giá từ vouchers
     */
    VoucherCalculationResult calculateVoucherDiscount(Order order, List<Integer> voucherIds, Integer userId);
    
    /**
     * Validate business rules cho việc áp dụng voucher
     */
    void validateVoucherApplication(Order order, List<Voucher> vouchers, Integer userId);
    
    /**
     * Tính toán giảm giá cho một voucher cụ thể
     */
    BigDecimal calculateSingleVoucherDiscount(Voucher voucher, BigDecimal orderSubtotal, BigDecimal shippingFee);
    
    /**
     * Kiểm tra xem user có thể sử dụng voucher không
     */
    boolean canUserUseVoucher(Integer userId, Integer voucherId);
    
    /**
     * Update số lần sử dụng voucher sau khi áp dụng thành công
     */
    void updateVoucherUsage(List<Integer> voucherIds, Integer userId);

    class VoucherCalculationResult {
        private BigDecimal totalProductDiscount = BigDecimal.ZERO;
        private BigDecimal totalShippingDiscount = BigDecimal.ZERO;
        private int regularVoucherCount = 0;
        private int shippingVoucherCount = 0;
        private List<VoucherApplicationDetail> appliedVouchers;

        // Getters and setters
        public BigDecimal getTotalProductDiscount() { return totalProductDiscount; }
        public void setTotalProductDiscount(BigDecimal totalProductDiscount) { this.totalProductDiscount = totalProductDiscount; }
        
        public BigDecimal getTotalShippingDiscount() { return totalShippingDiscount; }
        public void setTotalShippingDiscount(BigDecimal totalShippingDiscount) { this.totalShippingDiscount = totalShippingDiscount; }
        
        public int getRegularVoucherCount() { return regularVoucherCount; }
        public void setRegularVoucherCount(int regularVoucherCount) { this.regularVoucherCount = regularVoucherCount; }
        
        public int getShippingVoucherCount() { return shippingVoucherCount; }
        public void setShippingVoucherCount(int shippingVoucherCount) { this.shippingVoucherCount = shippingVoucherCount; }
        
        public List<VoucherApplicationDetail> getAppliedVouchers() { return appliedVouchers; }
        public void setAppliedVouchers(List<VoucherApplicationDetail> appliedVouchers) { this.appliedVouchers = appliedVouchers; }
    }

    class VoucherApplicationDetail {
        private Integer voucherId;
        private VoucherType voucherType;
        private BigDecimal discountApplied;

        public VoucherApplicationDetail(Integer voucherId, VoucherType voucherType, BigDecimal discountApplied) {
            this.voucherId = voucherId;
            this.voucherType = voucherType;
            this.discountApplied = discountApplied;
        }

        // Getters and setters
        public Integer getVoucherId() { return voucherId; }
        public void setVoucherId(Integer voucherId) { this.voucherId = voucherId; }
        
        public VoucherType getVoucherType() { return voucherType; }
        public void setVoucherType(VoucherType voucherType) { this.voucherType = voucherType; }
        
        public BigDecimal getDiscountApplied() { return discountApplied; }
        public void setDiscountApplied(BigDecimal discountApplied) { this.discountApplied = discountApplied; }
    }
}
