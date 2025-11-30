# User/account API

## Resource: User

**Table**: `users`  

**Fields**

- `id`
- `email`
- `passwordHash`
- `emailVerifiedAt`
- `createdAt`

**API use**

- Current profile.
- Account deletion.

---

## Endpoints

### [U1] Get current profile

- **Method**: `GET`  
- **Path**: `/api/me`  
- **Description**: Basic info about current user.

**Response JSON**

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


### [U2] Delete account

- **Method**: `DELETE`  
- **Path**: `/api/me`  
- **Description**: Request full account deletion and data removal.

**Request body**: none  

**Response**: empty or a small status object  

- **Success**: `202 Accepted` (deletion may be processed asynchronously)  
- **Errors**: `401`

---

## Business logic

### Account deletion (`DELETE /me`)

- Mark user for deletion or delete synchronously:
  - Remove user row.
  - Cascade deletes (words, cards, card_words, srs_state, stats/logs where applicable).
- Return `202 Accepted` so backend can execute heavy cleanup asynchronously if needed.
