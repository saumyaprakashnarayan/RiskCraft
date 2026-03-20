package com.example.riskcraft.model

data class Position(
    val symbol: String,
    val name: String = "",
    val quantity: Int,
    val avgPrice: Double,
    val ltp: Double // Last traded price
)
