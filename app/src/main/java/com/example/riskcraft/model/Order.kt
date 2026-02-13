package com.example.riskcraft.model

data class Order(
    val symbol: String,
    val side: String,
    val quantity: Int,
    val price: Double,
    val status: String
)
