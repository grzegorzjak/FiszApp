package com.example.fiszapp.repository;

import com.example.fiszapp.entity.Word;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WordRepository extends JpaRepository<Word, UUID> {

    List<Word> findByUserId(UUID userId);

    @Query("""
        SELECT w FROM Word w
        WHERE w.userId = :userId
        AND (:isUsed = true AND EXISTS (
            SELECT 1 FROM CardWord cw WHERE cw.wordId = w.id AND cw.userId = :userId
        ))
        OR (:isUsed = false AND NOT EXISTS (
            SELECT 1 FROM CardWord cw WHERE cw.wordId = w.id AND cw.userId = :userId
        ))
        """)
    List<Word> findByUserIdAndUsageStatus(@Param("userId") UUID userId, @Param("isUsed") boolean isUsed);

    @Query("""
        SELECT CASE WHEN COUNT(cw) > 0 THEN true ELSE false END
        FROM CardWord cw
        WHERE cw.wordId = :wordId AND cw.userId = :userId
        """)
    boolean isWordUsed(@Param("wordId") UUID wordId, @Param("userId") UUID userId);

    @Query("""
        SELECT cw.cardId
        FROM CardWord cw
        WHERE cw.wordId = :wordId AND cw.userId = :userId
        """)
    UUID findCardIdUsingWord(@Param("wordId") UUID wordId, @Param("userId") UUID userId);
}
