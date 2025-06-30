package org.datn.bookstation.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.datn.bookstation.entity.enums.VoucherType;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "order_voucher")
public class OrderVoucher {
    @EmbeddedId
    private OrderVoucherId id;

    @MapsId("orderId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @MapsId("voucherId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "voucher_id", nullable = false)
    private Voucher voucher;

    // Loại voucher được áp dụng 
    @Enumerated(EnumType.STRING)
    @Column(name = "voucher_type", length = 20)
    private VoucherType voucherType;

    // Số tiền giảm giá thực tế được áp dụng
    @Column(name = "discount_applied", precision = 10, scale = 2)
    private BigDecimal discountApplied;

    // Thời gian áp dụng voucher
    @Column(name = "applied_at", nullable = false)
    private Long appliedAt;

    @PrePersist
    protected void onCreate() {
        appliedAt = System.currentTimeMillis();
        if (voucher != null) {
            voucherType = voucher.getVoucherType();
        }
    }
}