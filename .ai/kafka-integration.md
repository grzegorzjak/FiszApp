# Kafka Integration

## Overview
FiszApp uses Apache Kafka for asynchronous event processing. Events are published after transaction commits to ensure data consistency.

## Architecture

### Topic Structure
- **word-created**: Published when a new word is created
  - Key: userId (String)
  - Value: WordCreatedEvent (JSON)
  - Partitions: 3
  - Replication Factor: 1

### Event Flow
1. User creates a word via WordService.createWord()
2. Word is saved to database within transaction
3. WordCreatedEvent is published via ApplicationEventPublisher
4. After transaction commits (AFTER_COMMIT), WordEventPublisher sends event to Kafka
5. Event is persisted to Kafka topic with userId as partition key

### Components

**KafkaConfig**
- Defines topic configuration
- Topic auto-creation on application startup

**WordCreatedEvent**
- Immutable record containing: wordId, userId, originalText, canonicalText, language

**WordEventPublisher**
- Listens for WordCreatedEvent using @TransactionalEventListener
- Publishes to Kafka only after transaction commits
- Handles errors gracefully with logging

**WordService**
- Publishes ApplicationEvent after saving word to database
- Event is part of the same transaction context

## Local Development

### Docker Compose Services
- **kafka**: Apache Kafka 3.9.0 in KRaft mode (no Zookeeper)
- **kafka-ui**: Web UI for monitoring topics and messages (port 8085)
- **kafka-init**: Initializes topics on startup

### Configuration
- Local: kafka:9092 (Docker) or localhost:9092
- Producer serialization: String (key), JSON (value)
- No type headers in JSON to simplify cross-service consumption

## Benefits
- Asynchronous processing decouples word creation from downstream actions
- Partition by userId enables parallelism and ordering guarantees per user
- AFTER_COMMIT ensures events are only sent for committed transactions
- Kafka UI provides visibility into message flow for debugging
