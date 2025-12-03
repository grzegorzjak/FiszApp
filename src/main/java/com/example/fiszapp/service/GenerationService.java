package com.example.fiszapp.service;

import com.example.fiszapp.dto.generation.GeneratedCardSummary;
import com.example.fiszapp.dto.generation.GenerationRequest;
import com.example.fiszapp.dto.generation.GenerationResponse;
import com.example.fiszapp.dto.openrouter.GeneratedCard;
import com.example.fiszapp.entity.Card;
import com.example.fiszapp.entity.CardWord;
import com.example.fiszapp.entity.Word;
import com.example.fiszapp.exception.NotEnoughFreeWordsException;
import com.example.fiszapp.repository.CardRepository;
import com.example.fiszapp.repository.CardWordRepository;
import com.example.fiszapp.repository.WordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenerationService {

    private final CardRepository cardRepository;
    private final CardWordRepository cardWordRepository;
    private final WordRepository wordRepository;
    private final OpenRouterClient openRouterClient;

    @Transactional
    public GenerationResponse generateCards(UUID userId, GenerationRequest request) {
        int maxCards = request.getMaxCards() != null ? request.getMaxCards() : 10;
        
        List<Word> freeWords = getFreeWords(userId);
        
        if (freeWords.size() < 2) {
            throw new NotEnoughFreeWordsException(
                "At least 2 free words are required for card generation. Current free words: " + freeWords.size()
            );
        }
        
        UUID batchId = UUID.randomUUID();
        Instant createdAt = Instant.now();
        
        List<GeneratedCard> aiGeneratedCards = openRouterClient.generateCards(freeWords, maxCards);
        
        log.info("OpenRouter generated {} cards from {} free words", aiGeneratedCards.size(), freeWords.size());
        
        List<GeneratedCardSummary> createdCards = new ArrayList<>();
        Map<String, Word> wordMap = buildWordMap(freeWords);
        Set<UUID> usedWordIds = new HashSet<>();
        
        for (GeneratedCard aiCard : aiGeneratedCards) {
            List<UUID> cardWordIds = mapUsedWordsToIds(aiCard.usedWords(), wordMap);
            
            if (cardWordIds.isEmpty()) {
                log.warn("Skipping card - no valid word IDs found: {}", aiCard.sentenceEn());
                continue;
            }
            
            if (hasConflictWithUsedWords(cardWordIds, usedWordIds)) {
                log.warn("Skipping card - word already used in another card: {}", aiCard.sentenceEn());
                continue;
            }
            
            Card card = createCardFromAI(userId, aiCard);
            card = cardRepository.save(card);
            
            createCardWordRelations(userId, card.getId(), cardWordIds);
            
            usedWordIds.addAll(cardWordIds);
            
            createdCards.add(new GeneratedCardSummary(
                card.getId(),
                card.getFrontEn(),
                card.getBackPl(),
                cardWordIds
            ));
            
            if (createdCards.size() >= maxCards) {
                break;
            }
        }
        
        log.info("Generated {} cards for user {} in batch {}", createdCards.size(), userId, batchId);
        
        return new GenerationResponse(
            batchId,
            createdAt,
            maxCards,
            createdCards.size(),
            createdCards
        );
    }

    private List<Word> getFreeWords(UUID userId) {
        List<Word> allUserWords = wordRepository.findByUserId(userId, org.springframework.data.domain.Pageable.unpaged())
            .getContent();
        
        return allUserWords.stream()
            .filter(word -> {
                List<CardWord> cardWords = cardWordRepository.findByUserIdAndWordId(userId, word.getId());
                return cardWords.isEmpty();
            })
            .collect(Collectors.toList());
    }

    private Card createCardFromAI(UUID userId, GeneratedCard aiCard) {
        Card card = new Card();
        card.setUserId(userId);
        card.setStatus("DRAFT");
        card.setFrontEn(aiCard.sentenceEn());
        card.setBackPl(aiCard.translationPl());
        return card;
    }

    private Map<String, Word> buildWordMap(List<Word> words) {
        return words.stream()
            .collect(Collectors.toMap(
                word -> word.getOriginalText().toLowerCase(),
                word -> word
            ));
    }

    private List<UUID> mapUsedWordsToIds(List<String> usedWords, Map<String, Word> wordMap) {
        return usedWords.stream()
            .map(String::toLowerCase)
            .map(wordMap::get)
            .filter(Objects::nonNull)
            .map(Word::getId)
            .toList();
    }

    private boolean hasConflictWithUsedWords(List<UUID> newWordIds, Set<UUID> usedWordIds) {
        return newWordIds.stream().anyMatch(usedWordIds::contains);
    }

    private void createCardWordRelations(UUID userId, UUID cardId, List<UUID> wordIds) {
        for (UUID wordId : wordIds) {
            CardWord cardWord = new CardWord();
            cardWord.setUserId(userId);
            cardWord.setCardId(cardId);
            cardWord.setWordId(wordId);
            cardWordRepository.save(cardWord);
        }
    }
}
