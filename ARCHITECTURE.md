# RiskCraft Backend - Architecture & Data Flow

## System Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    KOTLIN ANDROID APP                           │
│  (REST Client making HTTP/HTTPS requests with JSON)             │
└────────────────────────┬────────────────────────────────────────┘
                         │ JSON Requests with JWT Tokens
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│              EXPRESS HTTPS SERVER (Port 3000)                   │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌──────────────────────────────────────────────────────┐      │
│  │ MIDDLEWARE LAYER                                     │      │
│  ├──────────────────────────────────────────────────────┤      │
│  │ ✓ CORS Handler                                      │      │
│  │ ✓ JSON Parser                                       │      │
│  │ ✓ JWT Verification (Protected Routes)              │      │
│  └──────────────────────────────────────────────────────┘      │
│                                                                 │
│  ┌──────────────────────────────────────────────────────┐      │
│  │ ROUTE LAYER                                          │      │
│  ├──────────────────────────────────────────────────────┤      │
│  │ /api/auth/*          → authRoutes                    │      │
│  │ /api/trades/*        → tradeRoutes (protected)       │      │
│  │ /api/gamification/*  → gamificationRoutes (protected)│      │
│  │ /health              → health check                 │      │
│  └──────────────────────────────────────────────────────┘      │
│                                                                 │
│  ┌──────────────────────────────────────────────────────┐      │
│  │ SERVICE LAYER (Business Logic)                       │      │
│  ├──────────────────────────────────────────────────────┤      │
│  │ ┌──────────────────────────────────────────────┐    │      │
│  │ │ userService.js                               │    │      │
│  │ │ - Create/Authenticate Users                 │    │      │
│  │ │ - Manage Passwords                          │    │      │
│  │ │ - Update Progress (XP/Level)                │    │      │
│  │ └──────────────────────────────────────────────┘    │      │
│  │                                                      │      │
│  │ ┌──────────────────────────────────────────────┐    │      │
│  │ │ tradeEngine.js                               │    │      │
│  │ │ - Execute BUY/SELL Trades                   │    │      │
│  │ │ - Update Wallet Balance                     │    │      │
│  │ │ - Track Daily Losses                        │    │      │
│  │ │ - Calculate Realized P&L                    │    │      │
│  │ └──────────────────────────────────────────────┘    │      │
│  │                                                      │      │
│  │ ┌──────────────────────────────────────────────┐    │      │
│  │ │ portfolioEngine.js                           │    │      │
│  │ │ - Calculate Portfolio (from Trades)         │    │      │
│  │ │ - FIFO Position Matching                    │    │      │
│  │ │ - Unrealized P&L Calculation                │    │      │
│  │ │ - Diversity Score Calculation               │    │      │
│  │ └──────────────────────────────────────────────┘    │      │
│  │                                                      │      │
│  │ ┌──────────────────────────────────────────────┐    │      │
│  │ │ gamificationEngine.js                        │    │      │
│  │ │ - Award Badges & XP                         │    │      │
│  │ │ - Level Progression                         │    │      │
│  │ │ - Challenge Management                      │    │      │
│  │ │ - Leaderboard Updates                       │    │      │
│  │ │ - Automatic Badge Checks                    │    │      │
│  │ └──────────────────────────────────────────────┘    │      │
│  └──────────────────────────────────────────────────────┘      │
│                                                                 │
└────────────────┬───────────────────────────────────────────────┘
                 │ SQL Queries via Prisma ORM
                 ▼
┌─────────────────────────────────────────────────────────────────┐
│              PRISMA ORM (Type-Safe DB Layer)                    │
│  - Query Builder                                                │
│  - Migration Manager                                            │
│  - Type Generation                                              │
└────────────────┬───────────────────────────────────────────────┘
                 │ PostgreSQL Protocol (TCP/IP)
                 ▼
┌─────────────────────────────────────────────────────────────────┐
│           POSTGRESQL DATABASE (Port 5432)                       │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌──────────────────┐  ┌──────────────────┐                   │
│  │   USERS Table    │  │  WALLETS Table   │                   │
│  ├──────────────────┤  ├──────────────────┤                   │
│  │ id (PK)          │  │ id (PK)          │                   │
│  │ email (UNIQUE)   │  │ userId (FK)      │                   │
│  │ name             │  │ balance          │                   │
│  │ password         │  │ dailyLoss        │                   │
│  │ level            │  │ created/Updated  │                   │
│  │ xp               │  └──────────────────┘                   │
│  │ created/Updated  │                                         │
│  └──────────────────┘                                         │
│                                                                 │
│  ┌──────────────────┐  ┌──────────────────┐                   │
│  │   TRADES Table   │  │  PORTFOLIO View  │                   │
│  ├──────────────────┤  ├──────────────────┤                   │
│  │ id (PK)          │  │ (Calculated)     │                   │
│  │ userId (FK)      │  │ asset            │                   │
│  │ asset            │  │ netQuantity      │                   │
│  │ quantity         │  │ averagePrice     │                   │
│  │ price            │  │ unrealizedPnL    │                   │
│  │ tradeType        │  └──────────────────┘                   │
│  │ stopLoss         │                                         │
│  │ createdAt        │  ┌──────────────────┐                   │
│  └──────────────────┘  │  BADGES Table    │                   │
│                        ├──────────────────┤                   │
│  ┌──────────────────┐  │ id (PK)          │                   │
│  │USERBADGES Table  │  │ name             │                   │
│  ├──────────────────┤  │ description      │                   │
│  │ id (PK)          │  │ icon             │                   │
│  │ userId (FK)      │  └──────────────────┘                   │
│  │ badgeId (FK)     │                                         │
│  │ earnedAt         │  ┌──────────────────┐                   │
│  └──────────────────┘  │LEADERBOARD Table │                   │
│                        ├──────────────────┤                   │
│  ┌──────────────────┐  │ id (PK)          │                   │
│  │CHALLENGES Table  │  │ userId (FK)      │                   │
│  ├──────────────────┤  │ scoreType        │                   │
│  │ id (PK)          │  │ scoreValue       │                   │
│  │ name             │  │ rank             │                   │
│  │ description      │  └──────────────────┘                   │
│  │ rewardXp         │                                         │
│  │ isActive         │  ┌──────────────────┐                   │
│  └──────────────────┘  │USERCHALLENGES TB │                   │
│                        ├──────────────────┤                   │
│                        │ id (PK)          │                   │
│                        │ userId (FK)      │                   │
│                        │ challengeId (FK) │                   │
│                        │ status           │                   │
│                        │ completedAt      │                   │
│                        └──────────────────┘                   │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## Data Flow - Trade Execution

```
┌─────────────┐
│ Mobile App  │ POST /api/trades/execute
│             │ {asset, quantity, price, tradeType, stopLoss}
└──────┬──────┘
       │ JWT Token in Header
       ▼
┌─────────────────────────┐
│ verifyToken Middleware  │
│ Validates JWT           │
└──────┬──────────────────┘
       │ req.userId set
       ▼
┌─────────────────────────┐
│ tradeRoutes Handler     │
│ Validates input         │
└──────┬──────────────────┘
       │
       ▼
┌──────────────────────────────┐
│ tradeEngine.executeTrade()   │
├──────────────────────────────┤
│ 1. Fetch User Wallet         │
│ 2. Check Balance (BUY)       │
│ 3. Update Wallet Balance     │
│ 4. Create Trade Record       │
│ 5. Return new balance        │
└──────┬───────────────────────┘
       │
       ▼
┌──────────────────────────────┐
│ gamificationEngine.awardXP() │
│ Award 10 XP for trade        │
└──────┬───────────────────────┘
       │
       ▼
┌────────────────────────────────────────┐
│ gamificationEngine.checkBadges()       │
│ Check if user qualifies for badges:    │
│ - Risk Discipline (10+ stop-loss)      │
│ - Consistency (5+ consecutive days)    │
└──────┬─────────────────────────────────┘
       │
       ▼
┌─────────────────────┐
│ Response to Mobile  │
│ {trade, newBalance} │
└─────────────────────┘
```

---

## Database Relationships Diagram

```
User (1) ───────────────── (1) Wallet
         has one

User (1) ───────────────── (many) Trade
         executes

User (1) ───────────────── (many) UserBadge ───────────────── (1) Badge
         earns                                 references

User (1) ───────────────── (many) UserChallenge ───────────────── (1) Challenge
         participates                           references

User (1) ───────────────── (many) Leaderboard
         appears on

Portfolio ─ (Derived from Trades)
```

---

## API Response Flow

```
REQUEST VALIDATION
        ↓
ERROR → return 400 ✗
        ↓
JWT VERIFICATION (if protected route)
        ↓
ERROR → return 401 ✗
        ↓
BUSINESS LOGIC EXECUTION
        ↓
ERROR → return 400/500 ✗
        ↓
SUCCESS → return 200/201 ✓
        ↓
RESPONSE
{
  "success": true,
  "message": "...",
  "data": {...}
}
```

---

## User Progression System

```
LEVEL CALCULATION:
newLevel = Math.floor(totalXP / 100) + 1

Example:
0-99 XP     → Level 1
100-199 XP  → Level 2
200-299 XP  → Level 3
...
5000+ XP    → Level 51
```

---

## Badge Earning Logic

```
AUTOMATIC BADGE CHECKS:

Risk Disciplinarian:
  ┌─ Count trades with stopLoss NOT NULL
  ├─ if count >= 10
  └─ Award "Risk Disciplinarian" badge

Consistency Champion:
  ┌─ Extract unique trade dates
  ├─ Find consecutive day streaks
  ├─ if streak >= 5 days
  └─ Award "Consistency Champion" badge
```

---

## Leaderboard Types

```
1. RISK LEADERBOARD
   ├─ Score: Loss Risk Management
   ├─ Calculated from: Daily Loss Tracking
   └─ Lower score = Better risk management

2. CONSISTENCY LEADERBOARD
   ├─ Score: Trading Consistency
   ├─ Calculated from: Number of consecutive trading days
   └─ Higher score = More consistent

3. DISCIPLINE LEADERBOARD
   ├─ Score: Trade Discipline
   ├─ Calculated from: Trades with stop-loss + Win rate
   └─ Higher score = More disciplined
```

---

## Portfolio Calculation (FIFO)

```
TRADES HISTORY:
1. BUY AAPL  @150  qty=10
2. BUY AAPL  @155  qty=5
3. SELL AAPL @160  qty=8

FIFO MATCHING:
Sell 8 units from:
  - Sell 8 from transaction 1
  - Profit = (160-150)*8 = $80

REMAINING POSITION:
  - Asset: AAPL
  - Quantity: 7 (10+5-8)
  - Average Price: (150*10 + 155*5 - 80) / 7 ≈ 152.86
  - Cost Basis: $1,070
```

---

## Security Measures

```
PASSWORD SECURITY:
User Password Input
       ↓
bcryptjs.hash(password, 10)
       ↓
Hashed Password Stored in DB
       ↓
On Login: bcryptjs.compare(inputPassword, hashedPassword)

JWT TOKENS:
Issued at: Login/Register
Token contains: {id: userId}
Signed with: JWT_SECRET
Expires: 7 days (configurable)
Verified on: Every protected route access
```

---

## Error Handling Strategy

```
┌─────────────────────┐
│ Request Received    │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────────────────┐
│ Validate Input                  │
├─────────────────────────────────┤
│ Missing fields?    → 400         │
│ Invalid format?    → 400         │
│ Out of range?      → 400         │
└──────────┬──────────────────────┘
           │
           ▼
┌─────────────────────────────────┐
│ Verify Authentication (if needed)│
├─────────────────────────────────┤
│ No token?          → 401         │
│ Invalid token?     → 401         │
│ Expired token?     → 401         │
└──────────┬──────────────────────┘
           │
           ▼
┌─────────────────────────────────┐
│ Check Authorization             │
├─────────────────────────────────┤
│ User doesn't exist?  → 404       │
│ Resource not found?  → 404       │
│ Insufficient balance? → 400      │
└──────────┬──────────────────────┘
           │
           ▼
┌─────────────────────────────────┐
│ Database Operation              │
├─────────────────────────────────┤
│ Query failed?      → 500         │
│ Constraint violated? → 409       │
└──────────┬──────────────────────┘
           │
           ▼
┌─────────────────────────────────┐
│ Success Response                │
├─────────────────────────────────┤
│ Return 200/201 with data        │
└─────────────────────────────────┘
```

---

## Performance Considerations

```
INDEXING STRATEGY:
- userId in trades (frequent queries by user)
- asset in trades (asset-specific filtering)
- scoreType in leaderboard (leaderboard rankings)
- userId in all FK relationships (join operations)

PAGINATION:
- Trade history: 50 items default, 100 max
- Leaderboard: 50 items default, 100 max
- Prevents large dataset transfers

CACHING OPPORTUNITIES (Future):
- User profile (Redis cache)
- Portfolio calculations (cache for 1-5 minutes)
- Leaderboard rankings (cache for 1 hour)
```
