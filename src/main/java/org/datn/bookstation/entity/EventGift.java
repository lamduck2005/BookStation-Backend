package org.datn.bookstation.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "event_gift")
public class EventGift {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Size(max = 255)
    @NotNull
    @Nationalized
    @Column(name = "gift_name", nullable = false)
    private String giftName;

    @Nationalized
    @Lob
    @Column(name = "description")
    private String description;

    @Column(name = "gift_value", precision = 10, scale = 2)
    private BigDecimal giftValue;

    @Column(name = "quantity")
    @ColumnDefault("1")
    private Integer quantity = 1;

    @Column(name = "remaining_quantity")
    private Integer remainingQuantity;

    @Size(max = 500)
    @Nationalized
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Size(max = 100)
    @Nationalized
    @Column(name = "gift_type", length = 100)
    private String giftType; // BOOK, VOUCHER, POINT, PHYSICAL_ITEM

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book; // Nếu quà tặng là sách

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_id")
    private Voucher voucher; // Nếu quà tặng là voucher

    @Column(name = "point_value")
    private Integer pointValue; // Nếu quà tặng là điểm

    @Column(name = "is_active")
    @ColumnDefault("1")
    private Boolean isActive = true;

    @Column(name = "created_at")
    private Long createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = System.currentTimeMillis();
        if (remainingQuantity == null) {
            remainingQuantity = quantity;
        }
    }
}
