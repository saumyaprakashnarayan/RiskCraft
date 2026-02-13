package com.example.riskcraft.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.riskcraft.R
import com.example.riskcraft.model.LeaderboardEntry

class LeaderboardAdapter(private val leaderboard: List<LeaderboardEntry>) :
    RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewHolder>() {

    class LeaderboardViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val rank: TextView = view.findViewById(R.id.rankText)
        val image: ImageView = view.findViewById(R.id.userImage)
        val name: TextView = view.findViewById(R.id.userName)
        val profit: TextView = view.findViewById(R.id.profitText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderboardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_leaderboard, parent, false)
        return LeaderboardViewHolder(view)
    }

    override fun onBindViewHolder(holder: LeaderboardViewHolder, position: Int) {
        val entry = leaderboard[position]
        holder.rank.text = entry.rank.toString()
        holder.name.text = entry.name
        holder.profit.text = String.format("+₹%,.2f", entry.profit)

        if (entry.profileImageUrl.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(entry.profileImageUrl)
                .circleCrop()
                .into(holder.image)
        } else {
            holder.image.setImageResource(R.drawable.ic_profile)
        }
    }

    override fun getItemCount() = leaderboard.size
}
