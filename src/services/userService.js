const { PrismaClient } = require('@prisma/client');
const bcrypt = require('bcryptjs');

const prisma = new PrismaClient();

/**
 * Create a new user with wallet
 */
const createUser = async (email, name, password) => {
  try {
    const hashedPassword = await bcrypt.hash(password, 10);

    const user = await prisma.user.create({
      data: {
        email,
        name,
        password: hashedPassword,
      },
    });

    // Create wallet for user
    await prisma.wallet.create({
      data: {
        userId: user.id,
        balance: 10000, // Initial virtual capital
      },
    });

    return user;
  } catch (error) {
    throw new Error(`Error creating user: ${error.message}`);
  }
};

/**
 * Get user by email
 */
const getUserByEmail = async (email) => {
  return prisma.user.findUnique({
    where: { email },
    include: {
      wallet: true,
    },
  });
};

/**
 * Get user by ID
 */
const getUserById = async (id) => {
  return prisma.user.findUnique({
    where: { id },
    include: {
      wallet: true,
      userBadges: {
        include: {
          badge: true,
        },
      },
    },
  });
};

/**
 * Update user level and XP
 */
const updateUserProgress = async (userId, xpGained) => {
  const user = await prisma.user.findUnique({ where: { id: userId } });
  
  const newXp = user.xp + xpGained;
  const newLevel = Math.floor(newXp / 100) + 1; // Level up every 100 XP

  return prisma.user.update({
    where: { id: userId },
    data: {
      xp: newXp,
      level: newLevel,
    },
  });
};

/**
 * Verify password
 */
const verifyPassword = async (plainPassword, hashedPassword) => {
  return bcrypt.compare(plainPassword, hashedPassword);
};

module.exports = {
  createUser,
  getUserByEmail,
  getUserById,
  updateUserProgress,
  verifyPassword,
};
