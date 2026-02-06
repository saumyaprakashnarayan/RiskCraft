# Setup Instructions - RiskCraft Auth Server

## Prerequisites Installation

### Step 1: Install Node.js and npm

1. Download Node.js LTS from https://nodejs.org/
2. Run the installer and follow the setup wizard
3. Restart your computer to apply PATH changes
4. Verify installation:
   ```bash
   node --version
   npm --version
   ```

### Step 2: Install PostgreSQL

1. Download PostgreSQL from https://www.postgresql.org/download/
2. Run the installer and follow the setup wizard
3. Remember the password you set for the `postgres` superuser
4. Verify installation:
   ```bash
   psql --version
   ```

### Step 3: Create PostgreSQL Database

1. Open Command Prompt (cmd) or PowerShell
2. Connect to PostgreSQL:
   ```bash
   psql -U postgres
   ```
3. Create the database:
   ```sql
   CREATE DATABASE riskcraft_db;
   \q
   ```

## Project Setup

### Step 1: Navigate to Project Directory

```bash
cd "C:\Users\spnar\OneDrive\Desktop\RiskCraft"
```

### Step 2: Install Dependencies

```bash
npm install
```

### Step 3: Configure Environment Variables

1. Copy `.env.example` to `.env`:
   ```bash
   copy .env.example .env
   ```

2. Edit `.env` file and update with your database credentials:
   ```
   DATABASE_URL="postgresql://postgres:YOUR_PASSWORD@localhost:5432/riskcraft_db"
   JWT_SECRET="your-very-secure-secret-key-change-in-production"
   JWT_EXPIRE="7d"
   PORT=3000
   NODE_ENV="development"
   ```

### Step 4: Setup Prisma and Run Migrations

```bash
npx prisma migrate dev --name init
```

This will:
- Create the database tables
- Generate the Prisma Client

### Step 5: (Optional) Generate HTTPS SSL Certificates

```bash
mkdir certs
openssl req -nodes -new -x509 -keyout certs/key.pem -out certs/cert.pem -days 365 -subj "/CN=localhost"
```

Or use Git Bash if you have it installed:
```bash
# In Git Bash
mkdir certs
openssl req -nodes -new -x509 -keyout certs/key.pem -out certs/cert.pem -days 365 -subj "/CN=localhost"
```

## Running the Server

### Development Mode (with auto-reload)

```bash
npm run dev
```

### Production Mode

```bash
npm start
```

Server will run on:
- With HTTPS: `https://localhost:3000`
- Without certificates: `http://localhost:3000`

## Testing the API

You can test the API using:

### Option 1: Using cURL (Command Prompt/PowerShell)

**Health Check:**
```bash
curl -k https://localhost:3000/health
```

**Signup:**
```bash
curl -k -X POST https://localhost:3000/api/auth/signup ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"testuser\",\"email\":\"test@example.com\",\"password\":\"password123\"}"
```

**Login:**
```bash
curl -k -X POST https://localhost:3000/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"test@example.com\",\"password\":\"password123\"}"
```

### Option 2: Using Postman

1. Download Postman from https://www.postman.com/downloads/
2. Import endpoints:
   - POST `https://localhost:3000/api/auth/signup`
   - POST `https://localhost:3000/api/auth/login`
   - GET `https://localhost:3000/api/auth/profile` (add Authorization header with Bearer token)

### Option 3: Using VS Code REST Client

Install REST Client extension and create `requests.http`:
```http
### Health Check
GET https://localhost:3000/health

### Signup
POST https://localhost:3000/api/auth/signup
Content-Type: application/json

{
  "username": "testuser",
  "email": "test@example.com",
  "password": "password123"
}

### Login
POST https://localhost:3000/api/auth/login
Content-Type: application/json

{
  "email": "test@example.com",
  "password": "password123"
}

### Get Profile
GET https://localhost:3000/api/auth/profile
Authorization: Bearer <paste-your-token-here>
```

## Troubleshooting

### "npm is not recognized"
- Node.js is not installed or not in PATH
- Solution: Reinstall Node.js and restart your computer

### "psql is not recognized"
- PostgreSQL is not installed or not in PATH
- Solution: Reinstall PostgreSQL and add it to PATH

### "Database connection refused"
- PostgreSQL is not running
- Solution: Start PostgreSQL service from Services (Windows)

### "Prisma migrate fails"
- Wrong DATABASE_URL format
- Database doesn't exist
- Solution: Double-check `.env` and verify database was created

## Useful Commands

```bash
# Install all dependencies
npm install

# Run migrations
npm run prisma:migrate

# Open Prisma Studio (visual database editor)
npm run prisma:studio

# Generate Prisma Client
npm run prisma:generate

# Development with auto-reload
npm run dev

# Production mode
npm start
```

## Project Structure

```
RiskCraft/
├── src/
│   └── server.js           # Main Express server
├── prisma/
│   ├── schema.prisma       # Database schema
│   └── migrations/         # Database migration files
├── certs/                  # SSL certificates (generated)
│   ├── cert.pem
│   └── key.pem
├── node_modules/           # Installed packages
├── .env                    # Environment variables (DO NOT COMMIT)
├── .env.example            # Environment template
├── .gitignore              # Git ignore file
├── package.json            # Dependencies
├── package-lock.json       # Lock file
└── README.md               # Project documentation
```

## API Reference

See README.md for complete API documentation with request/response examples.

## Support

For issues or questions, check:
1. README.md - Full API documentation
2. .env configuration - Verify all variables are set
3. PostgreSQL connection - Test with `psql -U postgres`
4. Firewall - Ensure port 3000 is not blocked
