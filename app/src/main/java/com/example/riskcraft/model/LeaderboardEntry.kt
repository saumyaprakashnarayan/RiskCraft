package com.example.riskcraft.model

data class LeaderboardEntry(
    val uid: String = "",
    val name: String = "",
    val profileImageUrl: String = "",
    val profit: Double = 0.0,
    val rank: Int = 0
)
