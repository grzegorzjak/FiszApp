# Development Progress

## Current State

### What Works
- ✅ Basic Spring Boot 3.5.7 application structure
- ✅ Java 21 configured
- ✅ Basic REST controller (HomeController) with "/" endpoint returning welcome message
- ✅ Gradle build system configured
- ✅ Spring Data JPA dependency added
- ✅ PostgreSQL driver added
- ✅ Lombok dependency added
- ✅ Bean Validation dependency added
- ✅ PostgreSQL database running in Docker (fiszapp-postgres container)
- ✅ Database connection configured (localhost:5432/fiszapp)
- ✅ JPA entities created (User, Word, Card, CardWord, SrsState)

### What's NOT Implemented Yet

#### Infrastructure & Configuration
- [ ] Application properties profiles (dev, prod)
- [ ] CI/CD pipeline (GitHub Actions)

#### Core Dependencies Missing
- [ ] Spring Security
- [ ] Spring Mail (for email verification/reset)
- [ ] JWT authentication library
- [ ] OpenRouter (or RestTemplate configuration)
- [ ] Thymeleaf (for server-side templates)

#### Domain Model (Entities)
- [x] User entity
- [x] Word entity (with canonical form)
- [x] Card entity (flashcard)
- [x] SrsState entity (SM-2 algorithm state)
- [x] CardWord entity (M:N relationship)
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
**Database layer setup complete** - JPA entities created and PostgreSQL connected. Next: implement repositories and basic services.

## Next Immediate Steps
1. Create repository interfaces (UserRepository, WordRepository, CardRepository, etc.)
2. Implement basic service layer
3. Set up Spring Security with JWT
4. Create basic REST endpoints for CRUD operations
5. Add email functionality (Spring Mail)

## Known Issues
None yet - project in initial setup phase.
