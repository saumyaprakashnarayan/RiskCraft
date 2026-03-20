package com.example.riskcraft

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.riskcraft.adapter.ChallengesAdapter
import com.example.riskcraft.model.Challenge
import com.example.riskcraft.network.ApiClient
import com.example.riskcraft.network.MarketResponse
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar

class DashboardActivity : AppCompatActivity() {

    private val API_KEY = "d62id0pr01qlugeq660gd62id0pr01qlugeq6610"
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupGreeting()
        setupWalletCard()
        setupQuickActions()
        setupSearch()
        setupIndices()
        setupIndianMarket()
        setupCryptoMarket()
        setupChallenges()
        setupWatchlist()

        // Sync wallet data to Firestore for leaderboard
        FirestoreManager.syncWalletToCloud(this)

        val profileIcon = findViewById<ImageView>(R.id.profileIcon)
        val leaderboardIcon = findViewById<ImageView>(R.id.leaderboardIcon)

        loadUserThumbnail(profileIcon)

        profileIcon?.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        leaderboardIcon?.setOnClickListener {
            startActivity(Intent(this, LeaderboardActivity::class.java))
        }

        // AI Chat FAB
        val fabAiChat = findViewById<ExtendedFloatingActionButton>(R.id.fabAiChat)
        fabAiChat?.setOnClickListener {
            startActivity(Intent(this, GeminiChatActivity::class.java))
        }
    }

    private fun setupGreeting() {
        val greetingText = findViewById<TextView>(R.id.greetingText)
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val greeting = when {
            hour < 12 -> "Good Morning 👋"
            hour < 17 -> "Good Afternoon 👋"
            else -> "Good Evening 👋"
        }
        greetingText.text = greeting
    }

    private fun setupWalletCard() {
        val balance = WalletManager.getBalance(this)
        val invested = WalletManager.getInvestedValue(this)
        val returns = WalletManager.getTotalPnL(this)
        val available = balance

        findViewById<TextView>(R.id.walletBalance)?.text = String.format("₹%,.0f", balance + invested)
        findViewById<TextView>(R.id.investedValue)?.text = String.format("₹%,.0f", invested)
        findViewById<TextView>(R.id.availableBalance)?.text = String.format("₹%,.0f", available)

        val returnsView = findViewById<TextView>(R.id.returnsValue)
        returnsView?.text = String.format("%s₹%,.0f", if (returns >= 0) "+" else "", returns)
        returnsView?.setTextColor(if (returns >= 0) Color.parseColor("#34D399") else Color.parseColor("#F87171"))

        findViewById<LinearLayout>(R.id.walletCard)?.setOnClickListener {
            startActivity(Intent(this, WalletActivity::class.java))
        }
    }

    private fun setupQuickActions() {
        findViewById<LinearLayout>(R.id.tradeBtn)?.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.portfolioBtn)?.setOnClickListener {
            startActivity(Intent(this, WalletActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.ordersBtn)?.setOnClickListener {
            startActivity(Intent(this, OrdersActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.leaderboardBtn)?.setOnClickListener {
            startActivity(Intent(this, LeaderboardActivity::class.java))
        }
    }

    private fun setupSearch() {
        val searchBox = findViewById<LinearLayout>(R.id.searchBox)
        searchBox?.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }
        findViewById<View>(R.id.searchEditText)?.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }
    }

    private fun setupIndices() {
        val container = findViewById<LinearLayout>(R.id.indicesContainer) ?: return
        container.removeAllViews()

        val indices = IndianMarketData.getIndices()
        for (index in indices) {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(48, 36, 48, 36)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = 24 }
                setBackgroundResource(R.drawable.hero_card_bg)
            }

            val nameLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            nameLayout.addView(TextView(this).apply {
                text = index.symbol
                setTextColor(Color.WHITE)
                textSize = 16f
                setTypeface(typeface, android.graphics.Typeface.BOLD)
            })

            val valueLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.END
            }
            valueLayout.addView(TextView(this).apply {
                text = String.format("%,.2f", index.price)
                setTextColor(Color.WHITE)
                textSize = 16f
                setTypeface(typeface, android.graphics.Typeface.BOLD)
            })
            val isPositive = index.changePercent >= 0
            valueLayout.addView(TextView(this).apply {
                text = String.format("%s%.2f%%", if (isPositive) "+" else "", index.changePercent)
                setTextColor(if (isPositive) Color.parseColor("#10B981") else Color.parseColor("#EF4444"))
                textSize = 13f
                setTypeface(typeface, android.graphics.Typeface.BOLD)
            })

            row.addView(nameLayout)
            row.addView(valueLayout)

            row.setOnClickListener {
                val intent = Intent(this, TradeActivity::class.java).apply {
                    putExtra("symbol", index.symbol)
                    putExtra("name", index.symbol)
                    putExtra("price", index.price)
                    putExtra("isIndex", true)
                }
                startActivity(intent)
            }

            container.addView(row)
        }
    }

    private fun setupIndianMarket() {
        val container = findViewById<LinearLayout>(R.id.indianStocksContainer) ?: return
        container.removeAllViews()

        val stocks = IndianMarketData.getIndianStocks().take(10)
        for (stock in stocks) {
            val card = createMarketCard(stock.symbol, stock.name, stock.price, stock.changePercent)
            card.setOnClickListener {
                val intent = Intent(this, TradeActivity::class.java).apply {
                    putExtra("symbol", stock.symbol)
                    putExtra("name", stock.name)
                    putExtra("price", stock.price)
                }
                startActivity(intent)
            }
            container.addView(card)
        }
    }

    private fun setupCryptoMarket() {
        val container = findViewById<LinearLayout>(R.id.cryptoContainer) ?: return
        container.removeAllViews()

        val cryptoList = IndianMarketData.getCryptoSymbols()
        for ((apiSymbol, displayName) in cryptoList) {
            val card = createMarketCard(displayName, "CRYPTO", 0.0, 0.0)
            val priceText = card.findViewWithTag<TextView>("price")
            val changeText = card.findViewWithTag<TextView>("change")

            // Fetch live price from Finnhub
            ApiClient.api.getQuote(apiSymbol, API_KEY).enqueue(object : Callback<MarketResponse> {
                override fun onResponse(call: Call<MarketResponse>, response: Response<MarketResponse>) {
                    val data = response.body()
                    if (data != null && data.c != 0.0) {
                        val isPos = data.dp >= 0
                        priceText?.text = String.format("$%,.2f", data.c)
                        priceText?.setTextColor(Color.WHITE)
                        changeText?.text = String.format("%s%.2f%%", if (isPos) "+" else "", data.dp)
                        changeText?.setTextColor(if (isPos) Color.parseColor("#10B981") else Color.parseColor("#EF4444"))
                    } else {
                        priceText?.text = "N/A"
                    }
                }
                override fun onFailure(call: Call<MarketResponse>, t: Throwable) {
                    priceText?.text = "Error"
                }
            })

            card.setOnClickListener {
                val intent = Intent(this, TradeActivity::class.java).apply {
                    putExtra("symbol", displayName)
                    putExtra("name", displayName)
                    putExtra("price", 0.0)
                    putExtra("isCrypto", true)
                    putExtra("apiSymbol", apiSymbol)
                }
                startActivity(intent)
            }
            container.addView(card)
        }
    }

    private fun createMarketCard(name: String, subtitle: String, price: Double, change: Double): LinearLayout {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 36, 40, 36)
            layoutParams = LinearLayout.LayoutParams(420, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                marginEnd = 28
            }
            setBackgroundResource(R.drawable.market_card_bg)
            elevation = 4f
        }

        card.addView(TextView(this).apply {
            text = name
            setTextColor(Color.WHITE)
            textSize = 15f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        })

        card.addView(TextView(this).apply {
            text = subtitle
            setTextColor(Color.parseColor("#6B7280"))
            textSize = 11f
            setPadding(0, 4, 0, 16)
        })

        val isPositive = change >= 0
        card.addView(TextView(this).apply {
            tag = "price"
            text = if (price > 0) String.format("₹%,.2f", price) else "Loading..."
            setTextColor(Color.WHITE)
            textSize = 17f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        })

        card.addView(TextView(this).apply {
            tag = "change"
            text = if (price > 0) String.format("%s%.2f%%", if (isPositive) "+" else "", change) else ""
            setTextColor(if (isPositive) Color.parseColor("#10B981") else Color.parseColor("#EF4444"))
            textSize = 13f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setPadding(0, 8, 0, 0)
        })

        return card
    }

    private fun setupChallenges() {
        val recyclerView = findViewById<RecyclerView>(R.id.challengesRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        val challenges = listOf(
            Challenge("1", "Profit Master", "Achieve 10% portfolio profit", 10.0, 5, "₹50,000 RC Coins"),
            Challenge("2", "Volume Trader", "Execute 20 trades this week", 0.0, 7, "Gold Badge"),
            Challenge("3", "Consistency King", "End 3 days in green P&L", 5.0, 3, "₹20,000 RC Coins"),
            Challenge("4", "Diversifier", "Hold 5 different stocks", 0.0, 10, "Pro Trader Badge")
        )

        // Evaluate challenges and show completion status
        FirestoreManager.evaluateChallenges(this) { results ->
            runOnUiThread {
                val completedIds = results.filter { it.value }.keys.toList()
                recyclerView.adapter = ChallengesAdapter(challenges, completedIds)
            }
        }
    }

    private fun setupWatchlist() {
        val container = findViewById<LinearLayout>(R.id.watchlistContainer) ?: return
        container.removeAllViews()

        val watchlist = IndianMarketData.getIndianStocks().take(5)
        for (stock in watchlist) {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(48, 36, 48, 36)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = 24 }
                setBackgroundResource(R.drawable.hero_card_bg)
            }

            val infoLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            infoLayout.addView(TextView(this).apply {
                text = stock.symbol
                setTextColor(Color.WHITE)
                textSize = 15f
                setTypeface(typeface, android.graphics.Typeface.BOLD)
            })
            infoLayout.addView(TextView(this).apply {
                text = stock.name
                setTextColor(Color.parseColor("#6B7280"))
                textSize = 11f
                setPadding(0, 4, 0, 0)
            })

            val priceLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.END
            }
            priceLayout.addView(TextView(this).apply {
                text = String.format("₹%,.2f", stock.price)
                setTextColor(Color.WHITE)
                textSize = 15f
                setTypeface(typeface, android.graphics.Typeface.BOLD)
            })
            val isPos = stock.changePercent >= 0
            priceLayout.addView(TextView(this).apply {
                text = String.format("%s%.2f%% %s", if (isPos) "+" else "", stock.changePercent, if (isPos) "▲" else "▼")
                setTextColor(if (isPos) Color.parseColor("#10B981") else Color.parseColor("#EF4444"))
                textSize = 12f
            })

            row.addView(infoLayout)
            row.addView(priceLayout)

            row.setOnClickListener {
                val intent = Intent(this, TradeActivity::class.java).apply {
                    putExtra("symbol", stock.symbol)
                    putExtra("name", stock.name)
                    putExtra("price", stock.price)
                }
                startActivity(intent)
            }

            container.addView(row)
        }
    }

    private fun loadUserThumbnail(imageView: ImageView) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                val imageUrl = doc.getString("profileImageUrl")
                if (!imageUrl.isNullOrEmpty()) {
                    Glide.with(this)
                        .load(imageUrl)
                        .circleCrop()
                        .placeholder(R.drawable.ic_profile)
                        .into(imageView)
                }
            }
    }

    override fun onResume() {
        super.onResume()
        setupWalletCard()
        setupChallenges()
        loadUserThumbnail(findViewById(R.id.profileIcon))
        FirestoreManager.syncWalletToCloud(this)
    }
}
