const express = require('express');
const tradeEngine = require('../services/tradeEngine');
const portfolioEngine = require('../services/portfolioEngine');
const gamificationEngine = require('../services/gamificationEngine');

const router = express.Router();

/**
 * POST /api/trades/execute
 * Execute a trade (BUY or SELL)
 */
router.post('/execute', async (req, res) => {
  try {
    const userId = req.userId;
    const { asset, quantity, price, tradeType, stopLoss } = req.body;

    // Validation
    if (!asset || !quantity || !price || !tradeType) {
      return res.status(400).json({
        success: false,
        message: 'Asset, quantity, price, and tradeType are required',
      });
    }

    if (!['BUY', 'SELL'].includes(tradeType)) {
      return res.status(400).json({
        success: false,
        message: 'tradeType must be BUY or SELL',
      });
    }

    // Execute trade
    const { trade, newBalance } = await tradeEngine.executeTrade(
      userId,
      asset,
      quantity,
      price,
      tradeType,
      stopLoss
    );

    // Award XP for trade
    await gamificationEngine.awardXP(userId, 10, `${tradeType} ${asset}`);

    // Check badges
    await gamificationEngine.checkRiskDisciplineBadge(userId);

    res.status(201).json({
      success: true,
      message: `${tradeType} trade executed successfully`,
      data: {
        trade: {
          id: trade.id,
          asset: trade.asset,
          quantity: parseFloat(trade.quantity),
          price: parseFloat(trade.price),
          tradeType: trade.tradeType,
          stopLoss: trade.stopLoss ? parseFloat(trade.stopLoss) : null,
          createdAt: trade.createdAt,
        },
        newBalance,
      },
    });
  } catch (error) {
    console.error('Trade execution error:', error);
    res.status(400).json({
      success: false,
      message: error.message || 'Trade execution failed',
    });
  }
});

/**
 * GET /api/trades/history
 * Get user's trade history
 */
router.get('/history', async (req, res) => {
  try {
    const userId = req.userId;
    const limit = Math.min(parseInt(req.query.limit) || 50, 100);
    const offset = parseInt(req.query.offset) || 0;

    const trades = await tradeEngine.getTradeHistory(userId, limit, offset);
    const totalCount = await tradeEngine.getTotalTradesCount(userId);

    res.status(200).json({
      success: true,
      message: 'Trade history retrieved successfully',
      data: {
        trades: trades.map((trade) => ({
          id: trade.id,
          asset: trade.asset,
          quantity: parseFloat(trade.quantity),
          price: parseFloat(trade.price),
          tradeType: trade.tradeType,
          stopLoss: trade.stopLoss ? parseFloat(trade.stopLoss) : null,
          createdAt: trade.createdAt,
        })),
        pagination: {
          limit,
          offset,
          total: totalCount,
        },
      },
    });
  } catch (error) {
    console.error('Trade history error:', error);
    res.status(500).json({
      success: false,
      message: error.message || 'Failed to retrieve trade history',
    });
  }
});

/**
 * GET /api/trades/asset/:asset
 * Get trades for specific asset
 */
router.get('/asset/:asset', async (req, res) => {
  try {
    const userId = req.userId;
    const asset = req.params.asset.toUpperCase();

    const trades = await tradeEngine.getAssetTrades(userId, asset);

    res.status(200).json({
      success: true,
      message: `Trades for ${asset} retrieved successfully`,
      data: {
        asset,
        trades: trades.map((trade) => ({
          id: trade.id,
          quantity: parseFloat(trade.quantity),
          price: parseFloat(trade.price),
          tradeType: trade.tradeType,
          stopLoss: trade.stopLoss ? parseFloat(trade.stopLoss) : null,
          createdAt: trade.createdAt,
        })),
      },
    });
  } catch (error) {
    console.error('Asset trades error:', error);
    res.status(500).json({
      success: false,
      message: error.message || 'Failed to retrieve asset trades',
    });
  }
});

/**
 * GET /api/trades/portfolio
 * Get user's current portfolio
 */
router.get('/portfolio', async (req, res) => {
  try {
    const userId = req.userId;

    const portfolio = await portfolioEngine.getPortfolio(userId);
    const realizedPnL = await portfolioEngine.calculateRealizedPnL(userId);
    const diversityScore = await portfolioEngine.calculateDiversityScore(userId);

    res.status(200).json({
      success: true,
      message: 'Portfolio retrieved successfully',
      data: {
        portfolio: portfolio.map((position) => ({
          asset: position.asset,
          netQuantity: position.netQuantity,
          averagePrice: position.averagePrice,
        })),
        realizedPnL: realizedPnL.realizedPnL,
        totalVolume: realizedPnL.totalVolume,
        diversityScore,
      },
    });
  } catch (error) {
    console.error('Portfolio error:', error);
    res.status(500).json({
      success: false,
      message: error.message || 'Failed to retrieve portfolio',
    });
  }
});

module.exports = router;
