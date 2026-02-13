package com.example.riskcraft.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.riskcraft.R
import com.example.riskcraft.model.Challenge

class ChallengesAdapter(private val challenges: List<Challenge>) :
    RecyclerView.Adapter<ChallengesAdapter.ChallengeViewHolder>() {

    class ChallengeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.challengeTitle)
        val desc: TextView = view.findViewById(R.id.challengeDesc)
        val reward: TextView = view.findViewById(R.id.challengeReward)
        val joinBtn: Button = view.findViewById(R.id.joinBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChallengeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_challenge, parent, false)
        return ChallengeViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChallengeViewHolder, position: Int) {
        val challenge = challenges[position]
        holder.title.text = challenge.title
        holder.desc.text = challenge.description
        holder.reward.text = "Reward: ${challenge.reward}"
        holder.joinBtn.setOnClickListener {
            // Handle join logic
        }
    }

    override fun getItemCount() = challenges.size
}
