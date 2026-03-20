package com.example.riskcraft

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SearchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        val searchInput = findViewById<EditText>(R.id.searchInput)
        val recyclerView = findViewById<RecyclerView>(R.id.searchRecycler)
        val backBtn = findViewById<ImageView>(R.id.backBtn)

        backBtn?.setOnClickListener { finish() }

        recyclerView.layoutManager = LinearLayoutManager(this)

        // Show all Indian stocks initially
        val allStocks = IndianMarketData.getAllStockSymbols()
        showResults(recyclerView, allStocks)

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().uppercase().trim()
                if (query.isEmpty()) {
                    showResults(recyclerView, allStocks)
                } else {
                    val filtered = allStocks.filter {
                        it.first.contains(query) || it.second.uppercase().contains(query)
                    }
                    showResults(recyclerView, filtered)
                }
            }
        })
    }

    private fun showResults(recyclerView: RecyclerView, stocks: List<Pair<String, String>>) {
        recyclerView.adapter = SearchResultAdapter(stocks) { symbol, name ->
            val price = IndianMarketData.getStockPrice(symbol)
            val intent = Intent(this, TradeActivity::class.java).apply {
                putExtra("symbol", symbol)
                putExtra("name", name)
                putExtra("price", price)
            }
            startActivity(intent)
        }
    }
}
