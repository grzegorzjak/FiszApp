# .ai/db-plan.md

## 1) Tabele, kolumny, typy i ograniczenia

### `users`
- `id` UUID **PK** `DEFAULT gen_random_uuid()` **NOT NULL**
- `email` TEXT **UNIQUE** **NOT NULL**
- `password_hash` TEXT **NOT NULL**
- `email_verified_at` TIMESTAMP NULL
- `created_at` TIMESTAMP **NOT NULL** `DEFAULT now()`
  
Uwagi: prosta tożsamość użytkownika do skopowania danych domenowych per `user_id`. 

---

### `words`
- `id` UUID **PK** `DEFAULT gen_random_uuid()` **NOT NULL**
- `user_id` UUID **NOT NULL**
- `original_text` TEXT **NOT NULL**
- `canonical_text` TEXT **NOT NULL**
- `language` TEXT **NOT NULL**  — `'EN'` lub `'PL'` (walidowane w aplikacji)
- `created_at` TIMESTAMP **NOT NULL** `DEFAULT now()`
- **UNIQUE** (`user_id`, `id`)
- **UNIQUE** (`user_id`, `canonical_text`) — blokada duplikatów kanonicznych per użytkownik (US-024) 
- **FK** (`user_id`) → `users(id)` **DEFERRABLE INITIALLY IMMEDIATE**

---

### `cards`
- `id` UUID **PK** `DEFAULT gen_random_uuid()` **NOT NULL**
- `user_id` UUID **NOT NULL**
- `status` TEXT **NOT NULL** — dozwolone stany: `draft`, `accepted`, `archived` (egzekwowane w aplikacji, brak `CHECK`) 
- `front_en` TEXT **NOT NULL**
- `back_pl` TEXT **NOT NULL**
- `created_at` TIMESTAMP **NOT NULL** `DEFAULT now()`
- `updated_at` TIMESTAMP **NOT NULL** `DEFAULT now()`
- `accepted_at` TIMESTAMP NULL
- `archived_at` TIMESTAMP NULL
- **UNIQUE** (`user_id`, `id`)
- **FK** (`user_id`) → `users(id)` **DEFERRABLE INITIALLY IMMEDIATE**

Uwagi: treści EN→PL przy zachowaniu reguł jakości i długości (walidowane poza DB). Stany i przejścia obsługiwane w kodzie. 

---

### `card_words`  (łącznik M:N „karta ↔ słowa użyte w karcie”)
- `id` UUID **PK** `DEFAULT gen_random_uuid()` **NOT NULL**
- `user_id` UUID **NOT NULL**
- `card_id` UUID **NOT NULL**
- `word_id` UUID **NOT NULL**
- **UNIQUE** (`user_id`, `word_id`) — każde „słowo” może być użyte w **maks. jednej** fiszce per użytkownik (US-011) 
- **UNIQUE** (`user_id`, `card_id`, `word_id`)
- **FK** (`user_id`, `card_id`) → `cards(user_id, id)` **ON DELETE CASCADE** **DEFERRABLE INITIALLY IMMEDIATE**
- **FK** (`user_id`, `word_id`) → `words(user_id, id)` **ON DELETE RESTRICT** **DEFERRABLE INITIALLY IMMEDIATE**

Uwagi: usunięcie karty zwalnia wszystkie związane słowa (CASCADE). Odrzucenie draftu w aplikacji powinno usunąć powiązania 
i tym samym „oddać” słowa do puli „wolnych”.

---

### `srs_state`  (stan SM-2 per karta)
- `id` UUID **PK** `DEFAULT gen_random_uuid()` **NOT NULL**
- `user_id` UUID **NOT NULL**
- `card_id` UUID **NOT NULL**
- `easiness` NUMERIC(3,2) **NOT NULL**      — np. 1.30–2.50 (zakres kontrolowany w aplikacji)
- `interval_days` INT **NOT NULL**          — ≥0 (kontrola w aplikacji)
- `repetitions` INT **NOT NULL**            — ≥0
- `due_at` TIMESTAMP **NOT NULL**
- `last_grade` SMALLINT NULL                — 0/1/2/3 (Again/Hard/Good/Easy; mapowanie w aplikacji)
- `updated_at` TIMESTAMP **NOT NULL** `DEFAULT now()`
- **UNIQUE** (`user_id`, `card_id`) — jedna szufladka SRS na kartę
- **FK** (`user_id`, `card_id`) → `cards(user_id, id)` **ON DELETE CASCADE** **DEFERRABLE INITIALLY IMMEDIATE**

Uwagi: granice i reguły SM-2, w tym limit 30/dzień i „most overdue first”, są logiką aplikacyjną; schema utrzymuje 
jedynie aktualny stan i termin. 

---

## 2) Relacje między tabelami

- `users (1) —— (∞) words` przez `words.user_id`.   
- `users (1) —— (∞) cards` przez `cards.user_id`.   
- `cards (1) —— (∞) card_words` oraz `words (1) —— (∞) card_words`; łącznik wymusza spójność **per użytkownik** dzięki kompozycyjnym FK (`user_id`, `*_id`).   
- `cards (1) —— (1) srs_state` (technicznie 1:∞ ograniczone unikalnością `user_id, card_id` do 1:1). 

Kardynalności:
- `users` 1→N `words`  
- `users` 1→N `cards`  
- `cards` N→M `words` przez `card_words` (ograniczone do M≤ liczby słów; dodatkowo **word** może należeć do **max 1** 
- karty na użytkownika przez `UNIQUE (user_id, word_id)`).   
- `cards` 1→1 `srs_state`. 

Wszystkie FK są **DEFERRABLE INITIALLY IMMEDIATE**, co upraszcza wsady i kolejność operacji. 

---

## 3) Indeksy

> Minimalny zestaw indeksów wynikający z PK/FK/UNIQUE; kolejne po zebraniu metryk zapytań w produkcji. 

Tworzone implicite/przez ograniczenia:
- **PK** na `id` w każdej tabeli.
- **UNIQUE** (`user_id`, `id`) w `words`, `cards`.
- **UNIQUE** (`user_id`, `canonical_text`) w `words`.
- **UNIQUE** (`user_id`, `word_id`) w `card_words`.
- **UNIQUE** (`user_id`, `card_id`) w `srs_state`.
- Indeksy wspierające FK (na kolumnach referencyjnych) zgodnie z praktyką PostgreSQL.

---

## 4) Zasady PostgreSQL (RLS)

**Założenia wspólne**
- Aplikacja (Spring Boot 3.5.7, Java 21) ustawia GUC: `SET LOCAL app.user_id = :jwt_sub::text` w transakcji po uwierzytelnieniu.  
- RLS aktywne na tabelach domenowych: `users`, `words`, `cards`, `card_words`, `srs_state`.  
- Polityki oparte na prostym warunku `user_id = current_setting('app.user_id')::uuid`. 

**Przykładowa konfiguracja (skrót):**
- `ALTER TABLE users ENABLE ROW LEVEL SECURITY;`
  - Policy: `USING (id = current_setting('app.user_id')::uuid)`
  - `WITH CHECK (id = current_setting('app.user_id')::uuid)`
- `ALTER TABLE words ENABLE ROW LEVEL SECURITY;`
  - Policy: `USING (user_id = current_setting('app.user_id')::uuid)`
  - `WITH CHECK (user_id = current_setting('app.user_id')::uuid)`
- Analogiczne polityki dla `cards`, `card_words`, `srs_state`.

Uwagi: prosta separacja tenantowa (per użytkownik), zgodna z decyzjami z sesji. 

---

## 5) Dodatkowe uwagi projektowe

- **Klucze obce kompozycyjne** (`user_id`, `*_id`) zapobiegają „przekrosowym” wiązaniom między danymi różnych użytkowników i wymuszają spójność najemcy. 
- **Brak `CHECK`/`ENUM` dla `cards.status`** — katalog wartości `draft|accepted|archived` jest twardo egzekwowany w warstwie aplikacji zgodnie z ustaleniem MVP. 
- **Kasowanie kaskadowe**: `cards → card_words` oraz `cards → srs_state` to **ON DELETE CASCADE**; inne połączenia restrykcyjne. 
- **Czas**: `TIMESTAMP` bez strefy; przyjmujemy UTC i konwersje po stronie aplikacji. 
- **Statystyki/KPI** (acceptance_rate, dzienne metryki) — w MVP liczone zapytaniami/warstwą aplikacyjną; odłożenie dedykowanych tabel na później. 

---
