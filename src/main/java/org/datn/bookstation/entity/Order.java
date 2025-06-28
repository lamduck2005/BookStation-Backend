package org.datn.bookstation.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import org.datn.bookstation.entity.enums.OrderStatus;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "\"order\"")
public class Order {
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id")
    private User staff;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id")
    private Address address;

    @NotNull
    @Column(name = "order_date", nullable = false)
    private Long orderDate;

    @NotNull
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @ColumnDefault("1")
    @Column(name = "status")
    private Byte status;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", length = 20)
    private OrderStatus orderStatus;
    
    @Size(max = 20)
    @NotNull
    @Nationalized
    @Column(name = "order_type", nullable = false, length = 20)
    private String orderType;

    @Column(name = "created_at", nullable = false)
    private Long createdAt;

    @Column(name = "updated_at")
    private Long updatedAt;

    @NotNull
    @Column(name = "created_by", nullable = false)
    private Integer createdBy;

    @Column(name = "updated_by")
    private Integer updatedBy;

    @Size(max = 50)
    @NotNull
    @Nationalized
    @Column(name = "code", nullable = false, length = 50)
    private String code;

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