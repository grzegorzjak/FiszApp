## Used Technologies
Frontend:  HTMX + Alpine.js + Tailwind CSS
Backend:   Java 21 + Spring Boot 3.5.7
Database:  PostgreSQL (mikr.us managed)
AI:        OpenAI API (bezpośrednie wywołania przez RestTemplate)
Email:     Spring Mail + SMTP (np. Gmail SMTP lub SendGrid free tier)
Hosting:   mikr.us (darmowy tier dla start)
CI/CD:     GitHub Actions → Docker → mikr.us

#### Project Structure
```
FiszApp/
├── .ai/                          # AI-related documentation
│   ├── prd.md                    # Product Requirements Document
│   └── tech-stack.md             # Technical stack overview
├── memory-bank/                  # Cline's Memory Bank
│   ├── projectbrief.md          # Project foundation
│   ├── productContext.md        # Product context
│   ├── systemPatterns.md        # Architecture patterns
│   ├── techContext.md           # Technical details (this file)
│   ├── activeContext.md         # Current work context
│   └── progress.md              # Development progress
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/fiszapp/
│   │   │       ├── FiszAppApplication.java
│   │   │       ├── controller/
│   │   │       ├── service/        # To be created
│   │   │       ├── repository/     # To be created
│   │   │       ├── entity/         # To be created
│   │   │       ├── dto/            # To be created
│   │   │       ├── config/         # To be created
│   │   │       └── security/       # To be created
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── application-dev.properties    # To be created
│   │       ├── application-prod.properties   # To be created
│   │       ├── db/migration/      # Flyway migrations (to be created)
│   │       ├── templates/         # Thymeleaf templates (to be created)
│   │       └── static/            # CSS, JS, images (to be created)
│   └── test/
│       └── java/
│           └── com/example/fiszapp/
├── build.gradle                  # Gradle build configuration
├── settings.gradle               # Gradle settings
├── gradlew                       # Gradle wrapper script (Unix)
├── gradlew.bat                   # Gradle wrapper script (Windows)
├── .gitignore                    # Git ignore rules
├── Dockerfile                    # Docker image definition (to be created)
└── README.md                     # Project documentation (to be created)
```

## Coding & Data patterns
- DTOs as `record`; constructor DI (`@RequiredArgsConstructor`); `@ControllerAdvice` for errors.
- JPA: use projections/entity graphs; optimistic locking where contention appears.
- Transactions only at service layer; keep short.
- User‑scoped queries by `userId` everywhere.

### Key Design Patterns

#### Repository Pattern
- Interface-based data access extending `JpaRepository` or `CrudRepository`
- Spring Data JPA repositories with custom query methods
- Never expose entities in API responses – always map to DTOs
- Use `@Query` for complex queries, projections for multi-joins
- Performance: Use `@EntityGraph` or fetch joins to avoid N+1 problems
- Example: `WordRepository`, `CardRepository`, `SRSDataRepository`


#### Service Layer Pattern
- Business logic encapsulation with constructor injection
- Use `@Transactional` at service layer for state-changing methods
- Use `@Transactional(readOnly = true)` for read-only operations
- Keep transactions as short as possible
- Use Lombok's `@RequiredArgsConstructor` for clean dependency injection
- Use `@Slf4j` for logging instead of System.out.println
- Service interfaces with `@Service` implementations
- Example: `WordService`, `CardGenerationService`, `SRSService`

#### DTO Pattern
- Use DTOs as immutable `record` types (preferred over Lombok's `@Value`)
- Request/Response objects separate from entities
- Bean Validation annotations (`@NotBlank`, `@Size`, `@Email`, etc.)
- Use `@Valid` on `@RequestBody` parameters in controllers
- Example: `CreateWordRequest`, `CardResponse`, `AcceptCardRequest`

###Coding Standards Integration

All implementation must follow these standards:

**Lombok Usage:**
- Use `@RequiredArgsConstructor` for constructor injection
- Use `@Slf4j` for logging
- Prefer Java `record` over Lombok's `@Value` for DTOs
- Avoid `@Data` in non-DTO classes
- Apply Lombok annotations at field level when only some fields need them

**Spring Best Practices:**
- Constructor-based DI (via `@RequiredArgsConstructor`)
- Use configuration parameters instead of hardcoded values
- Controllers handle routing/I/O only, no business logic
- Prefer lambdas and streams over imperative loops
- Use `Optional` to avoid `NullPointerException`

**JPA Best Practices:**
- Use `@Version` for optimistic locking
- Avoid `CascadeType.REMOVE` on large relationships
- Use pagination for large datasets
- Use Specifications for dynamic filtering
- HikariCP connection pooling (Spring Boot default)

**Exception Handling:**
- Custom exceptions for business scenarios
- `@ControllerAdvice` for centralized handling
- Consistent error DTO structure across all endpoints