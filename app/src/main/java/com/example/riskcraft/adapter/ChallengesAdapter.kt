package com.example.riskcraft.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.riskcraft.R
import com.example.riskcraft.model.Challenge

class ChallengesAdapter(
    private val challenges: List<Challenge>,
    private val completedIds: List<String> = emptyList()
) : RecyclerView.Adapter<ChallengesAdapter.ChallengeViewHolder>() {

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

        val isCompleted = completedIds.contains(challenge.id)
        if (isCompleted) {
            holder.joinBtn.text = "✅ Done"
            holder.joinBtn.setBackgroundColor(Color.parseColor("#10B981"))
            holder.joinBtn.isEnabled = false
        } else {
            holder.joinBtn.text = "Join"
            holder.joinBtn.setBackgroundColor(Color.parseColor("#7C3AED"))
            holder.joinBtn.isEnabled = true
            holder.joinBtn.setOnClickListener {
                Toast.makeText(holder.itemView.context, "Keep trading to complete '${challenge.title}'!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount() = challenges.size
}
