package com.example.riskcraft

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SearchResultAdapter(
    private val stocks: List<Pair<String, String>>,
    private val onClick: (symbol: String, name: String) -> Unit
) : RecyclerView.Adapter<SearchResultAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val symbol: TextView = itemView.findViewById(R.id.stockSymbol)
        val name: TextView = itemView.findViewById(R.id.stockName)
        val price: TextView = itemView.findViewById(R.id.stockPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_stock, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (symbol, name) = stocks[position]
        holder.symbol.text = symbol
        holder.name.text = name

        val stockPrice = IndianMarketData.getStockPrice(symbol)
        holder.price.text = String.format("₹%,.2f", stockPrice)

        holder.itemView.setOnClickListener {
            onClick(symbol, name)
        }
    }

    override fun getItemCount(): Int = stocks.size
}
