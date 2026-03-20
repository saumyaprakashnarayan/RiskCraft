package com.example.riskcraft

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.riskcraft.adapter.PositionsAdapter

class PositionsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_positions)

        findViewById<ImageView>(R.id.backBtn)?.setOnClickListener { finish() }

        val recyclerView = findViewById<RecyclerView>(R.id.positionsRecyclerView)
        val emptyText = findViewById<TextView>(R.id.emptyText)

        recyclerView.layoutManager = LinearLayoutManager(this)

        val positions = WalletManager.getPositions(this)

        if (positions.isEmpty()) {
            emptyText?.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyText?.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            recyclerView.adapter = PositionsAdapter(positions)
        }

        // Summary
        val invested = WalletManager.getInvestedValue(this)
        val current = WalletManager.getCurrentValue(this)
        val pnl = WalletManager.getTotalPnL(this)

        findViewById<TextView>(R.id.totalInvested)?.text = String.format("₹%,.0f", invested)
        findViewById<TextView>(R.id.currentValue)?.text = String.format("₹%,.0f", current)

        val pnlView = findViewById<TextView>(R.id.totalPnl)
        pnlView?.text = String.format("%s₹%,.0f", if (pnl >= 0) "+" else "", pnl)
        pnlView?.setTextColor(if (pnl >= 0) 0xFF10B981.toInt() else 0xFFEF4444.toInt())
    }
}
