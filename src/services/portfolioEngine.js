const { PrismaClient } = require('@prisma/client');

const prisma = new PrismaClient();

/*
 * Calculate portfolio from trades
 * Returns current holdings, average price, and unrealized P&L
 */
const getPortfolio = async (userId) => {
  try {
    const trades = await prisma.trade.findMany({
      where: { userId },
      orderBy: { createdAt: 'asc' },
    });

    if (trades.length === 0) {
      return {};
    }

    const portfolio = {};

    // Group trades by asset and calculate positions
    trades.forEach((trade) => {
      if (!portfolio[trade.asset]) {
        portfolio[trade.asset] = {
          asset: trade.asset,
          totalQuantity: 0,
          totalCost: 0,
          averagePrice: 0,
          buys: [],
          sells: [],
        };
      }

      const position = portfolio[trade.asset];
      const tradeValue = parseFloat(trade.quantity) * parseFloat(trade.price);

      if (trade.tradeType === 'BUY') {
        position.totalQuantity += parseFloat(trade.quantity);
        position.totalCost += tradeValue;
        position.averagePrice = position.totalCost / position.totalQuantity;
        position.buys.push({
          quantity: parseFloat(trade.quantity),
          price: parseFloat(trade.price),
          date: trade.createdAt,
        });
      } else if (trade.tradeType === 'SELL') {
        position.totalQuantity -= parseFloat(trade.quantity);
        position.totalCost -= tradeValue;
        if (position.totalQuantity > 0) {
          position.averagePrice = position.totalCost / position.totalQuantity;
        }
        position.sells.push({
          quantity: parseFloat(trade.quantity),
          price: parseFloat(trade.price),
          date: trade.createdAt,
        });
      }
    });

    // Filter out closed positions
    const activePositions = Object.values(portfolio).filter((p) => p.totalQuantity > 0);

    return activePositions.map((position) => ({
      asset: position.asset,
      netQuantity: position.totalQuantity,
      averagePrice: Math.round(position.averagePrice * 100) / 100,
      buys: position.buys,
      sells: position.sells,
    }));
  } catch (error) {
    throw new Error(`Portfolio calculation failed: ${error.message}`);
  }
};

/**
 * Calculate unrealized P&L for specific position
 */
const calculateUnrealizedPnL = (netQuantity, averagePrice, currentPrice) => {
  const pnl = (parseFloat(currentPrice) - parseFloat(averagePrice)) * parseFloat(netQuantity);
  const pnlPercent = ((parseFloat(currentPrice) - parseFloat(averagePrice)) / parseFloat(averagePrice)) * 100;

  return {
    pnl: Math.round(pnl * 100) / 100,
    pnlPercent: Math.round(pnlPercent * 100) / 100,
  };
};

/**
 * Calculate realized P&L from closed trades
 */
const calculateRealizedPnL = async (userId, asset = null) => {
  try {
    let trades = await prisma.trade.findMany({
      where: asset ? { userId, asset } : { userId },
      orderBy: { createdAt: 'asc' },
    });

    let realizedPnL = 0;
    let totalVolume = 0;

    const positions = {};

    trades.forEach((trade) => {
      if (!positions[trade.asset]) {
        positions[trade.asset] = [];
      }

      if (trade.tradeType === 'BUY') {
        positions[trade.asset].push({
          quantity: parseFloat(trade.quantity),
          price: parseFloat(trade.price),
        });
      } else if (trade.tradeType === 'SELL') {
        const sellQuantity = parseFloat(trade.quantity);
        const sellPrice = parseFloat(trade.price);
        let remaining = sellQuantity;

        // FIFO: Match sell with oldest buy
        while (remaining > 0 && positions[trade.asset].length > 0) {
          const buy = positions[trade.asset][0];

          if (buy.quantity <= remaining) {
            const soldQuantity = buy.quantity;
            const pnl = (sellPrice - buy.price) * soldQuantity;
            realizedPnL += pnl;
            totalVolume += sellPrice * soldQuantity;
            remaining -= soldQuantity;
            positions[trade.asset].shift();
          } else {
            const pnl = (sellPrice - buy.price) * remaining;
            realizedPnL += pnl;
            totalVolume += sellPrice * remaining;
            buy.quantity -= remaining;
            remaining = 0;
          }
        }
      }
    });

    return {
      realizedPnL: Math.round(realizedPnL * 100) / 100,
      totalVolume: Math.round(totalVolume * 100) / 100,
    };
  } catch (error) {
    throw new Error(`Realized P&L calculation failed: ${error.message}`);
  }
};

/**
 * Calculate portfolio diversity score
 */
const calculateDiversityScore = async (userId) => {
  try {
    const portfolio = await getPortfolio(userId);
    
    if (portfolio.length === 0) {
      return 0;
    }

    // Simple diversity: penalize concentration in single asset
    const diversityScore = Math.min(100, portfolio.length * 20);
    return Math.round(diversityScore);
  } catch (error) {
    throw new Error(`Diversity calculation failed: ${error.message}`);
  }
};

module.exports = {
  getPortfolio,
  calculateUnrealizedPnL,
  calculateRealizedPnL,
  calculateDiversityScore,
};
