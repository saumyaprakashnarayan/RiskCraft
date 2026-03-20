const { PrismaClient } = require('@prisma/client');

const prisma = new PrismaClient();

/*
 * Execute a trade (BUY or SELL)
 */
const executeTrade = async (userId, asset, quantity, price, tradeType, stopLoss = null) => {
  try {
    // Get wallet
    const wallet = await prisma.wallet.findUnique({
      where: { userId },
    });

    if (!wallet) {
      throw new Error('Wallet not found');
    }

    const tradeAmount = parseFloat(quantity) * parseFloat(price);

    if (tradeType === 'BUY') {
      // Check if user has sufficient balance
      if (wallet.balance < tradeAmount) {
        throw new Error('Insufficient balance for trade');
      }

      // Deduct from balance
      await prisma.wallet.update({
        where: { userId },
        data: {
          balance: {
            decrement: tradeAmount,
          },
        },
      });
    } else if (tradeType === 'SELL') {
      // Add proceeds to balance
      await prisma.wallet.update({
        where: { userId },
        data: {
          balance: {
            increment: tradeAmount,
          },
        },
      });
    }

    // Create trade record
    const trade = await prisma.trade.create({
      data: {
        userId,
        asset,
        quantity: parseFloat(quantity),
        price: parseFloat(price),
        tradeType,
        stopLoss: stopLoss ? parseFloat(stopLoss) : null,
      },
    });

    return {
      trade,
      newBalance: tradeType === 'BUY' 
        ? wallet.balance - tradeAmount 
        : wallet.balance + tradeAmount,
    };
  } catch (error) {
    throw new Error(`Trade execution failed: ${error.message}`);
  }
};

/**
 * Get user's trade history
 */
const getTradeHistory = async (userId, limit = 50, offset = 0) => {
  return prisma.trade.findMany({
    where: { userId },
    orderBy: { createdAt: 'desc' },
    take: limit,
    skip: offset,
  });
};

/**
 * Get trades for specific asset
 */
const getAssetTrades = async (userId, asset) => {
  return prisma.trade.findMany({
    where: {
      userId,
      asset,
    },
    orderBy: { createdAt: 'asc' },
  });
};

/**
 * Calculate total trades count
 */
const getTotalTradesCount = async (userId) => {
  return prisma.trade.count({
    where: { userId },
  });
};

/**
 * Update wallet daily loss
 */
const updateDailyLoss = async (userId, lossAmount) => {
  return prisma.wallet.update({
    where: { userId },
    data: {
      dailyLoss: {
        increment: parseFloat(lossAmount),
      },
    },
  });
};

/**
 * Reset daily loss (typically at end of day)
 */
const resetDailyLoss = async (userId) => {
  return prisma.wallet.update({
    where: { userId },
    data: {
      dailyLoss: 0,
    },
  });
};

module.exports = {
  executeTrade,
  getTradeHistory,
  getAssetTrades,
  getTotalTradesCount,
  updateDailyLoss,
  resetDailyLoss,
};
