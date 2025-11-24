package com.example.fiszapp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "srs_state", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "card_id"})
})
@Getter
@Setter
public class SrsState {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @NotNull
    @Column(name = "card_id", nullable = false)
    private UUID cardId;

    @NotNull
    @Column(nullable = false, precision = 3, scale = 2)
    private BigDecimal easiness;

    @NotNull
    @Column(name = "interval_days", nullable = false)
    private Integer intervalDays;

    @NotNull
    @Column(nullable = false)
    private Integer repetitions;

    @NotNull
    @Column(name = "due_at", nullable = false)
    private Instant dueAt;

    @Column(name = "last_grade")
    private Short lastGrade;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "user_id", referencedColumnName = "user_id", insertable = false, updatable = false),
        @JoinColumn(name = "card_id", referencedColumnName = "id", insertable = false, updatable = false)
    })
    private Card card;
}
