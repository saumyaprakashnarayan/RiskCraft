require('dotenv').config();
const express = require('express');
const https = require('https');
const fs = require('fs');
const cors = require('cors');
const jwt = require('jsonwebtoken');
const { PrismaClient } = require('@prisma/client');

// Import routes
const authRoutes = require('./routes/auth');
const tradeRoutes = require('./routes/trades');
const gamificationRoutes = require('./routes/gamification');

const prisma = new PrismaClient();
const app = express();

// Middleware
app.use(cors());
app.use(express.json());

// Middleware: Verify JWT Token
const verifyToken = (req, res, next) => {
  const token = req.headers.authorization?.split(' ')[1];

  if (!token) {
    return res.status(401).json({
      success: false,
      message: 'No token provided',
    });
  }

  try {
    const decoded = jwt.verify(token, process.env.JWT_SECRET);
    req.userId = decoded.id;
    next();
  } catch (error) {
    return res.status(401).json({
      success: false,
      message: 'Invalid or expired token',
    });
  }
};

// Routes
app.use('/api/auth', authRoutes);
app.use('/api/trades', verifyToken, tradeRoutes);
app.use('/api/gamification', verifyToken, gamificationRoutes);

// Health check
app.get('/health', (req, res) => {
  res.status(200).json({
    success: true,
    message: 'Server is running',
  });
});

// Start Server
const PORT = process.env.PORT || 3000;

// Check if SSL certificates exist
const certPath = './certs/cert.pem';
const keyPath = './certs/key.pem';

if (fs.existsSync(certPath) && fs.existsSync(keyPath)) {
  // HTTPS Server
  const options = {
    cert: fs.readFileSync(certPath),
    key: fs.readFileSync(keyPath),
  };

  https.createServer(options, app).listen(PORT, () => {
    console.log(`HTTPS Server running on https://localhost:${PORT}`);
  });
} else {
  // HTTP Server (for development)
  app.listen(PORT, () => {
    console.log(`HTTP Server running on http://localhost:${PORT}`);
    console.log('To enable HTTPS, generate SSL certificates and place them in ./certs/ directory');
    console.log('Command: mkdir certs && openssl req -nodes -new -x509 -keyout certs/key.pem -out certs/cert.pem -days 365');
  });
}

// Graceful shutdown
process.on('SIGINT', async () => {
  console.log('\nShutting down gracefully...');
  await prisma.$disconnect();
  process.exit(0);
});
