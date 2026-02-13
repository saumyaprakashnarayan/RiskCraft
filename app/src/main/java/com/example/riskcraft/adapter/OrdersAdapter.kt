package com.example.riskcraft.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.riskcraft.R
import com.example.riskcraft.model.Order

class OrdersAdapter(
    private val orders: List<Order>
) : RecyclerView.Adapter<OrdersAdapter.OrderViewHolder>() {

    class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val symbol: TextView = itemView.findViewById(R.id.orderSymbol)
        val details: TextView = itemView.findViewById(R.id.orderDetails)
        val status: TextView = itemView.findViewById(R.id.orderStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        holder.symbol.text = order.symbol
        holder.details.text = "${order.side} • ${order.quantity} Qty • ₹${order.price}"
        holder.status.text = order.status

        if (order.side == "BUY") holder.details.setTextColor(0xFF4CAF50.toInt())
        else holder.details.setTextColor(0xFFF44336.toInt())
    }

    override fun getItemCount(): Int = orders.size
}
