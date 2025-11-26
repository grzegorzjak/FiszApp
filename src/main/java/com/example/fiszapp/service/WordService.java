package com.example.fiszapp.service;

import com.example.fiszapp.dto.common.PageResponse;
import com.example.fiszapp.dto.word.CreateWordRequest;
import com.example.fiszapp.dto.word.UpdateWordRequest;
import com.example.fiszapp.dto.word.WordResponse;
import com.example.fiszapp.entity.Card;
import com.example.fiszapp.entity.CardWord;
import com.example.fiszapp.entity.Word;
import com.example.fiszapp.exception.WordConflictException;
import com.example.fiszapp.exception.WordNotFoundException;
import com.example.fiszapp.mapper.WordMapper;
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
public class WordService {

    private final WordRepository wordRepository;
    private final CardWordRepository cardWordRepository;
    private final CardRepository cardRepository;
    private final SrsStateRepository srsStateRepository;
    private final WordMapper wordMapper;

    @Transactional(readOnly = true)
    public PageResponse<WordResponse> listWords(
        UUID userId,
        int page,
        int size,
        String sort,
        Boolean used,
        String search
    ) {
        size = Math.min(size, 100);
        
        Pageable pageable = createPageable(page, size, sort);
        
        Page<Word> wordPage;
        if (used != null || search != null) {
            wordPage = wordRepository.findByUserIdWithFilters(userId, used, search, pageable);
        } else {
            wordPage = wordRepository.findByUserId(userId, pageable);
        }
        
        List<WordResponse> content = wordPage.getContent().stream()
            .map(word -> {
                boolean isUsed = wordRepository.isWordUsedInAcceptedCard(word.getId());
                return wordMapper.toResponse(word, isUsed);
            })
            .toList();
        
        return new PageResponse<>(
            content,
            wordPage.getNumber(),
            wordPage.getSize(),
            wordPage.getTotalElements(),
            wordPage.getTotalPages()
        );
    }

    @Transactional
    public WordResponse createWord(UUID userId, CreateWordRequest request) {
        String canonicalText = canonicalize(request.originalText());
        
//        if (wordRepository.existsByUserIdAndCanonicalText(userId, canonicalText)) {
//            throw new WordConflictException("Word with canonical form '" + canonicalText + "' already exists");
//        }
        
        Word word = new Word();
        word.setUserId(userId);
        word.setOriginalText(request.originalText().trim());
        word.setCanonicalText(canonicalText);
        word.setLanguage(request.language().name());
        
        word = wordRepository.save(word);
        log.info("Created word {} for user {}", word.getId(), userId);
        
        return wordMapper.toResponse(word, false);
    }

    @Transactional
    public WordResponse updateWord(UUID userId, UUID wordId, UpdateWordRequest request) {
        Word word = wordRepository.findByIdAndUserId(wordId, userId)
            .orElseThrow(() -> new WordNotFoundException("Word not found: " + wordId));
        
        String newCanonicalText = canonicalize(request.originalText());
        
        if (!word.getCanonicalText().equals(newCanonicalText)) {
            if (wordRepository.existsByUserIdAndCanonicalText(userId, newCanonicalText)) {
                throw new WordConflictException("Word with canonical form '" + newCanonicalText + "' already exists");
            }
        }
        
        boolean wasUsedInAcceptedCard = wordRepository.isWordUsedInAcceptedCard(wordId);
        
        if (wasUsedInAcceptedCard) {
            archiveCardsUsingWord(userId, wordId);
        }
        
        word.setOriginalText(request.originalText().trim());
        word.setCanonicalText(newCanonicalText);
        word.setLanguage(request.language().name());
        
        word = wordRepository.save(word);
        log.info("Updated word {} for user {}", wordId, userId);
        
        return wordMapper.toResponse(word, false);
    }

    @Transactional
    public void deleteWord(UUID userId, UUID wordId) {
        Word word = wordRepository.findByIdAndUserId(wordId, userId)
            .orElseThrow(() -> new WordNotFoundException("Word not found: " + wordId));
        
        boolean wasUsedInAcceptedCard = wordRepository.isWordUsedInAcceptedCard(wordId);
        
        if (wasUsedInAcceptedCard) {
            archiveCardsUsingWord(userId, wordId);
        }
        
        wordRepository.delete(word);
        log.info("Deleted word {} for user {}", wordId, userId);
    }

    private void archiveCardsUsingWord(UUID userId, UUID wordId) {
        List<CardWord> cardWords = cardWordRepository.findByUserIdAndWordId(userId, wordId);
        
        for (CardWord cardWord : cardWords) {
            Card card = cardRepository.findById(cardWord.getCardId())
                .orElse(null);
            
            if (card != null && "ACCEPTED".equals(card.getStatus())) {
                card.setStatus("ARCHIVED");
                card.setArchivedAt(Instant.now());
                cardRepository.save(card);
                
                srsStateRepository.deleteByCardId(card.getId());
                
                List<CardWord> allCardWords = cardWordRepository.findByCardId(card.getId());
                cardWordRepository.deleteAll(allCardWords);
                
                log.info("Archived card {} and removed from SRS due to word {} change", card.getId(), wordId);
            }
        }
    }

    private String canonicalize(String text) {
        return text.trim().toLowerCase();
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
