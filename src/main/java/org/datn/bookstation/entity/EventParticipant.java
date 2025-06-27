package org.datn.bookstation.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.datn.bookstation.entity.enums.ParticipantStatus;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Setter
@Entity
@Table(name = "event_participant")
public class EventParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "joined_at")
    private Long joinedAt;

    @Column(name = "is_winner")
    @ColumnDefault("0")
    private Boolean isWinner = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "completion_status", length = 20)
    @ColumnDefault("'JOINED'")
    private ParticipantStatus completionStatus = ParticipantStatus.JOINED;

    @Lob
    @Column(name = "notes")
    private String notes;

    @PrePersist
    protected void onCreate() {
        joinedAt = System.currentTimeMillis();
    }
}
