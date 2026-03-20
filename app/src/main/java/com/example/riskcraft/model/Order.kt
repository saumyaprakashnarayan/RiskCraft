package com.example.riskcraft.model

data class Order(
    val symbol: String,
    val side: String,      // BUY / SELL
    val quantity: Int,
    val price: Double,
    val status: String,    // Completed / Pending / Rejected
    val timestamp: Long = System.currentTimeMillis(),
    val charges: Double = 0.0,       // total brokerage + taxes
    val brokerage: Double = 0.0,
    val stt: Double = 0.0,
    val exchangeCharges: Double = 0.0,
    val gst: Double = 0.0,
    val sebiCharges: Double = 0.0,
    val stampDuty: Double = 0.0
)
