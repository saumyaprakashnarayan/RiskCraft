package com.example.riskcraft

import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.riskcraft.network.ApiClient
import com.example.riskcraft.network.MarketResponse
import com.github.mikephil.charting.charts.CandleStickChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.CandleData
import com.github.mikephil.charting.data.CandleDataSet
import com.github.mikephil.charting.data.CandleEntry
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TradeActivity : AppCompatActivity() {

    private var currentPrice = 0.0
    private var stockSymbol = ""
    private var stockName = ""
    private var isCrypto = false
    private var isIndex = false
    private var apiSymbol = ""

    // Brokerage & Tax rates (simulated realistic Indian market charges)
    companion object {
        const val BROKERAGE_PERCENT = 0.03     // 0.03% brokerage
        const val STT_PERCENT = 0.1            // 0.1% STT (Securities Transaction Tax) on sell
        const val EXCHANGE_TXN_PERCENT = 0.00345 // Exchange transaction charges
        const val GST_PERCENT = 18.0           // 18% GST on brokerage + exchange charges
        const val SEBI_CHARGE_PER_CRORE = 10.0 // ₹10 per crore
        const val STAMP_DUTY_PERCENT = 0.015   // 0.015% stamp duty on buy
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trade)

        stockSymbol = intent.getStringExtra("symbol") ?: "RELIANCE"
        stockName = intent.getStringExtra("name") ?: "Reliance Industries"
        currentPrice = intent.getDoubleExtra("price", 0.0)
        isCrypto = intent.getBooleanExtra("isCrypto", false)
        isIndex = intent.getBooleanExtra("isIndex", false)
        apiSymbol = intent.getStringExtra("apiSymbol") ?: ""

        if (currentPrice == 0.0 && !isCrypto) {
            currentPrice = IndianMarketData.getStockPrice(stockSymbol)
        }

        setupUI()
        setupCandlestickChart()

        if (isCrypto && apiSymbol.isNotEmpty()) {
            fetchCryptoPrice()
        }
    }

    private fun fetchCryptoPrice() {
        val apiKey = "d62id0pr01qlugeq660gd62id0pr01qlugeq6610"
        ApiClient.api.getQuote(apiSymbol, apiKey).enqueue(object : Callback<MarketResponse> {
            override fun onResponse(call: Call<MarketResponse>, response: Response<MarketResponse>) {
                val data = response.body()
                if (data != null && data.c != 0.0) {
                    currentPrice = data.c
                    val priceText = findViewById<TextView>(R.id.stockPrice)
                    val changeText = findViewById<TextView>(R.id.stockChange)
                    priceText?.text = String.format("$%,.2f", currentPrice)
                    val isPos = data.dp >= 0
                    changeText?.text = String.format("%s%.2f%%", if (isPos) "+" else "", data.dp)
                    changeText?.setTextColor(if (isPos) 0xFF10B981.toInt() else 0xFFEF4444.toInt())
                    setupCandlestickChart() // Refresh chart with real price
                }
            }
            override fun onFailure(call: Call<MarketResponse>, t: Throwable) {}
        })
    }

    // ==================== CANDLESTICK CHART ====================
    private fun setupCandlestickChart() {
        val chart = findViewById<CandleStickChart>(R.id.candleChart) ?: return
        if (currentPrice <= 0) return

        val basePrice = currentPrice
        val entries = mutableListOf<CandleEntry>()

        // Generate 30 simulated candles based on the current price
        var prevClose = basePrice * 0.97 // Start ~3% below for visual range
        for (i in 0 until 30) {
            val volatility = basePrice * 0.015 // 1.5% volatility
            val open = prevClose + (Math.random() - 0.48) * volatility
            val close = open + (Math.random() - 0.47) * volatility
            val high = maxOf(open, close) + Math.random() * volatility * 0.5
            val low = minOf(open, close) - Math.random() * volatility * 0.5

            entries.add(
                CandleEntry(
                    i.toFloat(),
                    high.toFloat(),
                    low.toFloat(),
                    open.toFloat(),
                    close.toFloat()
                )
            )
            prevClose = close
        }

        val dataSet = CandleDataSet(entries, "").apply {
            // Green candles (bullish - close > open)
            increasingColor = Color.parseColor("#10B981")
            increasingPaintStyle = Paint.Style.FILL

            // Red candles (bearish - close < open)
            decreasingColor = Color.parseColor("#EF4444")
            decreasingPaintStyle = Paint.Style.FILL

            // Wick (shadow) color
            shadowColorSameAsCandle = true
            shadowWidth = 1.5f

            // Neutral candle
            neutralColor = Color.parseColor("#6B7280")

            // No value labels on candles
            setDrawValues(false)

            // Bar width
            barSpace = 0.1f
        }

        chart.apply {
            data = CandleData(dataSet)
            description.isEnabled = false
            legend.isEnabled = false

            // Background
            setBackgroundColor(Color.TRANSPARENT)
            setDrawGridBackground(false)

            // X Axis
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                textColor = Color.parseColor("#6B7280")
                textSize = 10f
                setDrawLabels(false)
            }

            // Left Y Axis
            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = Color.parseColor("#1E2642")
                textColor = Color.parseColor("#9CA3AF")
                textSize = 10f
            }

            // Right Y Axis
            axisRight.isEnabled = false

            // Touch interactions
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)

            // Animate
            animateX(800)
            invalidate()
        }
    }

    // ==================== BROKERAGE & TAX CALCULATOR ====================
    data class ChargesBreakdown(
        val brokerage: Double,
        val stt: Double,
        val exchangeCharges: Double,
        val gst: Double,
        val sebiCharges: Double,
        val stampDuty: Double,
        val totalCharges: Double,
        val netAmount: Double
    )

    private fun calculateSellCharges(quantity: Int, price: Double): ChargesBreakdown {
        val turnover = quantity * price

        val brokerage = minOf(turnover * BROKERAGE_PERCENT / 100, 20.0) // Max ₹20 per order
        val stt = turnover * STT_PERCENT / 100
        val exchangeCharges = turnover * EXCHANGE_TXN_PERCENT / 100
        val gst = (brokerage + exchangeCharges) * GST_PERCENT / 100
        val sebiCharges = turnover * SEBI_CHARGE_PER_CRORE / 10000000.0
        val stampDuty = 0.0 // Stamp duty is only on buy side

        val totalCharges = brokerage + stt + exchangeCharges + gst + sebiCharges + stampDuty
        val netAmount = turnover - totalCharges

        return ChargesBreakdown(brokerage, stt, exchangeCharges, gst, sebiCharges, stampDuty, totalCharges, netAmount)
    }

    private fun calculateBuyCharges(quantity: Int, price: Double): ChargesBreakdown {
        val turnover = quantity * price

        val brokerage = minOf(turnover * BROKERAGE_PERCENT / 100, 20.0)
        val stt = 0.0 // STT on delivery buy is 0.1% but we keep it simple
        val exchangeCharges = turnover * EXCHANGE_TXN_PERCENT / 100
        val gst = (brokerage + exchangeCharges) * GST_PERCENT / 100
        val sebiCharges = turnover * SEBI_CHARGE_PER_CRORE / 10000000.0
        val stampDuty = turnover * STAMP_DUTY_PERCENT / 100

        val totalCharges = brokerage + stt + exchangeCharges + gst + sebiCharges + stampDuty
        val netAmount = turnover + totalCharges

        return ChargesBreakdown(brokerage, stt, exchangeCharges, gst, sebiCharges, stampDuty, totalCharges, netAmount)
    }

    private fun formatChargesBreakdown(charges: ChargesBreakdown, isSell: Boolean): String {
        return buildString {
            append("Brokerage: ₹${String.format("%.2f", charges.brokerage)}\n")
            if (isSell) append("STT: ₹${String.format("%.2f", charges.stt)}\n")
            append("Exchange Txn: ₹${String.format("%.2f", charges.exchangeCharges)}\n")
            append("GST (18%): ₹${String.format("%.2f", charges.gst)}\n")
            append("SEBI Charges: ₹${String.format("%.4f", charges.sebiCharges)}\n")
            if (!isSell) append("Stamp Duty: ₹${String.format("%.2f", charges.stampDuty)}\n")
            append("━━━━━━━━━━━━━━━━━━━━\n")
            append("Total Charges: ₹${String.format("%.2f", charges.totalCharges)}\n")
            if (isSell) {
                append("Net Credit: ₹${String.format("%,.2f", charges.netAmount)}")
            } else {
                append("Total Debit: ₹${String.format("%,.2f", charges.netAmount)}")
            }
        }
    }

    // ==================== UI SETUP ====================
    private fun setupUI() {
        val backBtn = findViewById<ImageView>(R.id.backBtn)
        val symbolText = findViewById<TextView>(R.id.stockSymbol)
        val nameText = findViewById<TextView>(R.id.stockName)
        val priceText = findViewById<TextView>(R.id.stockPrice)
        val changeText = findViewById<TextView>(R.id.stockChange)
        val exchangeText = findViewById<TextView>(R.id.stockExchange)
        val qtyInput = findViewById<EditText>(R.id.quantityInput)
        val estimatedCost = findViewById<TextView>(R.id.estimatedCost)
        val buyBtn = findViewById<Button>(R.id.buyBtn)
        val sellBtn = findViewById<Button>(R.id.sellBtn)
        val balanceText = findViewById<TextView>(R.id.availableBalance)
        val positionCard = findViewById<LinearLayout>(R.id.positionCard)
        val positionInfo = findViewById<TextView>(R.id.positionInfo)
        backBtn?.setOnClickListener { finish() }

        symbolText?.text = stockSymbol
        nameText?.text = stockName

        val assetType = when {
            isIndex -> "INDEX"
            isCrypto -> "CRYPTO"
            else -> "NSE"
        }
        exchangeText?.text = assetType

        if (currentPrice > 0) {
            val prefix = if (isCrypto) "$" else "₹"
            priceText?.text = String.format("${prefix}%,.2f", currentPrice)
        } else {
            priceText?.text = "Loading..."
        }

        val changePercent = (Math.random() * 4 - 2)
        val isPositive = changePercent >= 0
        changeText?.text = String.format("%s%.2f%%", if (isPositive) "+" else "", changePercent)
        changeText?.setTextColor(if (isPositive) 0xFF10B981.toInt() else 0xFFEF4444.toInt())

        // For indexes, change label to "lots"
        if (isIndex) {
            qtyInput?.hint = "Enter number of lots"
        }

        // Show balance
        val balance = WalletManager.getBalance(this)
        balanceText?.text = String.format("Available: ₹%,.0f RC Coins", balance)

        // Show current position
        val positions = WalletManager.getPositions(this)
        val existing = positions.find { it.symbol == stockSymbol }
        if (existing != null) {
            val pnl = (currentPrice - existing.avgPrice) * existing.quantity
            positionInfo?.text = String.format(
                "Qty: %d  |  Avg: ₹%,.2f  |  P&L: %s₹%,.2f",
                existing.quantity, existing.avgPrice,
                if (pnl >= 0) "+" else "", pnl
            )
            positionCard?.visibility = View.VISIBLE
        } else {
            positionCard?.visibility = View.GONE
        }

        // Estimated cost
        qtyInput?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val qty = s.toString().toIntOrNull() ?: 0
                val cost = qty * currentPrice
                estimatedCost?.text = String.format("Estimated Cost: ₹%,.2f", cost)
            }
        })

        buyBtn?.setOnClickListener {
            if (currentPrice <= 0) {
                Toast.makeText(this, "Price not loaded yet", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val qty = qtyInput?.text.toString().toIntOrNull()
            if (qty == null || qty <= 0) {
                Toast.makeText(this, "Enter valid quantity", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val unit = if (isIndex) "lots" else "shares"
            val totalCost = qty * currentPrice

            AlertDialog.Builder(this)
                .setTitle("Confirm Buy Order")
                .setMessage("Buy $qty $unit of $stockSymbol @ ₹${String.format("%,.2f", currentPrice)}\n\nTotal: ₹${String.format("%,.2f", totalCost)}")
                .setPositiveButton("✅ Confirm") { _, _ ->
                    val charges = calculateBuyCharges(qty, currentPrice)
                    if (WalletManager.executeBuyWithCharges(this, stockSymbol, stockName, qty, currentPrice, charges.totalCharges, charges)) {
                        Toast.makeText(this, "Buy order executed! ✅", Toast.LENGTH_SHORT).show()
                        refreshUI()
                    } else {
                        Toast.makeText(this, "Insufficient balance! ❌", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        sellBtn?.setOnClickListener {
            if (currentPrice <= 0) {
                Toast.makeText(this, "Price not loaded yet", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val qty = qtyInput?.text.toString().toIntOrNull()
            if (qty == null || qty <= 0) {
                Toast.makeText(this, "Enter valid quantity", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val unit = if (isIndex) "lots" else "shares"
            val totalValue = qty * currentPrice

            AlertDialog.Builder(this)
                .setTitle("Confirm Sell Order")
                .setMessage("Sell $qty $unit of $stockSymbol @ ₹${String.format("%,.2f", currentPrice)}\n\nTotal: ₹${String.format("%,.2f", totalValue)}")
                .setPositiveButton("✅ Confirm") { _, _ ->
                    val charges = calculateSellCharges(qty, currentPrice)
                    val existingPos = WalletManager.getPositions(this).find { it.symbol == stockSymbol }
                    val pnl = if (existingPos != null) (currentPrice - existingPos.avgPrice) * qty else 0.0

                    if (WalletManager.executeSellWithCharges(this, stockSymbol, stockName, qty, currentPrice, charges.totalCharges, charges)) {
                        // Show brokerage breakdown AFTER successful sell
                        showSellChargesDialog(qty, currentPrice, charges, pnl)
                        refreshUI()
                    } else {
                        Toast.makeText(this, "No position or insufficient quantity! ❌", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun refreshUI() {
        setupUI()
        setupCandlestickChart()
    }

    private fun showSellChargesDialog(qty: Int, price: Double, charges: ChargesBreakdown, pnl: Double) {
        val turnover = qty * price
        val message = buildString {
            append("✅ Sell order executed!\n\n")
            append("Symbol: $stockSymbol\n")
            append("Qty: $qty @ ₹${String.format("%,.2f", price)}\n")
            append("Turnover: ₹${String.format("%,.2f", turnover)}\n\n")
            append("━━━ Charges Breakdown ━━━\n")
            append("Brokerage: ₹${String.format("%.2f", charges.brokerage)}\n")
            append("STT: ₹${String.format("%.2f", charges.stt)}\n")
            append("Exchange Txn: ₹${String.format("%.2f", charges.exchangeCharges)}\n")
            append("GST (18%): ₹${String.format("%.2f", charges.gst)}\n")
            append("SEBI Charges: ₹${String.format("%.4f", charges.sebiCharges)}\n")
            append("━━━━━━━━━━━━━━━━━━━━\n")
            append("Total Charges: ₹${String.format("%.2f", charges.totalCharges)}\n\n")
            append("P&L: ${if (pnl >= 0) "+" else ""}₹${String.format("%,.2f", pnl)}\n")
            append("Net Credit: ₹${String.format("%,.2f", charges.netAmount)}")
        }

        AlertDialog.Builder(this)
            .setTitle("📊 Trade Summary")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .setCancelable(false)
            .show()
    }
}
