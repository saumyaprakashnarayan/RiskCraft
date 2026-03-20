# 🚀 RiskCraft Backend - Quick Start Guide

## ⚡ 5-Minute Setup

### Prerequisites
- **Node.js** v14+ (download from https://nodejs.org/)
- **PostgreSQL** (download from https://www.postgresql.org/)

---

## Step 1: Download & Navigate (1 min)

```bash
# Navigate to project
cd "C:\Users\spnar\OneDrive\Desktop\RiskCraft"
```

---

## Step 2: Install Dependencies (2 min)

```bash
npm install
```

This installs:
- express (server framework)
- jsonwebtoken (JWT auth)
- bcryptjs (password hashing)
- @prisma/client (database)
- dotenv (environment variables)
- cors (cross-origin requests)

---

## Step 3: Setup Database (1 min)

```bash
# Create .env file with database URL
copy .env.example .env
```

Edit `.env` and update:
```
DATABASE_URL="postgresql://postgres:YOUR_DB_PASSWORD@localhost:5432/riskcraft_db"
JWT_SECRET="any-random-secret-key"
```

Then run migrations:
```bash
npx prisma migrate dev --name init
```

---

## Step 4: Start Server (30 sec)

```bash
# Development mode with auto-reload
npm run dev

# Or production mode
npm start
```

✅ **Server running at:** `http://localhost:3000` or `https://localhost:3000`

---

## 🧪 Quick Test

### Option 1: Using cURL

**Register User:**
```bash
curl -X POST http://localhost:3000/api/auth/register ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"test@example.com\",\"name\":\"Test User\",\"password\":\"password123\"}"
```

Copy the `token` from response.

**Execute Trade:**
```bash
curl -X POST http://localhost:3000/api/trades/execute ^
  -H "Authorization: Bearer YOUR_TOKEN" ^
  -H "Content-Type: application/json" ^
  -d "{\"asset\":\"AAPL\",\"quantity\":10,\"price\":150.25,\"tradeType\":\"BUY\",\"stopLoss\":145}"
```

### Option 2: Using REST Client Extension

Create `test.http` in VS Code:

```http
### Register
POST http://localhost:3000/api/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "name": "John Doe",
  "password": "password123"
}

### Get Profile (replace TOKEN with actual token)
GET http://localhost:3000/api/auth/profile
Authorization: Bearer TOKEN

### Execute Trade
POST http://localhost:3000/api/trades/execute
Authorization: Bearer TOKEN
Content-Type: application/json

{
  "asset": "AAPL",
  "quantity": 10,
  "price": 150.25,
  "tradeType": "BUY",
  "stopLoss": 145.00
}

### Get Portfolio
GET http://localhost:3000/api/trades/portfolio
Authorization: Bearer TOKEN

### Get Leaderboard
GET http://localhost:3000/api/gamification/leaderboard/consistency
Authorization: Bearer TOKEN
```

Install "REST Client" extension in VS Code and click "Send Request" on each block.

### Option 3: Using Postman

1. Download Postman: https://www.postman.com/downloads/
2. Import collection: [RiskCraft-API.postman_collection.json](RiskCraft-API.postman_collection.json)
3. Set `baseUrl` variable to `http://localhost:3000`
4. Run requests in order (tokens auto-capture)

---

## 📚 Available Endpoints

### Auth (Public)
```
POST   /api/auth/register         → Create account
POST   /api/auth/login            → Login
GET    /api/auth/profile          → Get profile (protected)
```

### Trades (Protected)
```
POST   /api/trades/execute        → Execute BUY/SELL
GET    /api/trades/history        → Trade history
GET    /api/trades/portfolio      → Current holdings
GET    /api/trades/asset/:asset   → Trades for asset
```

### Gamification (Protected)
```
GET    /api/gamification/badges        → User badges
GET    /api/gamification/leaderboard/:type  → Rankings
GET    /api/gamification/stats         → User stats
POST   /api/gamification/challenges/:id/complete → Complete challenge
```

### Health
```
GET    /health                    → Server status
```

---

## 💡 Common Commands

```bash
# Start dev server (auto-reload)
npm run dev

# Start production server
npm start

# Run database migrations
npm run prisma:migrate

# Open Prisma Studio (visual DB editor)
npm run prisma:studio

# Generate Prisma Client
npm run prisma:generate

# Seed sample data
npx prisma db seed
```

---

## 🐛 Troubleshooting

### "npm is not recognized"
→ Restart Windows after installing Node.js

### "psql is not recognized"
→ Add PostgreSQL to PATH or restart Windows

### "ERROR: connect ECONNREFUSED"
→ PostgreSQL not running. Start PostgreSQL service.

### "ERROR: database does not exist"
→ Create database: `CREATE DATABASE riskcraft_db;`

### "Error: Invalid Prisma schema"
→ Check schema.prisma for syntax errors

### "Port 3000 already in use"
→ Change PORT in .env or kill process on port 3000

---

## 🔑 Key Features Explained

### User Progression
- Register with email/name/password
- Get 10,000 virtual capital
- Earn XP through trades (10 XP per trade)
- Level up every 100 XP

### Trading System
- Execute BUY/SELL orders
- Set stop-loss on trades
- Real-time balance updates
- Track daily losses

### Portfolio
- Auto-calculated from trades
- FIFO position matching
- P&L calculations
- Diversity scoring

### Gamification
- 8 Badges (achievable through trading)
- 6 Daily Challenges with XP rewards
- 3 Leaderboards (risk, consistency, discipline)
- XP-based leveling system

---

## 📖 Full Documentation

For complete information, see:
- **[API_DOCS.md](API_DOCS.md)** - Full API reference
- **[SETUP.md](SETUP.md)** - Detailed setup
- **[ARCHITECTURE.md](ARCHITECTURE.md)** - System design
- **[README.md](README.md)** - Overview

---

## 🛡️ Security Notes

- ✅ Passwords hashed with bcryptjs (10 rounds)
- ✅ JWT tokens expire in 7 days
- ✅ All routes validated
- ✅ HTTPS ready (provide certificates)
- ✅ Protected endpoints require token

---

## 📊 Database Info

**Default Sample Data:**
- 8 Badges
- 6 Challenges
- Ready for user registration

**Initial Wallet Balance:** $10,000 (virtual)

---

## 🎯 Next Steps

1. **Complete Setup** → Run through Step 1-4 above
2. **Test Endpoints** → Try Quick Test section
3. **Read Docs** → Check [API_DOCS.md](API_DOCS.md)
4. **Build Frontend** → Connect Kotlin app to these endpoints
5. **Deploy** → Use [SETUP.md](SETUP.md) for production

---

## 📞 Need Help?

- 📖 Check [SETUP.md](SETUP.md) for detailed instructions
- 📚 Read [API_DOCS.md](API_DOCS.md) for endpoint details
- 🏗️ See [ARCHITECTURE.md](ARCHITECTURE.md) for system design
- ✅ Review [IMPLEMENTATION_CHECKLIST.md](IMPLEMENTATION_CHECKLIST.md)

---

**You're all set! 🎉**

Happy trading! 📈

*Last Updated: February 6, 2026*
