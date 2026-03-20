package com.example.riskcraft.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.riskcraft.R
import com.example.riskcraft.model.Order
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OrdersAdapter(
    private val orders: List<Order>
) : RecyclerView.Adapter<OrdersAdapter.OrderViewHolder>() {

    private val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())

    class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val symbol: TextView = itemView.findViewById(R.id.orderSymbol)
        val details: TextView = itemView.findViewById(R.id.orderDetails)
        val status: TextView = itemView.findViewById(R.id.orderStatus)
        val time: TextView = itemView.findViewById(R.id.orderTime)
        val viewChargesBtn: TextView = itemView.findViewById(R.id.viewChargesBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        holder.symbol.text = order.symbol
        holder.details.text = "${order.side} • ${order.quantity} Qty • ₹${String.format("%,.2f", order.price)}"
        holder.status.text = order.status
        holder.time.text = dateFormat.format(Date(order.timestamp))

        val sideColor = if (order.side == "BUY") 0xFF10B981.toInt() else 0xFFEF4444.toInt()
        holder.details.setTextColor(sideColor)

        // Status badge color
        when (order.status.uppercase()) {
            "EXECUTED", "COMPLETED" -> holder.status.setTextColor(0xFF10B981.toInt())
            "PENDING" -> holder.status.setTextColor(0xFFFBBF24.toInt())
            else -> holder.status.setTextColor(0xFF94A3B8.toInt())
        }

        // Show "View Charges" button only for orders that have charges
        if (order.charges > 0) {
            holder.viewChargesBtn.visibility = View.VISIBLE
            holder.viewChargesBtn.setOnClickListener {
                showChargesDialog(holder.itemView, order)
            }
        } else {
            holder.viewChargesBtn.visibility = View.GONE
        }
    }

    private fun showChargesDialog(view: View, order: Order) {
        val turnover = order.quantity * order.price
        val isSell = order.side == "SELL"

        val message = buildString {
            append("${order.side} • ${order.quantity} Qty @ ₹${String.format("%,.2f", order.price)}\n")
            append("Turnover: ₹${String.format("%,.2f", turnover)}\n\n")
            append("━━━ Charges Breakdown ━━━\n")
            append("Brokerage: ₹${String.format("%.2f", order.brokerage)}\n")
            if (isSell) append("STT: ₹${String.format("%.2f", order.stt)}\n")
            append("Exchange Txn: ₹${String.format("%.2f", order.exchangeCharges)}\n")
            append("GST (18%): ₹${String.format("%.2f", order.gst)}\n")
            append("SEBI Charges: ₹${String.format("%.4f", order.sebiCharges)}\n")
            if (!isSell && order.stampDuty > 0) append("Stamp Duty: ₹${String.format("%.2f", order.stampDuty)}\n")
            append("━━━━━━━━━━━━━━━━━━━━\n")
            append("Total Charges: ₹${String.format("%.2f", order.charges)}\n\n")
            if (isSell) {
                append("Net Credit: ₹${String.format("%,.2f", turnover - order.charges)}")
            } else {
                append("Total Debit: ₹${String.format("%,.2f", turnover + order.charges)}")
            }
        }

        AlertDialog.Builder(view.context)
            .setTitle("📊 ${order.symbol} — Charges")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    override fun getItemCount(): Int = orders.size
}
