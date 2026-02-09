# Plan warstwy security dla FiszApp (wymagania dla implementacji)

## 0. Założenia ogólne

- Spring Boot 3.x, Spring Security, JPA/Hibernate.
- `spring.jpa.hibernate.ddl-auto=update` – wszystkie tabele powstają z encji JPA.
- Autoryzacja: JWT (HS256, sekret z konfiguracji/zmiennych środowiskowych).
- JWT przechowywany w HttpOnly cookie (np. `AUTH_TOKEN`), ścieżka `/`, w produkcji `Secure`, `SameSite=Lax`.
- Czas życia JWT: ok. 24h.
- Aplikacja stateless – brak sesji serwerowych, stan logowania oparty na JWT.
- Jedna główna rola na MVP: `ROLE_USER`, z możliwością rozszerzenia w przyszłości.

---

## 1. Encja użytkownika (`User`) – wymagania

### 1.1. Zakres encji

Encja `User` (tabela `users`) musi wspierać:

- logowanie emailem,
- weryfikację adresu e-mail,
- soft delete konta (RODO),
- przechowywanie roli,
- śledzenie ostatniego logowania.

### 1.2. Pola wymagane

Wymagane pola (nazwy możesz dopasować do swojego stylu):

1. Identyfikator
   - UUID jako klucz główny, generowany automatycznie.

2. E-mail
   - tekst, walidacja e-mail, `NOT NULL`, `UNIQUE`.
   - wykorzystywany jako login.

3. Hasło
   - pole na zahaszowane hasło (np. `passwordHash`).
   - logika: zawsze hashowanie np. BCrypt po stronie serwisu, nigdy wprost.

4. Rola
   - tekst (np. `role`), wartość domyślna `ROLE_USER`.
   - na MVP jedna rola, ale możliwość rozszerzenia na wiele ról w przyszłości.

5. Weryfikacja e-maila
   - pole typu `Instant` oznaczające moment weryfikacji (`emailVerifiedAt`).
   - `null` = użytkownik nieweryfikowany, nie powinien się logować.

6. Daty tworzenia/aktualizacji
   - `createdAt` – ustawiane przy stworzeniu, nieedytowalne.
   - `updatedAt` – aktualizowane przy każdej zmianie encji.

7. Soft delete
   - `deletedAt` typu `Instant` – ustawiane przy usuwaniu konta (logiczne usunięcie).
   - użytkownik z ustawionym `deletedAt` nie powinien móc się logować ani być używany jako aktywny.

8. Ostatnie logowanie
   - `lastLoginAt` typu `Instant` – uzupełniane po poprawnym logowaniu.

### 1.3. Logika pomocnicza

Na poziomie modelu/domeny przydatne są metody:

- `isEmailVerified()` – `true`, jeśli pole weryfikacji nie jest `null`.
- `isDeleted()` – `true`, jeśli `deletedAt` nie jest `null`.

Repozytorium użytkownika powinno mieć metodę wyszukującą po e-mailu tylko użytkowników nieusuniętych (warunek `deletedAt IS NULL`).

---

## 2. Encje tokenów: weryfikacja e-maila i reset hasła

Celem jest posiadanie dwóch encji JPA tworzących odpowiednie tabele:

- `EmailVerificationToken` (tabela `email_verification_tokens`),
- `PasswordResetToken` (tabela `password_reset_tokens`).

### 2.1. Założenia wspólne

Dla obu encji:

- Identyfikator UUID jako klucz główny.
- Pole tekstowe `token` – unikalny identyfikator tokenu (np. UUID w postaci `String`).
- Relacja wiele-do-jednego z `User` (pole `user`):
  - nie może być `null`.
- Pole `expiresAt` (typ czasu, np. `Instant`) – moment wygaśnięcia tokenu.
- Pole `usedAt` (typ czasu, może być `null`) – moment wykorzystania tokenu.
- Pole `createdAt` – automatyczny timestamp stworzenia rekordu.
- Token jest **jednorazowy**:
  - użycie tokenu ustawia `usedAt`,
  - logika w serwisach musi sprawdzać trzy warunki:
    - token istnieje,
    - jest nieprzeterminowany,
    - nie jest oznaczony jako użyty.

### 2.2. `EmailVerificationToken`

Wymagania szczegółowe:

- Tabela: `email_verification_tokens`.
- Token generowany np. przy rejestracji nowego użytkownika.
- `expiresAt` – domyślnie ok. 24h od czasu wygenerowania.
- Wykorzystywany przy kliknięciu w link aktywacyjny w mailu.

### 2.3. `PasswordResetToken`

Wymagania szczegółowe:

- Tabela: `password_reset_tokens`.
- Token generowany przy zgłoszeniu resetu hasła.
- `expiresAt` – krótszy TTL, np. 1h.
- Używany przy ustawianiu nowego hasła użytkownika.

---

## 3. Integracja z Spring Security – UserDetails, UserDetailsService, bieżący użytkownik

### 3.1. Implementacja `UserDetails`

Wymagania:

- Utworzyć klasę implementującą `UserDetails`, która opakowuje encję `User`.
- Powołać się na następujące zasady:
  - `getUsername()` zwraca e-mail użytkownika.
  - `getPassword()` zwraca zahaszowane hasło.
  - `getAuthorities()` zwraca kolekcję z co najmniej jedną rolą odpowiadającą polu `role` encji `User`.
- Stan konta:
  - konto jest „włączone” (`isEnabled()`), gdy:
    - e-mail jest zweryfikowany (pole weryfikacji nie jest `null`),
    - użytkownik nie jest oznaczony jako usunięty.
  - pozostałe flagi (`isAccountNonExpired`, `isAccountNonLocked`, `isCredentialsNonExpired`) mogą na MVP być zawsze `true` albo powiązane z tymi samymi warunkami (brak dodatkowych statusów).

### 3.2. `UserDetailsService`

Wymagania:

- Utworzyć serwis implementujący `UserDetailsService`.
- Logika musi:
  - wyszukiwać użytkownika po e-mailu w repozytorium z warunkiem, że konto nie jest usunięte,
  - w razie niepowodzenia rzucać `UsernameNotFoundException`,
  - mapować znalezioną encję `User` na obiekt `UserDetails` opisany wyżej.

### 3.3. Dostęp do zalogowanego użytkownika w kodzie aplikacji

Wymagania:

- Zdefiniować prosty komponent/dostawcę aktualnego użytkownika (np. `CurrentUserProvider`):
  - metoda zwracająca aktualne `UserDetails` z `SecurityContext`,
  - metoda zwracająca encję `User` (np. poprzez id/e-mail z `UserDetails` i repozytorium użytkownika).
- Przyjęty wzorzec:
  - z `SecurityContextHolder` pobierane jest `Authentication`,
  - z obiektu `principal` aplikacja uzyskuje `UserDetails`,
  - na tej podstawie ładowany jest pełny `User` z bazy, gdy potrzebne są jego pola domenowe.
- W warstwie serwisów biznesowych:
  - preferowane jest użycie `CurrentUserProvider` do identyfikacji aktualnie zalogowanego użytkownika,
  - zapewnia to spójność i ogranicza potrzebę przekazywania wrażliwych danych z frontendu.

---

## 4. JWT – konfiguracja i serwis

### 4.1. Konfiguracja

Wymagania konfiguracyjne:

- Wprowadzić w konfiguracji aplikacji parametry:
  - sekret JWT (np. `app.jwt.secret`),
  - czas życia tokenu (np. `app.jwt.expiration-seconds=86400`).
- Sekret nie powinien być commitowany w repozytorium – pobierany z zewnątrz (np. zmienne środowiskowe).

### 4.2. Serwis JWT

Wymagania funkcjonalne:

- Metoda generowania tokenu:
  - przyjmuje `UserDetails` lub e-mail użytkownika,
  - ustawia `subject` na e-mail,
  - dodaje datę wystawienia i datę wygaśnięcia,
  - podpisuje token algorytmem HS256 z użyciem sekreta.
- Metoda odczytu e-maila (subject) z tokenu.
- Metoda weryfikacji tokenu:
  - sprawdza poprawność podpisu i datę ważności,
  - opcjonalnie porównuje subject z danymi użytkownika z bazy.

---

## 5. Filtr JWT i konfiguracja Spring Security

### 5.1. Filtr JWT

Wymagania:

- Filtr na bazie `OncePerRequestFilter`.
- Dla każdego żądania:
  - odczytanie JWT z HttpOnly cookie o skonfigurowanej nazwie (np. `AUTH_TOKEN`),
  - jeśli token istnieje:
    - ekstrakcja e-maila z tokenu,
    - załadowanie użytkownika przez `UserDetailsService`,
    - walidacja tokenu,
    - zbudowanie i ustawienie obiektu `Authentication` w `SecurityContext`, jeśli token jest ważny.
- Filtr powinien być dodany w łańcuchu przed domyślnym filtrem logowania.

### 5.2. SecurityFilterChain

Wymagania dla konfiguracji:

- Stateless: włączyć `SessionCreationPolicy.STATELESS`.
- Zarejestrować `UserDetailsService` oraz `PasswordEncoder` (BCrypt).
- Dodać filtr JWT do łańcucha filtrów.
- Skonfigurować reguły autoryzacji:
  - endpointy publiczne:
    - rejestracja: `/auth/register`,
    - logowanie: `/auth/login`,
    - weryfikacja e-maila: `/auth/verify/**`,
    - reset hasła: `/auth/password/**`,
    - zasoby statyczne (`/css/**`, `/js/**`, `/img/**`, itp.),
    - ewentualnie strony błędów.
  - pozostałe endpointy wymagają uwierzytelnienia.
- CSRF:
  - jeśli UI korzysta z formularzy Thymeleaf/HTMX, pozostawić CSRF włączone,
  - zapewnić przekazywanie tokena CSRF w formularzach i żądaniach HTMX,
  - ewentualnie wyłączyć CSRF selektywnie dla wybranych endpointów API (np. `/auth/login`).

---

## 6. Przepływy biznesowe – rejestracja, logowanie, reset hasła, usuwanie konta

### 6.1. Rejestracja

- Endpoint rejestracyjny:
  - sprawdza unikalność e-maila,
  - tworzy nowego użytkownika (rola `ROLE_USER`, hasło zahaszowane, brak weryfikacji e-maila),
  - zapisuje encję `User`,
  - generuje `EmailVerificationToken` z odpowiednim TTL,
  - wysyła e-mail z linkiem aktywacyjnym zawierającym token.

### 6.2. Weryfikacja e-maila

- Endpoint weryfikacyjny:
  - przyjmuje token,
  - wyszukuje odpowiadający rekord `EmailVerificationToken`,
  - sprawdza, czy token jest ważny i nieużyty,
  - ustawia w `User` pole weryfikacji e-maila,
  - oznacza token jako użyty,
  - zapisuje zmiany i zwraca odpowiedni komunikat/redirect.

### 6.3. Logowanie

- Endpoint logowania:
  - przyjmuje e-mail i hasło,
  - stosuje rate-limit (patrz sekcja 7),
  - weryfikuje użytkownika i hasło przy użyciu `PasswordEncoder`,
  - sprawdza, czy użytkownik jest zweryfikowany i nieusunięty,
  - przy sukcesie generuje JWT,
  - ustawia HttpOnly cookie z tokenem,
  - aktualizuje pole `lastLoginAt` użytkownika.

### 6.4. Wylogowanie

- Endpoint wylogowania:
  - ustawia cookie JWT z wygasłym czasem żywotności,
  - po stronie klienta usuwa stan logowania (np. poprzez nadpisanie cookie).

### 6.5. Reset hasła

- `reset-request`:
  - przyjmuje e-mail,
  - jeśli użytkownik istnieje i nie jest usunięty, tworzy `PasswordResetToken` z krótkim TTL,
  - wysyła e-mail z linkiem resetu,
  - odpowiedź zawsze neutralna (nie ujawnia, czy e-mail istnieje).
- właściwy reset:
  - przyjmuje token i nowe hasło,
  - waliduje token,
  - zmienia użytkownikowi hasło (z hashowaniem),
  - oznacza token jako użyty.

### 6.6. Usuwanie konta (soft delete)

- Endpoint dostępny dla zalogowanego użytkownika.
- Działanie:
  - odczyt aktualnego użytkownika z kontekstu bezpieczeństwa (przez komponent dostępu do bieżącego użytkownika),
  - ustawienie `deletedAt` na bieżący czas,
  - zapis zmian,
  - unieważnienie cookie z JWT po stronie klienta.
