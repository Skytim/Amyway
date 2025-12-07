# Lucky Draw System

A high-concurrency "Wheel of Fortune" style lucky draw system built with Java 21 and Spring Boot 3.

## Features
- **Prize Configuration**: Dynamic probability, stock control, and "Thank You" entries.
- **Concurrency Safety**: Prevents over-selling using Optimistic Locking (DB).
- **User Limits**: Restricts draws per user per activity.
- **Fairness**: Weighted Random Algorithm for probability distribution.
- **Multi-Environment**: Swappable configurations for Dev/Prod.

## Architecture

### Tech Stack
- **Language**: Java 21
- **Framework**: Spring Boot 3.2
- **Database**: H2 (Dev) / MySQL/PostgreSQL (Prod ready via config)
- **Cache/Stock**: Redis (Planned) / DB Optimistic Locking (Implemented)

### Stock Control Strategy
1.  **Check Stock**: Filter prizes with `availableStock > 0`.
2.  **Select Prize**: Use weighted random algorithm.
3.  **Decrement Stock**: atomic update `UPDATE ... SET stock = stock - 1 WHERE id = ? AND stock > 0`.
4.  **Retry/Fallback**: If update fails (0 rows modified), treat as "Miss" or retry. Current impl falls back to "Empty" prize.

## Database Schema

### ER Model
- **Activity** `1` -- `*` **Prize**
- **Activity** `1` -- `*` **DrawRecord**

### DDL (H2/MySQL Compatible)

```sql
CREATE TABLE activities (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    max_draws_per_user INT DEFAULT 1
);

CREATE TABLE prizes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    activity_id BIGINT,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(20) NOT NULL, -- GOLD, SILVER, BRONZE, EMPTY
    total_stock INT NOT NULL,
    available_stock INT NOT NULL,
    probability DOUBLE NOT NULL,
    version INT DEFAULT 0
);

CREATE TABLE draw_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id VARCHAR(255) NOT NULL,
    activity_id BIGINT NOT NULL,
    prize_id BIGINT,
    prize_name VARCHAR(255),
    draw_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_win BOOLEAN
);
```

## API Documentation

### Authentication
Header: `Authorization: Bearer <token>`
- Admin: `Bearer admin-secret`
- User: `Bearer user-token` (Mock)

### 1. Admin - Create Activity
- **POST** `/api/admin/activities`
- **Body**:
```json
{
  "name": "New Year Event",
  "maxDrawsPerUser": 5
}
```

### 2. Admin - Add Prize
- **POST** `/api/admin/prizes`
- **Body**:
```json
{
  "activityId": 1,
  "name": "iPhone 15",
  "type": "GOLD",
  "totalStock": 10,
  "availableStock": 10,
  "probability": 0.01
}
```

### 3. User - Draw
- **POST** `/api/draw`
- **Body**:
```json
{
  "userId": "user123",
  "activityId": 1
}
```
- **Response**:
```json
{
  "isWin": true,
  "prize": {
    "name": "iPhone 15",
    "type": "GOLD"
  },
  "message": "Congratulations!"
}
```

## Testing
Run unit and integration tests:
```bash
mvn test
```
