package org.datn.bookstation.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.datn.bookstation.entity.enums.GiftClaimStatus;
import org.datn.bookstation.entity.enums.GiftDeliveryMethod;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Setter
@Entity
@Table(name = "event_gift_claim")
public class EventGiftClaim {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_participant_id", nullable = false)
    private EventParticipant eventParticipant;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_gift_id", nullable = false)
    private EventGift eventGift;

    @Column(name = "claimed_at")
    private Long claimedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "claim_status", length = 20)
    @ColumnDefault("'PENDING'")
    private GiftClaimStatus claimStatus = GiftClaimStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_method", length = 20)
    @ColumnDefault("'ONLINE_SHIPPING'")
    private GiftDeliveryMethod deliveryMethod = GiftDeliveryMethod.ONLINE_SHIPPING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_order_id")
    private Order deliveryOrder; // Chỉ dùng khi delivery_method = ONLINE_SHIPPING

    @Column(name = "store_pickup_code", length = 50)
    private String storePickupCode; // Mã để nhận quà tại cửa hàng

    @Column(name = "pickup_store_id")
    private Integer pickupStoreId; // ID cửa hàng để nhận quà

    @Column(name = "staff_confirmed_by")
    private Integer staffConfirmedBy; // ID nhân viên xác nhận trao quà

    @Column(name = "auto_delivered")
    @ColumnDefault("0")
    private Boolean autoDelivered = false; // true cho điểm thưởng, voucher

    @Column(name = "completed_at")
    private Long completedAt; // Thời điểm hoàn thành việc nhận quà

    @Lob
    @Column(name = "notes")
    private String notes;

    @PrePersist
    protected void onCreate() {
        claimedAt = System.currentTimeMillis();
    }
}
