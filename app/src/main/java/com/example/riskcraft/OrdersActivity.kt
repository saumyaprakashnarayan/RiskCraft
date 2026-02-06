package com.example.riskcraft

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.riskcraft.adapter.OrdersAdapter
import com.example.riskcraft.model.Order

class OrdersActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_orders)

        val recyclerView = findViewById<RecyclerView>(R.id.ordersRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Dummy orders (later replaced by live / Firebase data)
        val orders = listOf(
            Order("RELIANCE", "BUY", 10, 2450.0, "Completed"),
            Order("TCS", "SELL", 5, 3820.0, "Pending"),
            Order("HDFCBANK", "BUY", 20, 1480.0, "Completed"),
            Order("INFY", "SELL", 10, 1560.0, "Rejected")
        )

        recyclerView.adapter = OrdersAdapter(orders)
    }
}
