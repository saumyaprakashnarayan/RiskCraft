# RiskCraft Backend API Documentation

Complete API documentation for the RiskCraft Express server with JWT authentication, PostgreSQL, and Prisma.

## Table of Contents

1. [Authentication Endpoints](#authentication-endpoints)
2. [Trade Endpoints](#trade-endpoints)
3. [Gamification Endpoints](#gamification-endpoints)
4. [Database Schema](#database-schema)
5. [Architecture](#architecture)

---

## Authentication Endpoints

### 1. Register User

**POST** `/api/auth/register`

Register a new user with email, name, and password.

**Request Body:**
```json
{
  "email": "john@example.com",
  "name": "John Doe",
  "password": "securepassword123"
}
```

**Response (201):**
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "user": {
      "id": 1,
      "email": "john@example.com",
      "name": "John Doe",
      "level": 1
    },
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

### 2. Login User

**POST** `/api/auth/login`

Login with email and password.

**Request Body:**
```json
{
  "email": "john@example.com",
  "password": "securepassword123"
}
```

**Response (200):**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "user": {
      "id": 1,
      "email": "john@example.com",
      "name": "John Doe",
      "level": 5,
      "xp": 450
    },
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

### 3. Get User Profile

**GET** `/api/auth/profile`

Retrieve current user's profile (protected route).

**Headers:**
```
Authorization: Bearer <token>
```

**Response (200):**
```json
{
  "success": true,
  "message": "Profile retrieved successfully",
  "data": {
    "user": {
      "id": 1,
      "email": "john@example.com",
      "name": "John Doe",
      "level": 5,
      "xp": 450,
      "wallet": {
        "id": 1,
        "userId": 1,
        "balance": "9250.50",
        "dailyLoss": "249.50"
      },
      "badges": [
        {
          "id": 1,
          "name": "First Trade",
          "description": "Execute your first trade",
          "earnedAt": "2024-02-06T10:30:00.000Z"
        }
      ]
    }
  }
}
```

---

## Trade Endpoints

**All trade endpoints require authentication (Bearer token)**

### 1. Execute Trade

**POST** `/api/trades/execute`

Execute a BUY or SELL trade.

**Request Body:**
```json
{
  "asset": "AAPL",
  "quantity": 10,
  "price": 150.25,
  "tradeType": "BUY",
  "stopLoss": 145.00
}
```

**Response (201):**
```json
{
  "success": true,
  "message": "BUY trade executed successfully",
  "data": {
    "trade": {
      "id": 1,
      "asset": "AAPL",
      "quantity": 10,
      "price": 150.25,
      "tradeType": "BUY",
      "stopLoss": 145.00,
      "createdAt": "2024-02-06T10:30:00.000Z"
    },
    "newBalance": 8497.50
  }
}
```

**Error Response (400):**
```json
{
  "success": false,
  "message": "Insufficient balance for trade"
}
```

### 2. Get Trade History

**GET** `/api/trades/history?limit=50&offset=0`

Retrieve user's trade history with pagination.

**Query Parameters:**
- `limit` (optional): Number of trades to retrieve (default: 50, max: 100)
- `offset` (optional): Number of trades to skip (default: 0)

**Response (200):**
```json
{
  "success": true,
  "message": "Trade history retrieved successfully",
  "data": {
    "trades": [
      {
        "id": 1,
        "asset": "AAPL",
        "quantity": 10,
        "price": 150.25,
        "tradeType": "BUY",
        "stopLoss": 145.00,
        "createdAt": "2024-02-06T10:30:00.000Z"
      },
      {
        "id": 2,
        "asset": "GOOGL",
        "quantity": 5,
        "price": 140.00,
        "tradeType": "BUY",
        "stopLoss": null,
        "createdAt": "2024-02-06T11:15:00.000Z"
      }
    ],
    "pagination": {
      "limit": 50,
      "offset": 0,
      "total": 2
    }
  }
}
```

### 3. Get Asset Trades

**GET** `/api/trades/asset/:asset`

Get all trades for a specific asset.

**Response (200):**
```json
{
  "success": true,
  "message": "Trades for AAPL retrieved successfully",
  "data": {
    "asset": "AAPL",
    "trades": [
      {
        "id": 1,
        "quantity": 10,
        "price": 150.25,
        "tradeType": "BUY",
        "stopLoss": 145.00,
        "createdAt": "2024-02-06T10:30:00.000Z"
      }
    ]
  }
}
```

### 4. Get Portfolio

**GET** `/api/trades/portfolio`

Get current portfolio holdings and P&L metrics.

**Response (200):**
```json
{
  "success": true,
  "message": "Portfolio retrieved successfully",
  "data": {
    "portfolio": [
      {
        "asset": "AAPL",
        "netQuantity": 10,
        "averagePrice": 150.25
      },
      {
        "asset": "GOOGL",
        "netQuantity": 5,
        "averagePrice": 140.00
      }
    ],
    "realizedPnL": 1250.50,
    "totalVolume": 50000.00,
    "diversityScore": 40
  }
}
```

---

## Gamification Endpoints

**All gamification endpoints require authentication (Bearer token)**

### 1. Get User Badges

**GET** `/api/gamification/badges`

Retrieve all badges earned by the user.

**Response (200):**
```json
{
  "success": true,
  "message": "Badges retrieved successfully",
  "data": {
    "badges": [
      {
        "id": 1,
        "name": "First Trade",
        "description": "Execute your first trade",
        "icon": "🚀",
        "earnedAt": "2024-02-06T10:30:00.000Z"
      },
      {
        "id": 2,
        "name": "Risk Disciplinarian",
        "description": "Completed 10 trades with stop-loss orders",
        "icon": "🛡️",
        "earnedAt": "2024-02-06T12:00:00.000Z"
      }
    ],
    "totalBadges": 2
  }
}
```

### 2. Complete Challenge

**POST** `/api/gamification/challenges/:challengeId/complete`

Mark a challenge as completed and award XP.

**Response (200):**
```json
{
  "success": true,
  "message": "Challenge completed successfully",
  "data": {
    "challenge": {
      "id": 1,
      "status": "completed",
      "completedAt": "2024-02-06T14:30:00.000Z"
    }
  }
}
```

### 3. Get Leaderboard

**GET** `/api/gamification/leaderboard/:scoreType?limit=50`

Get leaderboard rankings for a specific score type.

**Path Parameters:**
- `scoreType`: `risk`, `consistency`, or `discipline`

**Query Parameters:**
- `limit` (optional): Number of entries (default: 50, max: 100)

**Response (200):**
```json
{
  "success": true,
  "message": "consistency leaderboard retrieved successfully",
  "data": {
    "scoreType": "consistency",
    "leaderboard": [
      {
        "rank": 1,
        "user": {
          "id": 5,
          "name": "Jane Smith",
          "email": "jane@example.com",
          "level": 12
        },
        "score": 950.50
      },
      {
        "rank": 2,
        "user": {
          "id": 1,
          "name": "John Doe",
          "email": "john@example.com",
          "level": 5
        },
        "score": 850.25
      }
    ]
  }
}
```

### 4. Get Gamification Stats

**GET** `/api/gamification/stats`

Get user's gamification statistics.

**Response (200):**
```json
{
  "success": true,
  "message": "Gamification stats retrieved successfully",
  "data": {
    "totalBadges": 3,
    "rank": 15,
    "recentBadges": [
      {
        "name": "Risk Disciplinarian",
        "earnedAt": "2024-02-06T12:00:00.000Z"
      },
      {
        "name": "First Trade",
        "earnedAt": "2024-02-06T10:30:00.000Z"
      }
    ]
  }
}
```

---

## Database Schema

### Users Table

Stores user account information and progression.

| Field | Type | Description |
|-------|------|-------------|
| id | INT (PK) | User ID |
| email | STRING (UNIQUE) | Email address |
| name | STRING | Full name |
| password | STRING | Hashed password |
| level | INT | Current level (default: 1) |
| xp | INT | Experience points (default: 0) |
| createdAt | TIMESTAMP | Account creation time |
| updatedAt | TIMESTAMP | Last update time |

### Wallets Table

Maintains virtual capital and daily loss tracking.

| Field | Type | Description |
|-------|------|-------------|
| id | INT (PK) | Wallet ID |
| userId | INT (FK) | Reference to user |
| balance | DECIMAL | Current balance (default: 10000) |
| dailyLoss | DECIMAL | Daily loss amount (default: 0) |
| createdAt | TIMESTAMP | Creation time |
| updatedAt | TIMESTAMP | Last update time |

### Trades Table

Complete trade history.

| Field | Type | Description |
|-------|------|-------------|
| id | INT (PK) | Trade ID |
| userId | INT (FK) | Reference to user |
| asset | STRING | Asset symbol (e.g., AAPL) |
| quantity | DECIMAL | Number of units |
| price | DECIMAL | Execution price |
| tradeType | STRING | BUY or SELL |
| stopLoss | DECIMAL (NULL) | Stop-loss value |
| createdAt | TIMESTAMP | Trade execution time |

### Badges Table

Achievement badges.

| Field | Type | Description |
|-------|------|-------------|
| id | INT (PK) | Badge ID |
| name | STRING (UNIQUE) | Badge name |
| description | STRING | Badge criteria |
| icon | STRING (NULL) | Badge emoji/icon |
| createdAt | TIMESTAMP | Creation time |

### UserBadges Table

User-badge relationship (many-to-many).

| Field | Type | Description |
|-------|------|-------------|
| id | INT (PK) | Record ID |
| userId | INT (FK) | Reference to user |
| badgeId | INT (FK) | Reference to badge |
| earnedAt | TIMESTAMP | Badge earned time |

### Leaderboard Table

Leaderboard scores and rankings.

| Field | Type | Description |
|-------|------|-------------|
| id | INT (PK) | Record ID |
| userId | INT (FK) | Reference to user |
| scoreType | STRING | risk, consistency, discipline |
| scoreValue | DECIMAL | Score value |
| rank | INT | User rank |
| updatedAt | TIMESTAMP | Last update time |

### Challenges Table

Daily and weekly challenges.

| Field | Type | Description |
|-------|------|-------------|
| id | INT (PK) | Challenge ID |
| name | STRING (UNIQUE) | Challenge name |
| description | STRING | Challenge objective |
| rewardXp | INT | XP awarded (default: 10) |
| isActive | BOOLEAN | Active status (default: true) |
| createdAt | TIMESTAMP | Creation time |
| updatedAt | TIMESTAMP | Last update time |

### UserChallenges Table

User challenge participation.

| Field | Type | Description |
|-------|------|-------------|
| id | INT (PK) | Record ID |
| userId | INT (FK) | Reference to user |
| challengeId | INT (FK) | Reference to challenge |
| status | STRING | pending or completed |
| completedAt | TIMESTAMP (NULL) | Completion time |

---

## Architecture

```
┌──────────────────────────┐
│ Kotlin App (Android)     │
│ REST APIs (JSON + JWT)   │
└────────────┬─────────────┘
             │
             ▼
┌──────────────────────────┐
│ Express API Layer        │
│ - Routes                 │
│ - JWT Middleware         │
└────────────┬─────────────┘
             │
             ▼
┌──────────────────────────┐
│ Services (Core Logic)    │
│ - Trade Engine           │
│ - Portfolio Engine       │
│ - Gamification Engine    │
│ - User Service           │
└────────────┬─────────────┘
             │
             ▼
┌──────────────────────────┐
│ Prisma ORM               │
│ - Database Abstraction   │
│ - Type Safety            │
└────────────┬─────────────┘
             │
             ▼
┌──────────────────────────┐
│ PostgreSQL Database      │
│ - Users, Trades, Badges  │
│ - Wallets, Leaderboard   │
└──────────────────────────┘
```

---

## Error Responses

### 400 Bad Request
```json
{
  "success": false,
  "message": "Asset, quantity, price, and tradeType are required"
}
```

### 401 Unauthorized
```json
{
  "success": false,
  "message": "Invalid or expired token"
}
```

### 404 Not Found
```json
{
  "success": false,
  "message": "User not found"
}
```

### 409 Conflict
```json
{
  "success": false,
  "message": "User with this email already exists"
}
```

### 500 Internal Server Error
```json
{
  "success": false,
  "message": "Internal server error"
}
```

---

## Setup & Running

See [SETUP.md](./SETUP.md) for detailed setup instructions.

## Testing with cURL

**Register User:**
```bash
curl -X POST https://localhost:3000/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "name": "Test User",
    "password": "password123"
  }'
```

**Login:**
```bash
curl -X POST https://localhost:3000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "user@example.com", "password": "password123"}'
```

**Execute Trade:**
```bash
curl -X POST https://localhost:3000/api/trades/execute \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "asset": "AAPL",
    "quantity": 10,
    "price": 150.25,
    "tradeType": "BUY",
    "stopLoss": 145.00
  }'
```

**Get Portfolio:**
```bash
curl -X GET https://localhost:3000/api/trades/portfolio \
  -H "Authorization: Bearer <token>"
```

**Get Leaderboard:**
```bash
curl -X GET https://localhost:3000/api/gamification/leaderboard/consistency \
  -H "Authorization: Bearer <token>"
```

---

## License

ISC
