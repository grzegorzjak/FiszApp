package com.example.fiszapp.service;

import com.example.fiszapp.dto.generation.GeneratedCardSummary;
import com.example.fiszapp.dto.generation.GenerationRequest;
import com.example.fiszapp.dto.generation.GenerationResponse;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenerationService {

    private final CardRepository cardRepository;
    private final CardWordRepository cardWordRepository;
    private final WordRepository wordRepository;

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
        
        // TODO: AI INTEGRATION - Replace mock generation with OpenRouter API call
        // TODO: 1. Group free words into sets of 2-8 words per candidate card
        // TODO: 2. Build prompt for OpenRouter with word groups and generation rules:
        //         - Sentence: 4-8 words, EN, B1/B2 level, no rare idioms
        //         - Translation: PL, most common meaning
        //         - Use at least 2 words from provided list per card
        // TODO: 3. Call OpenRouter API (use RestTemplate or WebClient)
        // TODO: 4. Parse AI response and validate each generated card:
        //         - Check sentence length (4-8 words)
        // TODO: 5. Discard invalid cards, keep up to maxCards valid ones
        
        List<GeneratedCardSummary> createdCards = new ArrayList<>();
        int cardsToGenerate = Math.min(maxCards, freeWords.size() / 2);
        
        // MOCK IMPLEMENTATION - Replace this loop with AI-generated cards
        for (int i = 0; i < cardsToGenerate && (i * 2 + 1) < freeWords.size(); i++) {
            Word word1 = freeWords.get(i * 2);
            Word word2 = freeWords.get(i * 2 + 1);
            
            Card card = createDraftCard(userId, word1, word2);
            card = cardRepository.save(card);
            
            createCardWordRelations(userId, card.getId(), List.of(word1.getId(), word2.getId()));
            
            createdCards.add(new GeneratedCardSummary(
                card.getId(),
                card.getFrontEn(),
                card.getBackPl(),
                List.of(word1.getId(), word2.getId())
            ));
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

    private Card createDraftCard(UUID userId, Word word1, Word word2) {
        Card card = new Card();
        card.setUserId(userId);
        card.setStatus("DRAFT");
        card.setFrontEn(generateMockSentence(word1, word2));
        card.setBackPl(generateMockTranslation(word1, word2));
        return card;
    }

    // MOCK METHODS - Remove when AI integration is implemented
    private String generateMockSentence(Word word1, Word word2) {
        return String.format("Example sentence with %s and %s.", 
            word1.getOriginalText(), 
            word2.getOriginalText()
        );
    }

    private String generateMockTranslation(Word word1, Word word2) {
        return String.format("Przykładowe zdanie z %s i %s.", 
            word1.getOriginalText(), 
            word2.getOriginalText()
        );
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
