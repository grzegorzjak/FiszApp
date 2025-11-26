package com.example.fiszapp.repository;

import com.example.fiszapp.entity.SrsState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SrsStateRepository extends JpaRepository<SrsState, UUID> {

    void deleteByCardId(UUID cardId);

    void deleteByUserId(UUID userId);
}
