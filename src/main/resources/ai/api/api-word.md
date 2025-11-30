# Word API

## Resource: Word

**Table**: `words`  

**Fields**

- `id`
- `userId`
- `originalText`
- `canonicalText`
- `language`
- `createdAt`

**Rules**

- `language` ∈ {`EN`, `PL`}.
- Unique `(userId, canonicalText)`; used as input “words/phrases”.

---

## Endpoints

### [W1] List words

- **Method**: `GET`  
- **Path**: `/api/words`  
- **Description**: Paginated list of user words with “used/free” status.

**Query params**

- `page` (int, default `0`)
- `size` (int, default `20`, max `100`)
- `sort` (string, e.g. `createdAt,desc`)
- `used` (bool, optional; filter free/used)
- `search` (string, optional; substring on `originalText` / `canonicalText`)

**Request body**: none  

**Response JSON (shape)**

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


### [W2] Create word

- **Method**: `POST`  
- **Path**: `/api/words`  
- **Description**: Add new word/phrase.

**Request JSON**

```json
{
  "originalText": "take off",
  "language": "EN"
}
```

**Response JSON**

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


### [W3] Update word

- **Method**: `PUT`  
- **Path**: `/api/words/{id}`  
- **Description**: Update word text/language.  
  If used in accepted cards, those cards are archived and removed from SRS; words become free.

**Request JSON**

```json
{
  "originalText": "take off (plane)",
  "language": "EN"
}
```

**Response JSON**: same shape as [W2].

- **Success**: `200 OK`  
- **Errors**: `400`, `404 Not Found`, `409 Conflict` (canonical duplicate)


### [W4] Delete word

- **Method**: `DELETE`  
- **Path**: `/api/words/{id}`  
- **Description**: Delete word.  
  If used in accepted cards, those cards are archived and removed from SRS; words become free.

**Request body**: none  

**Response body**: empty  

- **Success**: `204 No Content`  
- **Errors**: `404`

---

## Validation

### Word

- `originalText`: required, trimmed, non-empty, length e.g. 1–200.
- `language`: required, enum `{EN, PL}`.
- `canonicalText`: derived on server; uniqueness `(userId, canonicalText)` enforced.
- On duplicate canonical: HTTP `409`.

---

## Business logic

### Changing / deleting words (`PUT` / `DELETE /words/{id}`)

- If the word is used in any **accepted** card:
  - Archive those cards (`status=archived`, `archivedAt=now()`).
  - Remove related `srs_state`.
  - Remove `card_words` entries; affected words become free.
