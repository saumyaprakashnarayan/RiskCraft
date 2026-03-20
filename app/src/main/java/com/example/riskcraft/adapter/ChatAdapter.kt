package com.example.riskcraft.adapter

import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.riskcraft.R
import com.example.riskcraft.model.ChatMessage

class ChatAdapter(private val messages: List<ChatMessage>) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val messageText: TextView = view.findViewById(R.id.messageText)
        val bubbleContainer: LinearLayout = view.findViewById(R.id.bubbleContainer)
        val messageRow: LinearLayout = view.findViewById(R.id.messageRow)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_message, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = messages[position]
        holder.messageText.text = message.text

        if (message.isUser) {
            holder.messageRow.gravity = Gravity.END
            holder.bubbleContainer.setBackgroundResource(R.drawable.chat_bubble_user)
            holder.messageText.setTextColor(Color.WHITE)
        } else {
            holder.messageRow.gravity = Gravity.START
            holder.bubbleContainer.setBackgroundResource(R.drawable.chat_bubble_ai)
            holder.messageText.setTextColor(Color.parseColor("#E5E7EB"))
        }
    }

    override fun getItemCount(): Int = messages.size
}
