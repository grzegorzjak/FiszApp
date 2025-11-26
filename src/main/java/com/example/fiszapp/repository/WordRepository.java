package com.example.fiszapp.repository;

import com.example.fiszapp.entity.Word;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WordRepository extends JpaRepository<Word, UUID> {

    Optional<Word> findByIdAndUserId(UUID id, UUID userId);

    boolean existsByUserIdAndCanonicalText(UUID userId, String canonicalText);

    Page<Word> findByUserId(UUID userId, Pageable pageable);

    @Query("""
        SELECT w FROM Word w
        WHERE w.userId = :userId
        AND (LOWER(w.originalText) LIKE LOWER(CONCAT('%', :search, '%'))
        OR LOWER(w.canonicalText) LIKE LOWER(CONCAT('%', :search, '%')))
        """)
    Page<Word> findByUserIdWithSearch(
        @Param("userId") UUID userId,
        @Param("search") String search,
        Pageable pageable
    );

    @Query("""
        SELECT w FROM Word w
        LEFT JOIN CardWord cw ON cw.word.id = w.id
        LEFT JOIN Card c ON cw.card.id = c.id AND c.status = 'ACCEPTED'
        WHERE w.userId = :userId
        AND (:used IS NULL OR (CASE WHEN c.id IS NULL THEN false ELSE true END) = :used)
        AND (:search IS NULL OR LOWER(w.originalText) LIKE LOWER(CONCAT('%', :search, '%'))
        OR LOWER(w.canonicalText) LIKE LOWER(CONCAT('%', :search, '%')))
        GROUP BY w.id
        """)
    Page<Word> findByUserIdWithFilters(
        @Param("userId") UUID userId,
        @Param("used") Boolean used,
        @Param("search") String search,
        Pageable pageable
    );

    @Query("""
        SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END
        FROM Word w
        JOIN CardWord cw ON cw.word.id = w.id
        JOIN Card c ON cw.card.id = c.id
        WHERE w.id = :wordId AND c.status = 'ACCEPTED'
        """)
    boolean isWordUsedInAcceptedCard(@Param("wordId") UUID wordId);

    void deleteByUserId(UUID userId);
}
