# Card and generation API

## Resources

### Card

**Table**: `cards`  

**Fields**

- `id`
- `userId`
- `status` (`draft`, `accepted`, `archived`)
- `frontEn`
- `backPl`
- `createdAt`
- `acceptedAt`
- `archivedAt`

**Rules**

- Flashcards EN→PL.
- `status` drives SRS and generation logic.

---

### CardWord

**Table**: `card_words`  

**Fields**

- `id`
- `userId`
- `cardId`
- `wordId`

**Rules**

- Unique `(userId, wordId)` → each word can belong to at most one card at a time.

---

### GenerationBatch

**Logical resource (no separate table)**  

- “Result of one AI prompt”: up to 10 new draft cards, consumes prompt quota.

---

## Card endpoints

### [C1] List cards

- **Method**: `GET`  
- **Path**: `/api/cards`  
- **Description**: Paginated list of user cards.

**Query params**

- `status` (optional: `draft`, `accepted`, `archived`)
- `page`, `size`, `sort` (e.g. `createdAt,desc`)

**Response JSON (shape)**

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


### [C2] Get card

- **Method**: `GET`  
- **Path**: `/api/cards/{id}`  
- **Description**: Card details, including used words.

**Response JSON (shape)**

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


### [C3] Update / accept / reject draft card

- **Method**: `PATCH`  
- **Path**: `/api/cards/{id}`  
- **Description**:
  - Edit draft content.
  - Accept draft (`draft → accepted`).
  - Reject draft (`draft → archived`).

**Request JSON (examples)**

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

**Response JSON**: updated card (shape as [C2]).

- **Success**: `200 OK`  
- **Errors**:
  - `400` – invalid transition, invalid sentence rules, <2 used words
  - `404` – not found or not draft
  - `409` – concurrent modification


### [C4] Archive accepted card (optional)

- **Method**: `POST`  
- **Path**: `/api/cards/{id}/archive`  
- **Description**: Manually archive an accepted card (stop reviewing it).

**Request body**: none  

**Response**: card DTO or empty body  

- **Success**: `200 OK`  
- **Errors**: `400`, `404`

---

## Card generation endpoints

### [G1] Trigger on-demand generation

- **Method**: `POST`  
- **Path**: `/api/generation-batches`  
- **Description**: Use free words to generate up to 10 new draft cards via AI.  
  Enforces: ≥2 free words, max 2 prompts/day, max 10 cards per prompt.

**Request JSON**

```json
{
  "maxCards": 10
}
```

(`maxCards` optional, default `10`, max `10`)

**Response JSON (shape)**

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
  - `500`


### [G2] List past generation batches (optional)

- **Method**: `GET`  
- **Path**: `/api/generation-batches`  
- **Description**: Basic history for debugging and stats.

**Query params**: `page`, `size`  

**Response**: paged list of batches with `createdCards`, `requestedCards`.

---

## Validation

### Card

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

---

## Business logic

### On-demand card generation (`/generation-batches`)

- Check number of free words (no `card_words` entry). If `< 2` → `400` (`NOT_ENOUGH_FREE_WORDS`).
- Check daily prompt count for user. If ≥ 2 → `429`.
- Build groups of free words (≥2 per candidate card) and call AI client.
- Validate each returned pair (sentence rules) and discard invalid ones.
- Create `cards` with `status = draft` and related `card_words`.
- Mark words as “used” implicitly (by existing `card_words` row).
- Increment prompt usage for stats.

### Reviewing drafts (`PATCH /cards/{id}`)

- On **accept**:
  - Validate `frontEn`, `backPl`, `usedWordIds` size ≥ 2.
  - Set `status=accepted`, `acceptedAt=now()`.
  - Create initial `srs_state` for card.
- On **reject**:
  - Set `status=archived`, `archivedAt=now()`.
  - Delete `card_words` entries; words become free.
- On **edit only**:
  - Only text changes allowed while keeping the same words (no change to `usedWordIds`).

> For the effects of changing/deleting words on cards and SRS, see `api-word.md`.
