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
        val details: TextView = itemView.findViewById(R.id.positionDetails)
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
        holder.details.text = "Qty: ${item.quantity} • Avg: ₹${item.avgPrice}"
        
        val pnlValue = (item.ltp - item.avgPrice) * item.quantity
        holder.pnl.text = String.format("₹%.2f", pnlValue)
        holder.pnl.setTextColor(if (pnlValue >= 0) 0xFF4CAF50.toInt() else 0xFFF44336.toInt())
    }

    override fun getItemCount(): Int = positions.size
}
