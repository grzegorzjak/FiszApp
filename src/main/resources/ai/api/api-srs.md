# SRS review API

## Resources

### SrsState

**Table**: `srs_state`  

**Fields**

- `id`
- `userId`
- `cardId`
- `easiness`
- `intervalDays`
- `repetitions`
- `dueAt`
- `lastGrade`

**Rules**

- One SRS state per accepted card; implements SM-2 schedule.

---

### SrsReview

**Logical resource (no separate table)**  

- One user review action (grade) for a card on a given day.

---

## Endpoints

### [R1] Get due cards for review

- **Method**: `GET`  
- **Path**: `/api/srs/reviews`  
- **Description**: Cards due today, sorted by `dueAt`.

**Response JSON (shape)**

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
  ]
}
```

- **Success**: `200 OK`  
- **Errors**: `401`


### [R2] Submit review grade

- **Method**: `POST`  
- **Path**: `/api/srs/reviews`  
- **Description**: Submit grade (`AGAIN`, `HARD`, `GOOD`, `EASY`) for one card and update SRS state.

**Request JSON**

```json
{
  "cardId": "card-uuid",
  "grade": "GOOD"
}
```

**Response JSON**

```json
{
  "cardId": "card-uuid",
  "nextDueAt": "...",
  "intervalDays": 4,
  "repetitions": 3
}
```

- **Success**: `200 OK`  
- **Errors**:
  - `400` – invalid grade or card not in reviewable state
  - `404` – card not found for user

---

## Validation

### SrsState

- Values respected by SM-2 algorithm:
  - `easiness` within configured range (e.g. 1.3–2.5).
  - `intervalDays` ≥ 0, `repetitions` ≥ 0.
  - `lastGrade` mapped from grade enum (`AGAIN`, `HARD`, `GOOD`, `EASY`).

---

## Business logic

### SRS reviews

- `GET /srs/reviews`:
  - Return due cards (`dueAt <= today`) sorted by `dueAt`.
- `POST /srs/reviews`:
  - Validate grade; update `easiness`, `intervalDays`, `repetitions`, `dueAt`.
