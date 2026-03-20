package com.example.riskcraft

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.riskcraft.adapter.OrdersAdapter

class OrdersActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_orders)

        findViewById<ImageView>(R.id.backBtn)?.setOnClickListener { finish() }

        val recyclerView = findViewById<RecyclerView>(R.id.ordersRecyclerView)
        val emptyText = findViewById<TextView>(R.id.emptyText)

        recyclerView.layoutManager = LinearLayoutManager(this)

        val orders = WalletManager.getOrders(this)
        if (orders.isEmpty()) {
            emptyText?.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyText?.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            recyclerView.adapter = OrdersAdapter(orders)
        }
    }
}
