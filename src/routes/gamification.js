const express = require('express');
const gamificationEngine = require('../services/gamificationEngine');

const router = express.Router();

/*
 * GET /api/gamification/badges
 * Get user's badges
 */
router.get('/badges', async (req, res) => {
  try {
    const userId = req.userId;

    const badges = await gamificationEngine.getUserBadges(userId);

    res.status(200).json({
      success: true,
      message: 'Badges retrieved successfully',
      data: {
        badges: badges.map((ub) => ({
          id: ub.badge.id,
          name: ub.badge.name,
          description: ub.badge.description,
          icon: ub.badge.icon,
          earnedAt: ub.earnedAt,
        })),
        totalBadges: badges.length,
      },
    });
  } catch (error) {
    console.error('Badges error:', error);
    res.status(500).json({
      success: false,
      message: error.message || 'Failed to retrieve badges',
    });
  }
});

/**
 * POST /api/gamification/challenges/:challengeId/complete
 * Complete a challenge
 */
router.post('/challenges/:challengeId/complete', async (req, res) => {
  try {
    const userId = req.userId;
    const challengeId = parseInt(req.params.challengeId);

    if (!challengeId) {
      return res.status(400).json({
        success: false,
        message: 'Challenge ID is required',
      });
    }

    const completedChallenge = await gamificationEngine.completeChallenge(
      userId,
      challengeId
    );

    res.status(200).json({
      success: true,
      message: 'Challenge completed successfully',
      data: {
        challenge: {
          id: completedChallenge.id,
          status: completedChallenge.status,
          completedAt: completedChallenge.completedAt,
        },
      },
    });
  } catch (error) {
    console.error('Challenge completion error:', error);
    res.status(400).json({
      success: false,
      message: error.message || 'Failed to complete challenge',
    });
  }
});

/**
 * GET /api/gamification/leaderboard/:scoreType
 * Get leaderboard for specific score type
 */
router.get('/leaderboard/:scoreType', async (req, res) => {
  try {
    const scoreType = req.params.scoreType.toLowerCase();
    const limit = Math.min(parseInt(req.query.limit) || 50, 100);

    if (!['risk', 'consistency', 'discipline'].includes(scoreType)) {
      return res.status(400).json({
        success: false,
        message: 'scoreType must be risk, consistency, or discipline',
      });
    }

    const leaderboard = await gamificationEngine.getLeaderboard(scoreType, limit);

    res.status(200).json({
      success: true,
      message: `${scoreType} leaderboard retrieved successfully`,
      data: {
        scoreType,
        leaderboard,
      },
    });
  } catch (error) {
    console.error('Leaderboard error:', error);
    res.status(500).json({
      success: false,
      message: error.message || 'Failed to retrieve leaderboard',
    });
  }
});

/**
 * GET /api/gamification/stats
 * Get user's gamification stats
 */
router.get('/stats', async (req, res) => {
  try {
    const userId = req.userId;

    const badges = await gamificationEngine.getUserBadges(userId);
    const leaderboards = await gamificationEngine.getLeaderboard('consistency');

    // Find user's rank
    const userRank = leaderboards.find((entry) => entry.user.id === userId);

    res.status(200).json({
      success: true,
      message: 'Gamification stats retrieved successfully',
      data: {
        totalBadges: badges.length,
        rank: userRank ? userRank.rank : 'N/A',
        recentBadges: badges.slice(0, 5).map((ub) => ({
          name: ub.badge.name,
          earnedAt: ub.earnedAt,
        })),
      },
    });
  } catch (error) {
    console.error('Stats error:', error);
    res.status(500).json({
      success: false,
      message: error.message || 'Failed to retrieve stats',
    });
  }
});

module.exports = router;
