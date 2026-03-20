package com.example.riskcraft.model

data class Challenge(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val targetProfit: Double = 0.0,
    val days: Int = 0,
    val reward: String = "",
    val type: String = "PROFIT" // PROFIT, TRADES, etc.
)
