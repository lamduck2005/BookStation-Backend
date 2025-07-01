package org.datn.bookstation.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

@Getter
@Setter
@Entity
@Table(name = "event_history")
public class EventHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Size(max = 100)
    @NotNull
    @Column(name = "action_type", nullable = false, length = 100)
    private String actionType; // CREATED, UPDATED, PUBLISHED, STARTED, COMPLETED, CANCELLED

    @Nationalized
    @Lob
    @Column(name = "description")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by")
    private User performedBy;

    @Column(name = "created_at")
    private Long createdAt;

    @Nationalized
    @Lob
    @Column(name = "old_values")
    private String oldValues; // JSON string để lưu giá trị cũ

    @Nationalized
    @Lob
    @Column(name = "new_values")
    private String newValues; // JSON string để lưu giá trị mới

    @PrePersist
    protected void onCreate() {
        createdAt = System.currentTimeMillis();
    }
}
