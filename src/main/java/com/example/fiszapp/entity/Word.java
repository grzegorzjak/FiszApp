package com.example.fiszapp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "words", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "id"}),
    @UniqueConstraint(columnNames = {"user_id", "canonical_text"})
})
@Getter
@Setter
public class Word {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @NotBlank
    @Column(name = "original_text", nullable = false, columnDefinition = "TEXT")
    private String originalText;

    @NotBlank
    @Column(name = "canonical_text", nullable = false, columnDefinition = "TEXT")
    private String canonicalText;

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String language;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", insertable = false, updatable = false)
    private User user;
}
