package com.example.fiszapp.service;

import com.example.fiszapp.config.KafkaConfig;
import com.example.fiszapp.event.WordCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
@Slf4j
public class WordEventPublisher {

    private final KafkaTemplate<String, WordCreatedEvent> kafkaTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleWordCreatedEvent(WordCreatedEvent event) {
        try {
            String key = event.userId().toString();
            kafkaTemplate.send(KafkaConfig.WORD_CREATED_TOPIC, key, event)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to send word-created event to Kafka for wordId={}, userId={}",
                                    event.wordId(), event.userId(), ex);
                        } else {
                            log.info("Successfully sent word-created event to Kafka: wordId={}, userId={}, partition={}",
                                    event.wordId(), event.userId(), result.getRecordMetadata().partition());
                        }
                    });
        } catch (Exception e) {
            log.error("Unexpected error while sending word-created event to Kafka for wordId={}, userId={}",
                    event.wordId(), event.userId(), e);
        }
    }
}
