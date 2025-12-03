package com.example.fiszapp.service;

import com.example.fiszapp.dto.openrouter.GeneratedCard;
import com.example.fiszapp.dto.openrouter.OpenRouterRequest;
import com.example.fiszapp.dto.openrouter.OpenRouterResponse;
import com.example.fiszapp.entity.Word;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenRouterClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${openrouter.api.url}")
    private String apiUrl;

    @Value("${openrouter.api.key}")
    private String apiKey;

    @Value("${openrouter.model}")
    private String model;

    @Value("${openrouter.max.tokens}")
    private Integer maxTokens;

    @Value("${openrouter.temperature}")
    private Double temperature;

    public List<GeneratedCard> generateCards(List<Word> words, int maxCards) {
        String prompt = buildPrompt(words, maxCards);
        
        OpenRouterRequest request = new OpenRouterRequest(
            model,
            List.of(new OpenRouterRequest.Message("user", prompt)),
            maxTokens,
            temperature
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        headers.set("HTTP-Referer", "https://fiszapp.com");
        headers.set("X-Title", "FiszApp");

        HttpEntity<OpenRouterRequest> entity = new HttpEntity<>(request, headers);

        try {
            log.info("Calling OpenRouter API with {} words, requesting max {} cards", words.size(), maxCards);
            OpenRouterResponse response = restTemplate.postForObject(apiUrl, entity, OpenRouterResponse.class);
            
            if (response == null || response.choices() == null || response.choices().isEmpty()) {
                log.warn("Empty response from OpenRouter API");
                return List.of();
            }

            String content = response.choices().get(0).message().content();
            log.info("Received response from OpenRouter API. Tokens used: {}", 
                response.usage() != null ? response.usage().totalTokens() : "unknown");
            
            return parseGeneratedCards(content, words);
        } catch (Exception e) {
            log.error("Error calling OpenRouter API", e);
            return List.of();
        }
    }

    private String buildPrompt(List<Word> words, int maxCards) {
        StringBuilder wordList = new StringBuilder();
        for (Word word : words) {
            wordList.append("- ").append(word.getOriginalText()).append("\n");
        }

        return String.format("""
            You are a language learning assistant. Generate flashcards for English learners (Polish native speakers).
            
            RULES:
            1. Generate up to %d flashcards
            2. Each flashcard must:
               - Use AT LEAST 2 words from the provided list
               - Take every generated EN sentence and count all words from this sentence. Maximum number of words is 8, minimum is 4.
               - Use B1/B2 level English (intermediate)
               - Be neutral in content
               - Avoid rare idioms
               - Include Polish translation (back) with the most common meaning
            3. Each word from the list can only be used in ONE flashcard
            4. Generate only complete, valid flashcards
            
            WORDS TO USE:
            %s
            
            OUTPUT FORMAT (JSON array):
            [
              {
                "sentenceEn": "I need to buy some fresh bread today.",
                "translationPl": "Muszę dzisiaj kupić trochę świeżego chleba.",
                "usedWords": ["buy", "bread"]
              }
            ]
            
            Return ONLY the JSON array, no additional text or explanation.
            """, maxCards, wordList.toString());
    }

    private List<GeneratedCard> parseGeneratedCards(String content, List<Word> availableWords) {
        List<GeneratedCard> cards = new ArrayList<>();
        
        try {
            String jsonContent = extractJsonArray(content);
            GeneratedCard[] parsedCards = objectMapper.readValue(jsonContent, GeneratedCard[].class);
            
            for (GeneratedCard card : parsedCards) {
                if (isValidCard(card, availableWords)) {
                    cards.add(card);
                    log.debug("Valid card parsed: {}", card.sentenceEn());
                } else {
                    log.warn("Invalid card rejected: {}", card.sentenceEn());
                }
            }
        } catch (JsonProcessingException e) {
            log.error("Error parsing JSON response from OpenRouter", e);
        }
        
        return cards;
    }

    private String extractJsonArray(String content) {
        int start = content.indexOf('[');
        int end = content.lastIndexOf(']');
        
        if (start != -1 && end != -1 && end > start) {
            return content.substring(start, end + 1);
        }
        
        return content;
    }

    private boolean isValidCard(GeneratedCard card, List<Word> availableWords) {
        if (card.sentenceEn() == null || card.translationPl() == null || card.usedWords() == null) {
            log.warn("Card has null fields");
            return false;
        }

        String[] words = card.sentenceEn().trim().split("\\s+");
        if (words.length < 4 || words.length > 8) {
            log.warn("Card sentence length invalid: {} words (expected 4-8)", words.length);
            return false;
        }

        if (card.usedWords().size() < 2) {
            log.warn("Card uses less than 2 words from the list: {}", card.usedWords().size());
            return false;
        }

        List<String> availableWordTexts = availableWords.stream()
            .map(Word::getOriginalText)
            .map(String::toLowerCase)
            .toList();

        for (String usedWord : card.usedWords()) {
//            if (!availableWordTexts.contains(usedWord.toLowerCase())) {
//                log.warn("Card uses word not in available list: {}", usedWord);
//                return false;
//            }
        }

        return true;
    }
}
