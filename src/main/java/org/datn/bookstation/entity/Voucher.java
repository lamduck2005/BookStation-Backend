package org.datn.bookstation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "voucher")
public class Voucher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 20)
    @NotNull
    @Nationalized
    @Column(name = "code", nullable = false, length = 20)
    private String code;

    @NotNull
    @Column(name = "discount_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal discountPercentage;

    @NotNull
    @Column(name = "start_time", nullable = false)
    private Long startTime;

    @NotNull
    @Column(name = "end_time", nullable = false)
    private Long endTime;

    @Column(name = "min_order_value", precision = 10, scale = 2)
    private BigDecimal minOrderValue;

    @Column(name = "max_discount_value", precision = 10, scale = 2)
    private BigDecimal maxDiscountValue;

    @ColumnDefault("1")
    @Nationalized
    @Column(name = "status", length = 50)
    private byte status;

    @Column(name = "created_at", nullable = false)
    private Long createdAt;

    @Column(name = "updated_at")
    private Long updatedAt;

    @NotNull
    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = System.currentTimeMillis();
        updatedAt = System.currentTimeMillis();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = System.currentTimeMillis();
    }
}