package com.example.fiszapp.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    public static final String WORD_CREATED_TOPIC = "word-created";

    @Bean
    public NewTopic wordCreatedTopic() {
        return TopicBuilder.name(WORD_CREATED_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
