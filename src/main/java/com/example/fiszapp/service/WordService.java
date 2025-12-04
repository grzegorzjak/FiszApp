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
        
        // Fetch all words for the user
        Page<Word> wordPage = wordRepository.findByUserId(userId, pageable);
        
        // Apply filters in Java
        List<WordResponse> allWords = wordPage.getContent().stream()
            .map(word -> {
                boolean isUsed = wordRepository.isWordUsedInAcceptedCard(word.getId());
                return wordMapper.toResponse(word, isUsed);
            })
            .filter(wordResponse -> applyFilters(wordResponse, used, search))
            .toList();
        
        // Calculate pagination for filtered results
        long totalElements = countFilteredWords(userId, used, search, pageable.getSort());
        int totalPages = (int) Math.ceil((double) totalElements / size);
        
        return new PageResponse<>(
            allWords,
            wordPage.getNumber(),
            wordPage.getSize(),
            totalElements,
            totalPages
        );
    }
    
    private boolean applyFilters(WordResponse wordResponse, Boolean used, String search) {
        // Filter by used status
        if (used != null && wordResponse.isUsed() != used) {
            return false;
        }
        
        // Filter by search term
        if (search != null && !search.isBlank()) {
            String searchLower = search.toLowerCase();
            String originalTextLower = wordResponse.getOriginalText().toLowerCase();
            String canonicalTextLower = wordResponse.getCanonicalText().toLowerCase();
            
            if (!originalTextLower.contains(searchLower) && !canonicalTextLower.contains(searchLower)) {
                return false;
            }
        }
        
        return true;
    }
    
    private long countFilteredWords(UUID userId, Boolean used, String search, Sort sort) {
        // Fetch all words to count filtered results
        List<Word> allWords = wordRepository.findByUserId(userId, PageRequest.of(0, Integer.MAX_VALUE, sort))
            .getContent();
        
        return allWords.stream()
            .filter(word -> {
                boolean isUsed = wordRepository.isWordUsedInAcceptedCard(word.getId());
                WordResponse response = wordMapper.toResponse(word, isUsed);
                return applyFilters(response, used, search);
            })
            .count();
    }

    @Transactional
    public WordResponse createWord(UUID userId, CreateWordRequest request) {
        String canonicalText = canonicalize(request.originalText());
        
        if (wordRepository.existsByUserIdAndCanonicalText(userId, canonicalText)) {
            throw new WordConflictException("Word with canonical form '" + canonicalText + "' already exists");
        }
        
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

    @Transactional(readOnly = true)
    public long countFreeWords(UUID userId) {
        List<Word> allWords = wordRepository.findByUserId(userId, Pageable.unpaged()).getContent();
        
        return allWords.stream()
            .filter(word -> !wordRepository.isWordUsedInAcceptedCard(word.getId()))
            .count();
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
