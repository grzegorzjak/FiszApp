# Stats API

## Resource: Stats

**Logical resource (aggregated per-day data)**  

- Aggregated per-day data:
  - cards generated/accepted/rejected
  - prompts used
  - reviews done
  - acceptance rate

---

## Endpoints

### [S1] Daily stats

- **Method**: `GET`  
- **Path**: `/api/stats/daily`  
- **Description**: Per-day metrics for current user (cards, prompts, reviews, acceptance rate).

**Query params**

- `date` (ISO date, optional, default today)
- or `from`, `to` (ISO dates) to return an array of days

**Response JSON (single day)**

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

## Validation

### Stats

- Dates validated (`from <= to`, max range, etc.).

---

## Business logic

### Stats computation (`/stats/daily`)

For each day (on read or via background pre-aggregation) compute:

- `cardsGenerated` – number of cards created via generation.
- `cardsAccepted`, `cardsRejected` – based on status transitions.
- `acceptanceRate` – accepted / generated (per day).
- `promptsUsed` – generation batches count.
- `reviewsDone` – number of SRS review events.
