# Development Progress

## Current State

### What Works
- ✅ Basic Spring Boot 3.5.7 application structure
- ✅ Java 21 configured
- ✅ Basic REST controller (HomeController) with "/" endpoint returning welcome message
- ✅ Gradle build system configured

### What's NOT Implemented Yet

#### Infrastructure & Configuration
- [ ] PostgreSQL database setup and configuration
- [ ] Flyway migrations for database schema
- [ ] Application properties (dev, prod profiles)
- [ ] Docker configuration
- [ ] CI/CD pipeline (GitHub Actions)

#### Core Dependencies Missing
- [ ] Spring Data JPA
- [ ] Spring Security
- [ ] Spring Mail (for email verification/reset)
- [ ] JWT authentication library
- [ ] Lombok
- [ ] PostgreSQL driver
- [ ] Flyway
- [ ] OpenAI API client (or RestTemplate configuration)
- [ ] Bean Validation
- [ ] Thymeleaf (for server-side templates)

#### Domain Model (Entities)
- [ ] User entity
- [ ] Word entity (with canonical form)
- [ ] Card entity (flashcard)
- [ ] SRSData entity (SM-2 algorithm state)
- [ ] UserSession/Token management

#### Core Features
- [ ] User registration with email verification
- [ ] Login/logout with JWT
- [ ] Password reset flow
- [ ] Word CRUD operations
- [ ] Card generation service (AI integration)
- [ ] Daily batch generation at 06:00 local time
- [ ] On-demand generation with prompt limits
- [ ] Card acceptance/rejection/editing workflow
- [ ] SM-2 repetition algorithm
- [ ] Daily review limit (30 cards)
- [ ] Word status tracking (free/used)
- [ ] Card invalidation on word changes
- [ ] Statistics and metrics (acceptance rate)

#### Frontend
- [ ] HTMX setup
- [ ] Alpine.js integration
- [ ] Tailwind CSS configuration
- [ ] Templates directory structure
- [ ] Static assets directory

#### Security & Authorization
- [ ] JWT token generation and validation
- [ ] Security configuration
- [ ] Rate limiting for login attempts
- [ ] User-scoped data access enforcement
- [ ] CSRF protection

#### Testing
- [ ] Unit tests
- [ ] Integration tests
- [ ] Test database configuration

## Current Focus
**Initial setup phase** - Project has minimal Spring Boot skeleton only. Next steps involve setting up core infrastructure.

## Next Immediate Steps
1. Add required dependencies to build.gradle
2. Create database schema design
3. Set up PostgreSQL connection
4. Configure Flyway migrations
5. Create domain entities
6. Implement repository layer
7. Set up Spring Security with JWT

## Known Issues
None yet - project in initial setup phase.