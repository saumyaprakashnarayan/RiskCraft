package com.example.riskcraft

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.riskcraft.network.ApiClient
import com.example.riskcraft.network.MarketResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DashboardActivity : AppCompatActivity() {

    private val API_KEY = "d62id0pr01qlugeq660gd62id0pr01qlugeq6610"
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val profileIcon = findViewById<ImageView>(R.id.profileIcon)
        loadUserThumbnail(profileIcon)

        profileIcon?.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        loadLiveMarketData()
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

    private fun loadLiveMarketData() {
        // Using US stock symbols as they are reliably supported on Finnhub's free tier
        fetchPrice("AAPL", findViewById(R.id.niftyText), "APPLE")
        fetchPrice("GOOGL", findViewById(R.id.sensexText), "GOOGLE")
        fetchPrice("AMZN", findViewById(R.id.bankNiftyText), "AMAZON")
    }

    private fun fetchPrice(symbol: String, textView: TextView?, label: String) {
        ApiClient.api.getQuote(symbol, API_KEY).enqueue(object : Callback<MarketResponse> {
            override fun onResponse(call: Call<MarketResponse>, response: Response<MarketResponse>) {
                val data = response.body()
                if (data != null && data.c != 0.0) {
                    val price = data.c
                    val changePercent = data.dp
                    val color = if (changePercent >= 0) "#4CAF50" else "#F44336"
                    val sign = if (changePercent >= 0) "+" else ""
                    
                    // Displaying Price and Percent Change
                    val text = "$${String.format("%.2f", price)} ($sign${String.format("%.2f", changePercent)}%)"
                    textView?.text = text
                    textView?.setTextColor(Color.parseColor(color))
                } else {
                    textView?.text = "N/A"
                }
            }
            override fun onFailure(call: Call<MarketResponse>, t: Throwable) {
                textView?.text = "Error"
            }
        })
    }

    override fun onResume() {
        super.onResume()
        loadUserThumbnail(findViewById(R.id.profileIcon))
    }
}
