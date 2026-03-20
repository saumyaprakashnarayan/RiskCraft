package com.example.riskcraft.network

data class MarketResponse(
    val c: Double,   // current price
    val d: Double,   // change
    val dp: Double,  // percent change
    val h: Double,   // high price of the day
    val l: Double,   // low price of the day
    val o: Double,   // open price of the day
    val pc: Double   // previous close price
)
