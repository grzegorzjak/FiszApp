# FiszApp

A modern web application for efficient language learning that leverages AI to automatically generate high-quality flashcards in sentence form (EN→PL). FiszApp combines simple user management, AI-powered content generation, daily review workflows, and the proven SM-2 spaced repetition algorithm to make language learning more effective and engaging.

## Table of Contents

- [Project Description](#project-description)
- [Tech Stack](#tech-stack)
- [Getting Started Locally](#getting-started-locally)
- [Available Scripts](#available-scripts)

## Project Description

FiszApp addresses the time-consuming challenge of creating effective language learning flashcards by automating the process through AI. Instead of single-word flashcards, it generates contextual sentence-based cards that enhance comprehension and retention.

### Key Features

- **AI-Powered Card Generation**: Automatically creates English sentences (4-8 words, ~B1/B2 level) with Polish translations based on user-provided vocabulary
- **Smart Word Management**: Track which words have been used, with each word appearing in exactly one flashcard per user
- **Daily Review System**: Scheduled card generation at 06:00 local time with on-demand generation option
- **SM-2 Spaced Repetition**: Integrated learning algorithm with daily review limits (30 cards)
- **Quality Control**: User review and acceptance workflow with edit/reject capabilities
- **Transparent Word Tracking**: See which words are used in each card, with automatic card invalidation when words are modified
- **Usage Limits**: Cost-effective operation with 2 AI prompts per day, each generating up to 10 cards

### Goals

- Reduce time and barriers to creating high-quality flashcards
- Increase motivation for regular SRS-based learning through AI convenience and simple daily rituals
- Achieve ≥75% AI-generated card acceptance rate
- Ensure ≥75% of new cards come from AI generation

## Tech Stack

### Frontend
- **HTMX**: Modern hypermedia-driven interactions
- **Alpine.js**: Lightweight JavaScript framework for reactive components
- **Tailwind CSS**: Utility-first CSS framework for styling

### Backend
- **Java 21**: Latest LTS version with modern language features
- **Spring Boot 3.5.7**: Application framework with embedded server
- **Spring Data JPA**: Data persistence layer
- **Spring Security**: Authentication and authorization
- **Spring Mail**: Email verification and password reset
- **Lombok**: Code generation for cleaner Java

### Database
- **PostgreSQL**: Relational database hosted on mikr.us

### AI Integration
- **OpenAI API**: Direct REST calls via Spring's RestTemplate

### DevOps
- **Hosting**: mikr.us (free tier)
- **CI/CD**: GitHub Actions → Docker → mikr.us
- **Deployment**: Docker containerization

### Development Tools
- **Gradle**: Build automation and dependency management
- **Flyway**: Database migration management
- **Bean Validation**: Input validation framework

## Getting Started Locally

### Prerequisites

- **Java 21** or higher
- **PostgreSQL** (local installation or Docker)
- **Gradle** (or use included Gradle wrapper)
- **OpenAI API Key** (for card generation)

### Installation Steps

1. **Clone the repository**
   ```bash
   git clone https://github.com/grzegorzjak/FiszApp.git
   cd FiszApp
   ```

2. **Set up PostgreSQL database**
   ```sql
   CREATE DATABASE fiszapp;
   CREATE USER fiszapp_user WITH PASSWORD 'your_password';
   GRANT ALL PRIVILEGES ON DATABASE fiszapp TO fiszapp_user;
   ```
  
3. **Build the project**
   ```bash
   ./gradlew build
   ```

4. **Run the application**
   ```bash
   ./gradlew bootRun --args='--spring.profiles.active=dev'
   ```

5. **Access the application**
   
   Open your browser and navigate to: `http://localhost:8080`

## Available Scripts

### Development

```bash
# Run the application in development mode
./gradlew bootRun --args='--spring.profiles.active=dev'

# Run tests
./gradlew test

# Run integration tests
./gradlew integrationTest

# Clean build artifacts
./gradlew clean

# Build without running tests
./gradlew build -x test
```

### Build and Package

```bash
# Create executable JAR
./gradlew bootJar

# Build Docker image
docker build -t fiszapp:latest .

# Run with Docker
docker run -p 8080:8080 fiszapp:latest
```

### Database Migrations

```bash
# Run Flyway migrations
./gradlew flywayMigrate

# Check migration status
./gradlew flywayInfo

# Clean database (caution: drops all objects)
./gradlew flywayClean
```
