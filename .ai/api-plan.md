# REST API Plan

## 1. Resources

**Core DB-backed resources**

1. **User** (`users`)  
   - Fields: `id`, `email`, `passwordHash`, `emailVerifiedAt`, `createdAt`  
   - API use: current profile, account deletion.

2. **Word** (`words`)  
   - Fields: `id`, `userId`, `originalText`, `canonicalText`, `language`, `createdAt`  
   - Rules: `language` ∈ {`EN`, `PL`}, unique `(userId, canonicalText)`; used as input “words/phrases”.

3. **Card** (`cards`)  
   - Fields: `id`, `userId`, `status` (`draft`, `accepted`, `archived`), `frontEn`, `backPl`, `createdAt`, `acceptedAt`, `archivedAt`  
   - Rules: flashcards EN→PL; status drives SRS and generation logic.

4. **CardWord** (`card_words`)  
   - Fields: `id`, `userId`, `cardId`, `wordId`  
   - Rules: unique `(userId, wordId)` → each word can belong to at most one card at a time.

5. **SrsState** (`srs_state`)  
   - Fields: `id`, `userId`, `cardId`, `easiness`, `intervalDays`, `repetitions`, `dueAt`, `lastGrade`  
   - Rules: one SRS state per accepted card; implements SM-2 schedule.

**Logical resources (no separate tables)**

6. **GenerationBatch**  
   - “Result of one AI prompt”: up to 10 new draft cards, consumes prompt quota.

7. **SrsReview**  
   - One user review action (grade) for a card on a given day.

8. **Stats**  
   - Aggregated per-day data (cards generated/accepted/rejected, prompts used, reviews done, acceptance rate).

---

## 2. Endpoints

Base prefix: `/api`.  
All responses are JSON; timestamps in ISO-8601.

### 2.1 Word endpoints

#### [W1] List words

- **Method**: `GET`  
- **Path**: `/api/words`  
- **Description**: Paginated list of user words with “used/free” status.

- **Query params**  
  - `page` (int, default 0)  
  - `size` (int, default 20, max 100)  
  - `sort` (string, e.g. `createdAt,desc`)  
  - `used` (bool, optional; filter free/used)  
  - `search` (string, optional; substring on `originalText` / `canonicalText`)

- **Request body**: none  

- **Response JSON (shape)**  

  ```json
  {
    "content": [
      {
        "id": "uuid",
        "originalText": "take off",
        "canonicalText": "take off",
        "language": "EN",
        "used": false,
        "createdAt": "..."
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 42,
    "totalPages": 3
  }
  ```

- **Success**: `200 OK`  
- **Errors**: `400 Bad Request`, `401 Unauthorized`, `500 Internal Server Error`


#### [W2] Create word

- **Method**: `POST`  
- **Path**: `/api/words`  
- **Description**: Add new word/phrase.

- **Request JSON**

  ```json
  {
    "originalText": "take off",
    "language": "EN"
  }
  ```

- **Response JSON**

  ```json
  {
    "id": "uuid",
    "originalText": "take off",
    "canonicalText": "take off",
    "language": "EN",
    "used": false,
    "createdAt": "..."
  }
  ```

- **Success**: `201 Created`  
- **Errors**:  
  - `400` – invalid text or language  
  - `409 Conflict` – canonical duplicate for this user  


#### [W3] Update word

- **Method**: `PUT`  
- **Path**: `/api/words/{id}`  
- **Description**: Update word text/language. If used in accepted cards, those cards are archived and removed from SRS; words become free.

- **Request JSON**

  ```json
  {
    "originalText": "take off (plane)",
    "language": "EN"
  }
  ```

- **Response JSON**: same shape as [W2].

- **Success**: `200 OK`  
- **Errors**: `400`, `404 Not Found`, `409 Conflict` (canonical duplicate)


#### [W4] Delete word

- **Method**: `DELETE`  
- **Path**: `/api/words/{id}`  
- **Description**: Delete word. If used in accepted cards, those cards are archived and removed from SRS; words become free.

- **Request body**: none  
- **Response body**: empty  
- **Success**: `204 No Content`  
- **Errors**: `404`


---

### 2.2 Card endpoints

#### [C1] List cards

- **Method**: `GET`  
- **Path**: `/api/cards`  
- **Description**: Paginated list of user cards.

- **Query params**  
  - `status` (optional: `draft`, `accepted`, `archived`)  
  - `page`, `size`, `sort` (e.g. `createdAt,desc`)

- **Response JSON (shape)**

  ```json
  {
    "content": [
      {
        "id": "uuid",
        "status": "draft",
        "frontEn": "She turned off the light.",
        "backPl": "Wyłączyła światło.",
        "usedWordIds": ["word-1", "word-2"],
        "createdAt": "...",
        "acceptedAt": null,
        "archivedAt": null
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 10,
    "totalPages": 1
  }
  ```

- **Success**: `200`  
- **Errors**: `401`, `500`


#### [C2] Get card

- **Method**: `GET`  
- **Path**: `/api/cards/{id}`  
- **Description**: Card details, including used words.

- **Response JSON (shape)**

  ```json
  {
    "id": "uuid",
    "status": "accepted",
    "frontEn": "She turned off the light.",
    "backPl": "Wyłączyła światło.",
    "usedWords": [
      { "id": "word-1", "originalText": "turn off", "language": "EN" }
    ],
    "createdAt": "...",
    "acceptedAt": "...",
    "archivedAt": null
  }
  ```

- **Success**: `200`  
- **Errors**: `404`


#### [C3] Update / accept / reject draft card

- **Method**: `PATCH`  
- **Path**: `/api/cards/{id}`  
- **Description**:  
  - Edit draft content.  
  - Accept draft (`draft → accepted`).  
  - Reject draft (`draft → archived`).

- **Request JSON (examples)**

  - **Edit content only**

    ```json
    {
      "frontEn": "She turned off the light.",
      "backPl": "Wyłączyła światło."
    }
    ```

  - **Accept**

    ```json
    {
      "status": "accepted"
    }
    ```

  - **Reject**

    ```json
    {
      "status": "archived"
    }
    ```

- **Response JSON**: updated card (shape as [C2]).

- **Success**: `200 OK`  
- **Errors**:  
  - `400` – invalid transition, invalid sentence rules, <2 used words  
  - `404` – not found or not draft  
  - `409` – concurrent modification


#### [C4] Archive accepted card (optional)

- **Method**: `POST`  
- **Path**: `/api/cards/{id}/archive`  
- **Description**: Manually archive an accepted card (stop reviewing it).

- **Request body**: none  
- **Response**: card DTO or empty body  
- **Success**: `200 OK`  
- **Errors**: `400`, `404`


---

### 2.3 Card generation (batches)

#### [G1] Trigger on-demand generation

- **Method**: `POST`  
- **Path**: `/api/generation-batches`  
- **Description**: Use free words to generate up to 10 new draft cards via AI. Enforces: ≥2 free words, max 2 prompts/day, max 10 cards per prompt.

- **Request JSON**

  ```json
  {
    "maxCards": 10
  }
  ```

  (`maxCards` optional, default 10, max 10)

- **Response JSON (shape)**

  ```json
  {
    "batchId": "uuid",
    "createdAt": "...",
    "requestedCards": 10,
    "createdCards": 7,
    "remainingPromptQuota": 1,
    "cards": [
      {
        "id": "card-id",
        "frontEn": "...",
        "backPl": "...",
        "usedWordIds": ["word-1", "word-2"]
      }
    ]
  }
  ```

- **Success**: `201 Created`  
- **Errors**:  
  - `400` – not enough free words or invalid `maxCards`  
  - `401`  
  - `429 Too Many Requests` – daily prompt limit reached  
  - `500`


#### [G2] List past generation batches (optional)

- **Method**: `GET`  
- **Path**: `/api/generation-batches`  
- **Description**: Basic history for debugging and stats.

- **Query params**: `page`, `size`  
- **Response**: paged list of batches with `createdCards`, `requestedCards`.  


---

### 2.4 SRS review endpoints

#### [R1] Get due cards for review

- **Method**: `GET`  
- **Path**: `/api/srs/reviews`  
- **Description**: Cards due today, sorted by `dueAt`, limited to at most 30 per day.

- **Query params**  
  - `limit` (int, optional, default 30, max 30)

- **Response JSON (shape)**

  ```json
  {
    "cards": [
      {
        "cardId": "card-uuid",
        "frontEn": "She turned off the light.",
        "backPl": "Wyłączyła światło.",
        "dueAt": "...",
        "intervalDays": 3,
        "repetitions": 2,
        "lastGrade": 3
      }
    ],
    "remainingDailyLimit": 18
  }
  ```

- **Success**: `200 OK`  
- **Errors**: `401`


#### [R2] Submit review grade

- **Method**: `POST`  
- **Path**: `/api/srs/reviews`  
- **Description**: Submit grade (`AGAIN`, `HARD`, `GOOD`, `EASY`) for one card and update SRS state.

- **Request JSON**

  ```json
  {
    "cardId": "card-uuid",
    "grade": "GOOD"
  }
  ```

- **Response JSON**

  ```json
  {
    "cardId": "card-uuid",
    "nextDueAt": "...",
    "intervalDays": 4,
    "repetitions": 3,
    "remainingDailyLimit": 17
  }
  ```

- **Success**: `200 OK`  
- **Errors**:  
  - `400` – invalid grade or card not in reviewable state  
  - `404` – card not found for user  
  - `429` – daily review limit reached  


---

### 2.5 Stats endpoints

#### [S1] Daily stats

- **Method**: `GET`  
- **Path**: `/api/stats/daily`  
- **Description**: Per-day metrics for current user (cards, prompts, reviews, acceptance rate).

- **Query params**  
  - `date` (ISO date, optional, default today)  
  - or `from`, `to` (ISO dates) to return an array of days

- **Response JSON (single day)**

  ```json
  {
    "date": "2025-01-01",
    "cardsGenerated": 10,
    "cardsAccepted": 8,
    "cardsRejected": 2,
    "acceptanceRate": 0.8,
    "promptsUsed": 1,
    "promptsLimit": 2,
    "reviewsDone": 25,
    "reviewsLimit": 30
  }
  ```

- **Success**: `200 OK`  
- **Errors**: `400`, `401`


---

### 2.6 User/account endpoints (no auth endpoints)

#### [U1] Get current profile

- **Method**: `GET`  
- **Path**: `/api/me`  
- **Description**: Basic info about current user.

- **Response JSON**

  ```json
  {
    "id": "uuid",
    "email": "user@example.com",
    "createdAt": "...",
    "timezone": "Europe/Warsaw"
  }
  ```

- **Success**: `200 OK`  
- **Errors**: `401`


#### [U2] Delete account

- **Method**: `DELETE`  
- **Path**: `/api/me`  
- **Description**: Request full account deletion and data removal.

- **Request body**: none  
- **Response**: empty or a small status object  

- **Success**: `202 Accepted` (deletion may be processed asynchronously)  
- **Errors**: `401`


---

## 4. Validation and business logic

### 4.1 Per-resource validation

1. **Word**  
   - `originalText`: required, trimmed, non-empty, length e.g. 1–200.  
   - `language`: required, enum `{EN, PL}`.  
   - `canonicalText`: derived on server; uniqueness `(userId, canonicalText)` enforced.  
   - On duplicate canonical: HTTP `409`.

2. **Card**  
   - `status`: enum `{draft, accepted, archived}`; transitions:  
     - `draft → accepted` (accept)  
     - `draft → archived` (reject)  
     - `accepted → archived` (manual/archive/word change)  
   - `frontEn`: EN sentence; 4–8 words; correct grammar; no weird idioms (validated by AI + heuristics).  
   - `backPl`: correct PL translation using most common meaning.  
   - `usedWordIds` (when creating/updating):  
     - Must contain at least 2 ids.  
     - All words must belong to current user and be currently “free”.  
     - DB unique `(userId, wordId)` in `card_words` guarantees a word is used by at most one card; conflicts → `409`.

3. **SrsState**  
   - Values respected by SM-2 algorithm:  
     - `easiness` within configured range (e.g. 1.3–2.5).  
     - `intervalDays` ≥ 0, `repetitions` ≥ 0.  
     - `lastGrade` mapped from grade enum (`AGAIN`, `HARD`, `GOOD`, `EASY`).

4. **Stats**  
   - Dates validated (`from <= to`, max range, etc.).  


### 4.2 Core business flows

1. **On-demand card generation (`/generation-batches`)**  
   - Check number of free words (no `card_words` entry). If `< 2` → `400` (`NOT_ENOUGH_FREE_WORDS`).  
   - Check daily prompt count for user. If ≥ 2 → `429`.  
   - Build groups of free words (≥2 per candidate card) and call AI client.  
   - Validate each returned pair (sentence rules) and discard invalid ones.  
   - Create `cards` with `status = draft` and related `card_words`.  
   - Mark words as “used” implicitly (by existing `card_words` row).  
   - Increment prompt usage for stats.

2. **Reviewing drafts (`PATCH /cards/{id}`)**  
   - On **accept**:  
     - Validate `frontEn`, `backPl`, `usedWordIds` size ≥ 2.  
     - Set `status=accepted`, `acceptedAt=now()`.  
     - Create initial `srs_state` for card.  
   - On **reject**:  
     - Set `status=archived`, `archivedAt=now()`.  
     - Delete `card_words` entries; words become free.  
   - On **edit only**:  
     - Only text changes allowed while keeping the same words (no change to `usedWordIds`).

3. **Changing / deleting words (`PUT` / `DELETE /words/{id}`)**  
   - If the word is used in any **accepted** card:  
     - Archive those cards (`status=archived`, `archivedAt=now()`).  
     - Remove related `srs_state`.  
     - Remove `card_words` entries; affected words become free.  

4. **SRS reviews**  
   - `GET /srs/reviews`:  
     - Return due cards (`dueAt <= today`) sorted by `dueAt`, up to daily limit (30).  
   - `POST /srs/reviews`:  
     - Validate grade; update `easiness`, `intervalDays`, `repetitions`, `dueAt`.  
     - Increment daily review counter; if limit exceeded before update → `429`.

5. **Stats computation (`/stats/daily`)**  
   - For each day (on read or via background pre-aggregation) compute:  
     - `cardsGenerated` – number of cards created via generation.  
     - `cardsAccepted`, `cardsRejected` – based on status transitions.  
     - `acceptanceRate` – accepted / generated (per day).  
     - `promptsUsed` – generation batches count.  
     - `reviewsDone` – number of SRS review events.  

6. **Account deletion (`DELETE /me`)**  
   - Mark user for deletion or delete synchronously:  
     - Remove user row.  
     - Cascade deletes (words, cards, card_words, srs_state, stats/logs where applicable).  
   - Return `202 Accepted` so backend can execute heavy cleanup asynchronously if needed.
