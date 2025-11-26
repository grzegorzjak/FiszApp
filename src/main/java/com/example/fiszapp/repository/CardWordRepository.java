package com.example.fiszapp.repository;

import com.example.fiszapp.entity.CardWord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CardWordRepository extends JpaRepository<CardWord, UUID> {

    List<CardWord> findByUserIdAndWordId(UUID userId, UUID wordId);

    List<CardWord> findByCardId(UUID cardId);

    void deleteByUserId(UUID userId);
}
