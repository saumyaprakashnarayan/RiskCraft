# RiskCraft Backend - File Manifest

## Complete File Structure

```
RiskCraft/
│
├── 📄 Configuration Files
│   ├── package.json              (Dependencies & scripts)
│   ├── .env.example              (Environment template)
│   └── .gitignore                (Git ignore rules)
│
├── 📁 src/
│   │
│   ├── server.js                 (Express HTTPS server, route registration)
│   │
│   ├── 📁 routes/
│   │   ├── auth.js               (Auth endpoints: register, login, profile)
│   │   ├── trades.js             (Trade endpoints: execute, history, portfolio)
│   │   └── gamification.js       (Gamification: badges, leaderboard, challenges)
│   │
│   └── 📁 services/
│       ├── userService.js        (User CRUD, auth, progression)
│       ├── tradeEngine.js        (Trade execution, wallet management)
│       ├── portfolioEngine.js    (Portfolio calc, P&L, diversity)
│       └── gamificationEngine.js (Badges, XP, challenges, leaderboard)
│
├── 📁 prisma/
│   ├── schema.prisma             (Database schema - all 8 models)
│   └── seed.js                   (Database seed - badges & challenges)
│
├── 📚 Documentation
│   ├── README.md                 (Project setup & overview)
│   ├── SETUP.md                  (Detailed setup instructions)
│   ├── API_DOCS.md               (Complete API reference)
│   ├── ARCHITECTURE.md           (System design & data flow)
│   └── IMPLEMENTATION_SUMMARY.md (What was built)
│
├── 🧪 Testing
│   └── RiskCraft-API.postman_collection.json (Postman collection)
│
└── 📁 node_modules/              (Dependencies - created after npm install)

```

---

## File Descriptions

### Configuration & Build

**[package.json](package.json)**
- Project metadata
- Dependencies: express, bcryptjs, jsonwebtoken, @prisma/client, dotenv, cors
- Scripts: start, dev, prisma:migrate, prisma:generate, prisma:studio

**[.env.example](.env.example)**
- Template for environment variables
- DATABASE_URL, JWT_SECRET, JWT_EXPIRE, PORT, NODE_ENV

**[.gitignore](.gitignore)**
- Excludes: .env, node_modules, certs/, .DS_Store

---

### Server & Routes

**[src/server.js](src/server.js)** (~70 lines)
- Express HTTPS server setup
- Middleware: CORS, JSON parser, JWT verification
- Route registration for auth, trades, gamification
- Health check endpoint
- Graceful shutdown with Prisma disconnect

**[src/routes/auth.js](src/routes/auth.js)** (~80 lines)
- `POST /api/auth/register` - User registration with wallet creation
- `POST /api/auth/login` - User authentication with JWT
- `GET /api/auth/profile` - Get user profile (protected)
- Input validation and error handling

**[src/routes/trades.js](src/routes/trades.js)** (~100 lines)
- `POST /api/trades/execute` - Execute BUY/SELL trades
- `GET /api/trades/history` - Trade history with pagination
- `GET /api/trades/asset/:asset` - Asset-specific trades
- `GET /api/trades/portfolio` - Current portfolio & metrics

**[src/routes/gamification.js](src/routes/gamification.js)** (~90 lines)
- `GET /api/gamification/badges` - User badges
- `POST /api/gamification/challenges/:id/complete` - Complete challenge
- `GET /api/gamification/leaderboard/:type` - Get rankings
- `GET /api/gamification/stats` - User stats

---

### Services & Business Logic

**[src/services/userService.js](src/services/userService.js)** (~60 lines)
- `createUser()` - Register new user with wallet
- `getUserByEmail()` - Authentication lookup
- `getUserById()` - Get user with relations
- `updateUserProgress()` - Update XP and level
- `verifyPassword()` - bcrypt password verification

**[src/services/tradeEngine.js](src/services/tradeEngine.js)** (~100 lines)
- `executeTrade()` - BUY/SELL order execution with balance check
- `getTradeHistory()` - Paginated trade history
- `getAssetTrades()` - Trades for specific asset
- `getTotalTradesCount()` - Count user trades
- `updateDailyLoss()` - Track daily losses
- `resetDailyLoss()` - Daily reset

**[src/services/portfolioEngine.js](src/services/portfolioEngine.js)** (~120 lines)
- `getPortfolio()` - Calculate portfolio from trades (derived)
- `calculateUnrealizedPnL()` - Current P&L
- `calculateRealizedPnL()` - FIFO-based closed position P&L
- `calculateDiversityScore()` - Asset diversification metric

**[src/services/gamificationEngine.js](src/services/gamificationEngine.js)** (~150 lines)
- `awardBadge()` - Award achievement badge
- `getUserBadges()` - Get user's badges
- `awardXP()` - Award XP with level-up check
- `completeChallenge()` - Complete challenge & award XP
- `checkRiskDisciplineBadge()` - Automatic badge check (10 stop-loss trades)
- `checkConsistencyBadge()` - Automatic badge check (5 consecutive days)
- `updateLeaderboardScore()` - Update ranking score
- `getLeaderboard()` - Get ranked users

---

### Database

**[prisma/schema.prisma](prisma/schema.prisma)** (~140 lines)

Models:
1. **User** - id, email, name, password, level, xp
2. **Wallet** - id, userId, balance, dailyLoss
3. **Trade** - id, userId, asset, quantity, price, tradeType, stopLoss
4. **Badge** - id, name, description, icon
5. **UserBadge** - id, userId, badgeId, earnedAt (many-to-many)
6. **Leaderboard** - id, userId, scoreType, scoreValue, rank
7. **Challenge** - id, name, description, rewardXp, isActive
8. **UserChallenge** - id, userId, challengeId, status, completedAt (many-to-many)

Relationships:
- User ↔ Wallet (1:1)
- User ↔ Trade (1:many)
- User ↔ UserBadge ↔ Badge (1:many:many)
- User ↔ UserChallenge ↔ Challenge (1:many:many)
- User ↔ Leaderboard (1:many)

**[prisma/seed.js](prisma/seed.js)** (~60 lines)
- Creates 8 badges: Risk Disciplinarian, Consistency Champion, First Trade, Portfolio Master, Profit Pioneer, Disciplined Trader, Risk Manager, Trading Legend
- Creates 6 challenges: Daily Trader, Multiple Trades, Diversify Portfolio, Profit Target, Risk Control, No Loss Day

---

### Documentation

**[README.md](README.md)** (~250 lines)
- Project overview
- Features & prerequisites
- Installation steps (3-5 minutes)
- Running instructions
- API endpoints summary
- Database schema overview
- Security features
- Testing examples (cURL, Postman, REST Client)

**[SETUP.md](SETUP.md)** (~300 lines)
- Step-by-step setup guide
- Node.js & PostgreSQL installation
- Database creation
- Environment configuration
- Project setup (npm install, Prisma migrations)
- HTTPS certificate generation
- Multiple testing methods
- Troubleshooting guide
- Useful commands

**[API_DOCS.md](API_DOCS.md)** (~400 lines)
- Complete API reference
- Authentication endpoints (register, login, profile)
- Trade endpoints (execute, history, portfolio)
- Gamification endpoints (badges, challenges, leaderboard)
- Database schema tables (all 8 models with fields)
- Architecture diagram
- Error responses
- Testing examples with cURL
- Development commands

**[ARCHITECTURE.md](ARCHITECTURE.md)** (~400 lines)
- System architecture diagram
- Data flow for trade execution
- Database relationships diagram
- API response flow
- User progression system
- Badge earning logic
- Leaderboard types
- Portfolio FIFO calculation
- Security measures
- Error handling strategy
- Performance considerations

**[IMPLEMENTATION_SUMMARY.md](IMPLEMENTATION_SUMMARY.md)** (~200 lines)
- What was built (8 database models, 4 services, 3 routes)
- Architecture overview
- Key features checklist
- File structure
- Development commands

---

### Testing

**[RiskCraft-API.postman_collection.json](RiskCraft-API.postman_collection.json)** (~250 lines)
- Complete Postman collection for all endpoints
- Pre-configured base URL and token variables
- Tests that auto-capture tokens from responses
- All 13 endpoints ready to test
- Example request bodies for each endpoint
- Support for authentication flow

---

## Summary

| Category | Count | Lines |
|----------|-------|-------|
| Configuration Files | 3 | 20 |
| Route Handlers | 3 | 270 |
| Service/Engine Files | 4 | 430 |
| Database Schema | 2 | 200 |
| Documentation | 6 | 1,600 |
| Testing | 1 | 250 |
| **TOTAL** | **19** | **~2,770** |

---

## Quick Navigation

### For Deployment
1. [SETUP.md](SETUP.md) - Complete setup guide
2. [package.json](package.json) - Dependencies
3. [.env.example](.env.example) - Configuration template

### For Development
1. [src/server.js](src/server.js) - Main server entry
2. [src/routes/](src/routes/) - API endpoints
3. [src/services/](src/services/) - Business logic

### For Integration
1. [API_DOCS.md](API_DOCS.md) - Complete API reference
2. [RiskCraft-API.postman_collection.json](RiskCraft-API.postman_collection.json) - Testing
3. [ARCHITECTURE.md](ARCHITECTURE.md) - System design

### For Database
1. [prisma/schema.prisma](prisma/schema.prisma) - Data models
2. [prisma/seed.js](prisma/seed.js) - Sample data
3. [API_DOCS.md#database-schema](API_DOCS.md) - Database documentation

---

## File Dependencies

```
server.js
  ├── routes/auth.js → services/userService.js
  ├── routes/trades.js → services/tradeEngine.js, portfolioEngine.js, gamificationEngine.js
  └── routes/gamification.js → services/gamificationEngine.js

All services
  └── @prisma/client → prisma/schema.prisma

prisma/seed.js
  └── @prisma/client → prisma/schema.prisma
```

---

## Generated Files After Setup

After running `npm install` and `npx prisma migrate dev`:

```
node_modules/               (Dependencies)
prisma/
  └── migrations/           (Database migration history)

After generating certs:
certs/
  ├── cert.pem              (SSL certificate)
  └── key.pem               (Private key)

Created by Prisma:
prisma/
  └── client/               (Prisma Client JS library)
```

---

Last Updated: February 6, 2026
Status: ✅ Complete & Ready for Development
