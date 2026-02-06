const { PrismaClient } = require('@prisma/client');

const prisma = new PrismaClient();

/*
 * Award badge to user
 */
const awardBadge = async (userId, badgeName) => {
  try {
    // Find badge by name
    const badge = await prisma.badge.findUnique({
      where: { name: badgeName },
    });

    if (!badge) {
      throw new Error(`Badge '${badgeName}' not found`);
    }

    // Check if user already has this badge
    const existingBadge = await prisma.userBadge.findUnique({
      where: {
        userId_badgeId: {
          userId,
          badgeId: badge.id,
        },
      },
    });

    if (existingBadge) {
      return { message: 'User already has this badge' };
    }

    // Award badge
    const userBadge = await prisma.userBadge.create({
      data: {
        userId,
        badgeId: badge.id,
      },
      include: {
        badge: true,
      },
    });

    return userBadge;
  } catch (error) {
    throw new Error(`Badge award failed: ${error.message}`);
  }
};

/**
 * Get user's badges
 */
const getUserBadges = async (userId) => {
  return prisma.userBadge.findMany({
    where: { userId },
    include: {
      badge: true,
    },
    orderBy: { earnedAt: 'desc' },
  });
};

/**
 * Award XP and check for level up
 */
const awardXP = async (userId, xpAmount, reason = 'trade') => {
  try {
    const user = await prisma.user.findUnique({
      where: { id: userId },
    });

    const newXP = user.xp + xpAmount;
    const newLevel = Math.floor(newXP / 100) + 1;
    const leveledUp = newLevel > user.level;

    await prisma.user.update({
      where: { id: userId },
      data: {
        xp: newXP,
        level: newLevel,
      },
    });

    return {
      previousXP: user.xp,
      newXP,
      xpGained: xpAmount,
      previousLevel: user.level,
      newLevel,
      leveledUp,
      reason,
    };
  } catch (error) {
    throw new Error(`XP award failed: ${error.message}`);
  }
};

/**
 * Complete a challenge
 */
const completeChallenge = async (userId, challengeId) => {
  try {
    const challenge = await prisma.challenge.findUnique({
      where: { id: challengeId },
    });

    if (!challenge) {
      throw new Error('Challenge not found');
    }

    // Update challenge status
    const userChallenge = await prisma.userChallenge.update({
      where: {
        userId_challengeId: {
          userId,
          challengeId,
        },
      },
      data: {
        status: 'completed',
        completedAt: new Date(),
      },
    });

    // Award XP
    await awardXP(userId, challenge.rewardXp, `challenge: ${challenge.name}`);

    return userChallenge;
  } catch (error) {
    throw new Error(`Challenge completion failed: ${error.message}`);
  }
};

/**
 * Check if user should get risk discipline badge
 * Criteria: Completed 10 trades with stop-loss orders
 */
const checkRiskDisciplineBadge = async (userId) => {
  try {
    const tradesWithStopLoss = await prisma.trade.findMany({
      where: {
        userId,
        stopLoss: {
          not: null,
        },
      },
    });

    if (tradesWithStopLoss.length >= 10) {
      await awardBadge(userId, 'Risk Disciplinarian');
      return true;
    }
    return false;
  } catch (error) {
    console.error('Risk discipline badge check failed:', error.message);
    return false;
  }
};

/**
 * Check if user should get consistency badge
 * Criteria: 5 consecutive days of trading
 */
const checkConsistencyBadge = async (userId) => {
  try {
    const trades = await prisma.trade.findMany({
      where: { userId },
      orderBy: { createdAt: 'desc' },
    });

    if (trades.length < 5) {
      return false;
    }

    const dates = [...new Set(trades.map((t) => t.createdAt.toDateString()))];
    let consecutiveDays = 1;

    for (let i = 0; i < dates.length - 1; i++) {
      const current = new Date(dates[i]);
      const next = new Date(dates[i + 1]);
      const diffDays = Math.floor((current - next) / (1000 * 60 * 60 * 24));

      if (diffDays === 1) {
        consecutiveDays++;
        if (consecutiveDays >= 5) {
          await awardBadge(userId, 'Consistency Champion');
          return true;
        }
      } else {
        consecutiveDays = 1;
      }
    }

    return false;
  } catch (error) {
    console.error('Consistency badge check failed:', error.message);
    return false;
  }
};

/**
 * Update leaderboard scores
 */
const updateLeaderboardScore = async (userId, scoreType, scoreValue) => {
  try {
    const existing = await prisma.leaderboard.findUnique({
      where: {
        userId_scoreType: {
          userId,
          scoreType,
        },
      },
    });

    if (existing) {
      return prisma.leaderboard.update({
        where: { id: existing.id },
        data: { scoreValue: parseFloat(scoreValue) },
      });
    } else {
      return prisma.leaderboard.create({
        data: {
          userId,
          scoreType,
          scoreValue: parseFloat(scoreValue),
          rank: 1,
        },
      });
    }
  } catch (error) {
    throw new Error(`Leaderboard update failed: ${error.message}`);
  }
};

/**
 * Get leaderboard rankings
 */
const getLeaderboard = async (scoreType, limit = 50) => {
  try {
    const rankings = await prisma.leaderboard.findMany({
      where: { scoreType },
      include: {
        user: {
          select: {
            id: true,
            name: true,
            email: true,
            level: true,
          },
        },
      },
      orderBy: { scoreValue: 'desc' },
      take: limit,
    });

    return rankings.map((entry, index) => ({
      rank: index + 1,
      user: entry.user,
      score: entry.scoreValue,
      scoreType,
    }));
  } catch (error) {
    throw new Error(`Leaderboard fetch failed: ${error.message}`);
  }
};

module.exports = {
  awardBadge,
  getUserBadges,
  awardXP,
  completeChallenge,
  checkRiskDisciplineBadge,
  checkConsistencyBadge,
  updateLeaderboardScore,
  getLeaderboard,
};
