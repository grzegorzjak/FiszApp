package com.example.fiszapp.repository;

import com.example.fiszapp.entity.Card;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CardRepository extends JpaRepository<Card, UUID> {

    Optional<Card> findByIdAndUserId(UUID id, UUID userId);

    Page<Card> findByUserId(UUID userId, Pageable pageable);

    Page<Card> findByUserIdAndStatus(UUID userId, String status, Pageable pageable);

    void deleteByUserId(UUID userId);
}
