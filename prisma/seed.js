// prisma/seed.js
const { PrismaClient } = require('@prisma/client');

const prisma = new PrismaClient();

async function main() {
  console.log('Starting seed...');

  // Create badges
  const badges = await prisma.badge.createMany({
    data: [
      {
        name: 'Risk Disciplinarian',
        description: 'Completed 10 trades with stop-loss orders',
        icon: '🛡️',
      },
      {
        name: 'Consistency Champion',
        description: '5 consecutive days of trading',
        icon: '📈',
      },
      {
        name: 'First Trade',
        description: 'Execute your first trade',
        icon: '🚀',
      },
      {
        name: 'Portfolio Master',
        description: 'Hold 10 different assets',
        icon: '💎',
      },
      {
        name: 'Profit Pioneer',
        description: 'Achieve 100% realized profit',
        icon: '💰',
      },
      {
        name: 'Disciplined Trader',
        description: 'Complete 5 daily challenges',
        icon: '⭐',
      },
      {
        name: 'Risk Manager',
        description: 'Maintain a loss under 5% on daily basis',
        icon: '🎯',
      },
      {
        name: 'Trading Legend',
        description: 'Reach level 50',
        icon: '👑',
      },
    ],
    skipDuplicates: true,
  });

  console.log(`Created ${badges.count} badges`);

  // Create daily challenges
  const challenges = await prisma.challenge.createMany({
    data: [
      {
        name: 'Daily Trader',
        description: 'Execute at least 1 trade',
        rewardXp: 10,
        isActive: true,
      },
      {
        name: 'Multiple Trades',
        description: 'Execute at least 5 trades',
        rewardXp: 25,
        isActive: true,
      },
      {
        name: 'Diversify Portfolio',
        description: 'Hold at least 3 different assets',
        rewardXp: 20,
        isActive: true,
      },
      {
        name: 'Profit Target',
        description: 'Achieve $500 in realized profit',
        rewardXp: 50,
        isActive: true,
      },
      {
        name: 'Risk Control',
        description: 'Execute 10 trades with stop-loss',
        rewardXp: 30,
        isActive: true,
      },
      {
        name: 'No Loss Day',
        description: 'Complete a trading day with no losses',
        rewardXp: 40,
        isActive: true,
      },
    ],
    skipDuplicates: true,
  });

  console.log(`Created ${challenges.count} challenges`);

  console.log('Seed completed!');
}

main()
  .catch((e) => {
    console.error(e);
    process.exit(1);
  })
  .finally(async () => {
    await prisma.$disconnect();
  });
