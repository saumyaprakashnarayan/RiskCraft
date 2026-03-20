package com.example.riskcraft.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.riskcraft.R
import com.example.riskcraft.model.Position

class PositionsAdapter(
    private val positions: List<Position>
) : RecyclerView.Adapter<PositionsAdapter.PositionViewHolder>() {

    class PositionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val symbol: TextView = itemView.findViewById(R.id.positionSymbol)
        val name: TextView = itemView.findViewById(R.id.positionName)
        val details: TextView = itemView.findViewById(R.id.positionDetails)
        val ltp: TextView = itemView.findViewById(R.id.positionLtp)
        val pnl: TextView = itemView.findViewById(R.id.positionPnl)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PositionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_position, parent, false)
        return PositionViewHolder(view)
    }

    override fun onBindViewHolder(holder: PositionViewHolder, position: Int) {
        val item = positions[position]

        holder.symbol.text = item.symbol
        holder.name.text = if (item.name.isNotEmpty()) item.name else item.symbol
        holder.details.text = "Qty: ${item.quantity} • Avg: ₹${String.format("%,.2f", item.avgPrice)}"
        holder.ltp.text = String.format("₹%,.2f", item.ltp)

        val pnlValue = (item.ltp - item.avgPrice) * item.quantity
        val pnlPercent = if (item.avgPrice > 0) ((item.ltp - item.avgPrice) / item.avgPrice) * 100 else 0.0
        holder.pnl.text = String.format("%s₹%,.2f (%.1f%%)", if (pnlValue >= 0) "+" else "", pnlValue, pnlPercent)
        holder.pnl.setTextColor(if (pnlValue >= 0) 0xFF10B981.toInt() else 0xFFEF4444.toInt())
    }

    override fun getItemCount(): Int = positions.size
}
