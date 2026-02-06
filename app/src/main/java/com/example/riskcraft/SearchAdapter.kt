package com.example.riskcraft

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.riskcraft.network.SearchStock

class SearchAdapter(
    private val stocks: List<SearchStock>,
    private val onClick: (SearchStock) -> Unit
) : RecyclerView.Adapter<SearchAdapter.SearchViewHolder>() {

    class SearchViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val symbol: TextView = view.findViewById(R.id.searchSymbol)
        val desc: TextView = view.findViewById(R.id.searchDesc)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_stock, parent, false)
        return SearchViewHolder(view)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        val stock = stocks[position]
        holder.symbol.text = stock.symbol
        holder.desc.text = stock.description
        holder.itemView.setOnClickListener { onClick(stock) }
    }

    override fun getItemCount() = stocks.size
}
