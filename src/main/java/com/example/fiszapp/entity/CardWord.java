package com.example.fiszapp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "card_words", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "word_id"}),
    @UniqueConstraint(columnNames = {"user_id", "card_id", "word_id"})
})
@Getter
@Setter
public class CardWord {

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
    @Column(name = "word_id", nullable = false)
    private UUID wordId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "user_id", referencedColumnName = "user_id", insertable = false, updatable = false),
        @JoinColumn(name = "card_id", referencedColumnName = "id", insertable = false, updatable = false)
    })
    private Card card;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "user_id", referencedColumnName = "user_id", insertable = false, updatable = false),
        @JoinColumn(name = "word_id", referencedColumnName = "id", insertable = false, updatable = false)
    })
    private Word word;
}
