package com.example.riskcraft

import android.content.Context
import com.example.riskcraft.model.Order
import com.example.riskcraft.model.Position
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object WalletManager {

    private const val PREFS_NAME = "riskcraft_wallet"
    private const val KEY_BALANCE = "rc_balance"
    private const val KEY_POSITIONS = "positions"
    private const val KEY_ORDERS = "orders"
    const val STARTING_BALANCE = 1000000.0 // 10 Lakh RC Coins

    private val gson = Gson()

    fun getBalance(context: Context): Double {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (!prefs.contains(KEY_BALANCE)) {
            prefs.edit().putFloat(KEY_BALANCE, STARTING_BALANCE.toFloat()).apply()
        }
        return prefs.getFloat(KEY_BALANCE, STARTING_BALANCE.toFloat()).toDouble()
    }

    fun updateBalance(context: Context, newBalance: Double) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putFloat(KEY_BALANCE, newBalance.toFloat()).apply()
    }

    fun getPositions(context: Context): MutableList<Position> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_POSITIONS, "[]")
        val type = object : TypeToken<MutableList<Position>>() {}.type
        return try {
            gson.fromJson(json, type) ?: mutableListOf()
        } catch (e: Exception) {
            mutableListOf()
        }
    }

    fun savePositions(context: Context, positions: List<Position>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_POSITIONS, gson.toJson(positions)).apply()
    }

    fun getOrders(context: Context): MutableList<Order> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_ORDERS, "[]")
        val type = object : TypeToken<MutableList<Order>>() {}.type
        return try {
            gson.fromJson(json, type) ?: mutableListOf()
        } catch (e: Exception) {
            mutableListOf()
        }
    }

    fun addOrder(context: Context, order: Order) {
        val orders = getOrders(context)
        orders.add(0, order)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_ORDERS, gson.toJson(orders)).apply()
    }

    fun executeBuy(context: Context, symbol: String, name: String, quantity: Int, price: Double): Boolean {
        return executeBuyWithCharges(context, symbol, name, quantity, price, 0.0)
    }

    fun executeBuyWithCharges(context: Context, symbol: String, name: String, quantity: Int, price: Double, charges: Double, chargesDetail: TradeActivity.ChargesBreakdown? = null): Boolean {
        val totalCost = quantity * price + charges
        val balance = getBalance(context)

        if (totalCost > balance) return false

        updateBalance(context, balance - totalCost)

        val positions = getPositions(context)
        val existing = positions.find { it.symbol == symbol }
        if (existing != null) {
            val newQty = existing.quantity + quantity
            val newAvg = ((existing.avgPrice * existing.quantity) + (price * quantity)) / newQty
            val index = positions.indexOf(existing)
            positions[index] = Position(symbol, name, newQty, newAvg, price)
        } else {
            positions.add(Position(symbol, name, quantity, price, price))
        }
        savePositions(context, positions)

        val order = if (chargesDetail != null) {
            Order(symbol, "BUY", quantity, price, "Completed", System.currentTimeMillis(),
                charges, chargesDetail.brokerage, chargesDetail.stt,
                chargesDetail.exchangeCharges, chargesDetail.gst,
                chargesDetail.sebiCharges, chargesDetail.stampDuty)
        } else {
            Order(symbol, "BUY", quantity, price, "Completed", System.currentTimeMillis())
        }
        addOrder(context, order)

        return true
    }

    fun executeSell(context: Context, symbol: String, name: String, quantity: Int, price: Double): Boolean {
        return executeSellWithCharges(context, symbol, name, quantity, price, 0.0)
    }

    fun executeSellWithCharges(context: Context, symbol: String, name: String, quantity: Int, price: Double, charges: Double, chargesDetail: TradeActivity.ChargesBreakdown? = null): Boolean {
        val positions = getPositions(context)
        val existing = positions.find { it.symbol == symbol } ?: return false

        if (quantity > existing.quantity) return false

        val totalRevenue = quantity * price - charges
        val balance = getBalance(context)
        updateBalance(context, balance + totalRevenue)

        val newQty = existing.quantity - quantity
        val index = positions.indexOf(existing)
        if (newQty == 0) {
            positions.removeAt(index)
        } else {
            positions[index] = Position(symbol, existing.name, newQty, existing.avgPrice, price)
        }
        savePositions(context, positions)

        val order = if (chargesDetail != null) {
            Order(symbol, "SELL", quantity, price, "Completed", System.currentTimeMillis(),
                charges, chargesDetail.brokerage, chargesDetail.stt,
                chargesDetail.exchangeCharges, chargesDetail.gst,
                chargesDetail.sebiCharges, chargesDetail.stampDuty)
        } else {
            Order(symbol, "SELL", quantity, price, "Completed", System.currentTimeMillis())
        }
        addOrder(context, order)

        return true
    }

    fun getInvestedValue(context: Context): Double {
        return getPositions(context).sumOf { it.avgPrice * it.quantity }
    }

    fun getCurrentValue(context: Context): Double {
        return getPositions(context).sumOf { it.ltp * it.quantity }
    }

    fun getTotalPnL(context: Context): Double {
        return getCurrentValue(context) - getInvestedValue(context)
    }

    fun resetWallet(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }
}
