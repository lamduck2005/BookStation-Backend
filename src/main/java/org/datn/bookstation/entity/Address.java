package org.datn.bookstation.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "address")
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Size(max = 100)
    @NotNull
    @Nationalized
    @Column(name = "recipient_name", nullable = false, length = 100)
    private String recipientName;

    @NotNull
    @Nationalized
    @Lob
    @Column(name = "address_detail", nullable = false)
    private String addressDetail;

    @Size(max = 20)
    @NotNull
    @Nationalized
    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @ColumnDefault("0")
    @Column(name = "is_default")
    private Boolean isDefault;

    @NotNull
    @ColumnDefault("getdate()")
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @NotNull
    @Column(name = "created_by", nullable = false)
    private Integer createdBy;

    @Column(name = "updated_by")
    private Integer updatedBy;

    @Size(max = 50)
    @Nationalized
    @Column(name = "status", length = 50)
    private String status;

}