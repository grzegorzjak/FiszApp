package com.example.fiszapp.service;

import com.example.fiszapp.dto.card.CardDetailResponse;
import com.example.fiszapp.dto.card.CardResponse;
import com.example.fiszapp.dto.card.UpdateCardRequest;
import com.example.fiszapp.dto.common.PageResponse;
import com.example.fiszapp.dto.enums.CardStatus;
import com.example.fiszapp.entity.Card;
import com.example.fiszapp.entity.CardWord;
import com.example.fiszapp.entity.SrsState;
import com.example.fiszapp.entity.Word;
import com.example.fiszapp.exception.CardInvalidStatusException;
import com.example.fiszapp.exception.CardNotFoundException;
import com.example.fiszapp.mapper.CardMapper;
import com.example.fiszapp.repository.CardRepository;
import com.example.fiszapp.repository.CardWordRepository;
import com.example.fiszapp.repository.SrsStateRepository;
import com.example.fiszapp.repository.WordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardService {

    private final CardRepository cardRepository;
    private final CardWordRepository cardWordRepository;
    private final WordRepository wordRepository;
    private final SrsStateRepository srsStateRepository;
    private final CardMapper cardMapper;

    @Transactional(readOnly = true)
    public PageResponse<CardResponse> listCards(
        UUID userId,
        String status,
        int page,
        int size,
        String sort
    ) {
        size = Math.min(size, 100);
        
        Pageable pageable = createPageable(page, size, sort);
        
        Page<Card> cardPage;
        if (status != null && !status.isBlank()) {
            cardPage = cardRepository.findByUserIdAndStatus(userId, status.toUpperCase(), pageable);
        } else {
            cardPage = cardRepository.findByUserId(userId, pageable);
        }
        
        List<CardResponse> content = cardPage.getContent().stream()
            .map(card -> {
                List<UUID> usedWordIds = cardWordRepository.findByCardId(card.getId()).stream()
                    .map(CardWord::getWordId)
                    .toList();
                return cardMapper.toCardResponse(card, usedWordIds);
            })
            .toList();
        
        return new PageResponse<>(
            content,
            cardPage.getNumber(),
            cardPage.getSize(),
            cardPage.getTotalElements(),
            cardPage.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public CardDetailResponse getCard(UUID userId, UUID cardId) {
        Card card = cardRepository.findByIdAndUserId(cardId, userId)
            .orElseThrow(() -> new CardNotFoundException("Card not found: " + cardId));
        
        List<CardWord> cardWords = cardWordRepository.findByCardId(cardId);
        List<Word> usedWords = cardWords.stream()
            .map(CardWord::getWordId)
            .map(wordId -> wordRepository.findById(wordId).orElse(null))
            .filter(word -> word != null)
            .toList();
        
        return cardMapper.toCardDetailResponse(card, usedWords);
    }

    @Transactional
    public CardDetailResponse updateCard(UUID userId, UUID cardId, UpdateCardRequest request) {
        Card card = cardRepository.findByIdAndUserId(cardId, userId)
            .orElseThrow(() -> new CardNotFoundException("Card not found: " + cardId));
        
        if (!"DRAFT".equals(card.getStatus())) {
            throw new CardInvalidStatusException("Only draft cards can be updated");
        }
        
        if (request.getStatus() != null) {
            handleStatusChange(card, request.getStatus());
        } else if (request.getFrontEn() != null || request.getBackPl() != null) {
            handleContentEdit(card, request);
        }
        
        card = cardRepository.save(card);
        log.info("Updated card {} for user {}", cardId, userId);
        
        return getCard(userId, cardId);
    }

    private void handleStatusChange(Card card, CardStatus newStatus) {
        String currentStatus = card.getStatus();
        String targetStatus = newStatus.name();
        
        if ("DRAFT".equals(currentStatus) && "ACCEPTED".equals(targetStatus)) {
            acceptCard(card);
        } else if ("DRAFT".equals(currentStatus) && "ARCHIVED".equals(targetStatus)) {
            rejectCard(card);
        } else {
            throw new CardInvalidStatusException(
                "Invalid status transition from " + currentStatus + " to " + targetStatus
            );
        }
    }

    private void acceptCard(Card card) {
        List<CardWord> cardWords = cardWordRepository.findByCardId(card.getId());
        
        if (cardWords.size() < 2) {
            throw new CardInvalidStatusException("Card must use at least 2 words");
        }
        
        card.setStatus("ACCEPTED");
        card.setAcceptedAt(Instant.now());
        
        SrsState srsState = new SrsState();
        srsState.setUserId(card.getUserId());
        srsState.setCardId(card.getId());
        srsState.setIntervalDays(1);
        srsState.setRepetitions(0);
        srsState.setEasiness(new java.math.BigDecimal("2.50"));
        srsState.setDueAt(Instant.now());
        srsStateRepository.save(srsState);
        
        log.info("Accepted card {} and created SRS state", card.getId());
    }

    private void rejectCard(Card card) {
        card.setStatus("ARCHIVED");
        card.setArchivedAt(Instant.now());
        
        List<CardWord> cardWords = cardWordRepository.findByCardId(card.getId());
        cardWordRepository.deleteAll(cardWords);
        
        log.info("Rejected card {} and freed {} words", card.getId(), cardWords.size());
    }

    private void handleContentEdit(Card card, UpdateCardRequest request) {
        if (request.getFrontEn() != null && !request.getFrontEn().isBlank()) {
            card.setFrontEn(request.getFrontEn().trim());
        }
        
        if (request.getBackPl() != null && !request.getBackPl().isBlank()) {
            card.setBackPl(request.getBackPl().trim());
        }
        
        log.info("Edited content of card {}", card.getId());
    }

    private Pageable createPageable(int page, int size, String sort) {
        if (sort == null || sort.isBlank()) {
            return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        }
        
        String[] parts = sort.split(",");
        String property = parts[0].trim();
        Sort.Direction direction = parts.length > 1 && "desc".equalsIgnoreCase(parts[1].trim())
            ? Sort.Direction.DESC
            : Sort.Direction.ASC;
        
        return PageRequest.of(page, size, Sort.by(direction, property));
    }
}
