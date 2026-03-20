package com.example.riskcraft

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.riskcraft.adapter.OrdersAdapter
import com.example.riskcraft.adapter.PositionsAdapter

class WalletActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallet)

        findViewById<ImageView>(R.id.backBtn)?.setOnClickListener { finish() }
        findViewById<TextView>(R.id.resetBtn)?.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Reset Portfolio")
                .setMessage("This will reset your wallet to ₹10,00,000 RC Coins and clear all positions and orders. Are you sure?")
                .setPositiveButton("Reset") { _, _ ->
                    WalletManager.resetWallet(this)
                    Toast.makeText(this, "Portfolio reset! 🔄", Toast.LENGTH_SHORT).show()
                    refreshUI()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        refreshUI()
    }

    private fun refreshUI() {
        val balance = WalletManager.getBalance(this)
        val invested = WalletManager.getInvestedValue(this)
        val currentValue = WalletManager.getCurrentValue(this)
        val pnl = WalletManager.getTotalPnL(this)
        val totalPortfolio = balance + currentValue

        findViewById<TextView>(R.id.totalValue)?.text = String.format("₹%,.0f", totalPortfolio)
        findViewById<TextView>(R.id.investedValue)?.text = String.format("₹%,.0f", invested)
        findViewById<TextView>(R.id.availableBalance)?.text = String.format("₹%,.0f", balance)

        val pnlView = findViewById<TextView>(R.id.pnlValue)
        pnlView?.text = String.format("%s₹%,.0f", if (pnl >= 0) "+" else "", pnl)
        pnlView?.setTextColor(if (pnl >= 0) 0xFF34D399.toInt() else 0xFFF87171.toInt())

        // Positions
        val positions = WalletManager.getPositions(this)
        val positionsRecycler = findViewById<RecyclerView>(R.id.positionsRecyclerView)
        val emptyHoldings = findViewById<TextView>(R.id.emptyHoldings)

        if (positions.isEmpty()) {
            emptyHoldings?.visibility = View.VISIBLE
            positionsRecycler?.visibility = View.GONE
        } else {
            emptyHoldings?.visibility = View.GONE
            positionsRecycler?.visibility = View.VISIBLE
            positionsRecycler?.layoutManager = LinearLayoutManager(this)
            positionsRecycler?.adapter = PositionsAdapter(positions)
        }

        // Orders
        val orders = WalletManager.getOrders(this)
        val ordersRecycler = findViewById<RecyclerView>(R.id.ordersRecyclerView)
        val emptyOrders = findViewById<TextView>(R.id.emptyOrders)

        if (orders.isEmpty()) {
            emptyOrders?.visibility = View.VISIBLE
            ordersRecycler?.visibility = View.GONE
        } else {
            emptyOrders?.visibility = View.GONE
            ordersRecycler?.visibility = View.VISIBLE
            ordersRecycler?.layoutManager = LinearLayoutManager(this)
            ordersRecycler?.adapter = OrdersAdapter(orders)
        }
    }

    override fun onResume() {
        super.onResume()
        refreshUI()
    }
}
