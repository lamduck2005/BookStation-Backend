package org.datn.bookstation.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.datn.bookstation.entity.enums.EventStatus;
import org.datn.bookstation.entity.enums.EventType;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "event")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 255)
    @NotNull
    @Nationalized
    @Column(name = "event_name", nullable = false)
    private String eventName;

    @Nationalized
    @Lob
    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", length = 50)
    private EventType eventType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_category_id")
    private EventCategory eventCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    @ColumnDefault("'DRAFT'")
    private EventStatus status = EventStatus.DRAFT;

    @Column(name = "start_date")
    private Long startDate;

    @Column(name = "end_date")
    private Long endDate;

    @Column(name = "max_participants")
    private Integer maxParticipants;

    @Column(name = "current_participants")
    @ColumnDefault("0")
    private Integer currentParticipants = 0;

    @Size(max = 500)
    @Nationalized
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Size(max = 500)
    @Nationalized
    @Column(name = "location", length = 500)
    private String location;

    @Nationalized
    @Lob
    @Column(name = "rules")
    private String rules;

    @Column(name = "is_online")
    @ColumnDefault("0")
    private Boolean isOnline = false;

    @Column(name = "created_at")
    private Long createdAt;

    @Column(name = "updated_at")
    private Long updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<EventGift> eventGifts = new LinkedHashSet<>();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<EventParticipant> eventParticipants = new LinkedHashSet<>();

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
