# Auth Server - Express + JWT + PostgreSQL + Prisma

A secure HTTPS Express server with user authentication (login/signup), JWT tokens, and PostgreSQL database using Prisma ORM.

## Features

- ✅ User Registration (Signup) with username, email, and password
- ✅ User Login with email/username and password
- ✅ JWT Token-based Authentication
- ✅ Password Hashing with bcryptjs
- ✅ PostgreSQL Database with Prisma ORM
- ✅ HTTPS Support
- ✅ Protected Routes
- ✅ CORS Enabled

## Prerequisites

- Node.js (v14 or higher)
- PostgreSQL database
- npm or yarn

## Installation

1. **Install dependencies:**
   ```bash
   npm install
   ```

2. **Set up environment variables:**
   ```bash
   cp .env.example .env
   ```
   Update `.env` with your database credentials and JWT secret:
   ```
   DATABASE_URL="postgresql://USER:PASSWORD@localhost:5432/riskcraft_db"
   JWT_SECRET="your-secure-secret-key"
   JWT_EXPIRE="7d"
   PORT=3000
   NODE_ENV="development"
   ```

3. **Setup Prisma and Database:**
   ```bash
   npx prisma migrate dev --name init
   ```

4. **(Optional) Generate SSL Certificates for HTTPS:**
   ```bash
   mkdir certs
   openssl req -nodes -new -x509 -keyout certs/key.pem -out certs/cert.pem -days 365
   ```

## Running the Server

### Development Mode
```bash
npm run dev
```

### Production Mode
```bash
npm start
```

The server will run on `https://localhost:3000` (with certificates) or `http://localhost:3000` (without certificates).

## API Endpoints

### 1. Signup (Register)
**POST** `/api/auth/signup`

Request Body:
```json
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "securepassword123"
}
```

Response:
```json
{
  "message": "User registered successfully",
  "user": {
    "id": 1,
    "username": "john_doe",
    "email": "john@example.com"
  },
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### 2. Login
**POST** `/api/auth/login`

Request Body (with email):
```json
{
  "email": "john@example.com",
  "password": "securepassword123"
}
```

Or with username:
```json
{
  "username": "john_doe",
  "password": "securepassword123"
}
```

Response:
```json
{
  "message": "Login successful",
  "user": {
    "id": 1,
    "username": "john_doe",
    "email": "john@example.com"
  },
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### 3. Get Profile (Protected)
**GET** `/api/auth/profile`

Headers:
```
Authorization: Bearer <token>
```

Response:
```json
{
  "message": "Profile retrieved successfully",
  "user": {
    "id": 1,
    "username": "john_doe",
    "email": "john@example.com",
    "createdAt": "2024-02-06T10:30:00.000Z"
  }
}
```

### 4. Health Check
**GET** `/health`

Response:
```json
{
  "message": "Server is running"
}
```

## Project Structure

```
.
├── src/
│   └── server.js           # Main server file
├── prisma/
│   └── schema.prisma       # Prisma database schema
├── certs/                  # SSL certificates (optional)
│   ├── cert.pem
│   └── key.pem
├── .env                    # Environment variables (create from .env.example)
├── .env.example            # Environment variables template
├── package.json
└── README.md
```

## Database Schema

The `User` model includes:
- `id`: Auto-incrementing primary key
- `email`: Unique email address
- `username`: Unique username
- `password`: Hashed password
- `createdAt`: User creation timestamp
- `updatedAt`: Last update timestamp

## Security Features

- 🔒 Password hashing with bcryptjs (10 salt rounds)
- 🔑 JWT tokens with expiration
- 📋 Email and username uniqueness constraints
- 🛡️ HTTPS support
- ✔️ Input validation
- 🚫 Protected routes with token verification

## Testing with cURL

**Signup:**
```bash
curl -X POST https://localhost:3000/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"test@example.com","password":"password123"}'
```

**Login:**
```bash
curl -X POST https://localhost:3000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}'
```

**Get Profile:**
```bash
curl -X GET https://localhost:3000/api/auth/profile \
  -H "Authorization: Bearer <your-token>"
```

## Useful Commands

```bash
# Run migrations
npm run prisma:migrate

# Generate Prisma Client
npm run prisma:generate

# Open Prisma Studio (database GUI)
npm run prisma:studio

# Start development server with auto-reload
npm run dev

# Start production server
npm start
```

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DATABASE_URL` | PostgreSQL connection string | Required |
| `JWT_SECRET` | Secret key for JWT signing | Required |
| `JWT_EXPIRE` | JWT token expiration time | 7d |
| `PORT` | Server port | 3000 |
| `NODE_ENV` | Environment (development/production) | development |

## Error Handling

The server provides meaningful error responses:
- `400`: Bad Request (missing or invalid fields)
- `401`: Unauthorized (invalid credentials or token)
- `409`: Conflict (user already exists)
- `500`: Internal Server Error

## Next Steps

1. Generate SSL certificates for HTTPS
2. Set up PostgreSQL database
3. Configure environment variables
4. Run database migrations
5. Start the server
6. Test endpoints using cURL or Postman

## License

ISC
