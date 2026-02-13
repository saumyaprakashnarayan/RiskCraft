package com.example.riskcraft

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.riskcraft.adapter.LeaderboardAdapter
import com.example.riskcraft.model.LeaderboardEntry

class LeaderboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)

        val backBtn = findViewById<ImageView>(R.id.backBtn)
        backBtn.setOnClickListener { finish() }

        val recyclerView = findViewById<RecyclerView>(R.id.leaderboardRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Sample data (replace with real data from Firestore)
        val leaderboard = listOf(
            LeaderboardEntry("1", "Alice", "", 12450.0, 1),
            LeaderboardEntry("2", "Bob", "", 9800.0, 2),
            LeaderboardEntry("3", "Charlie", "", 7500.0, 3),
            LeaderboardEntry("4", "David", "", 5200.0, 4),
            LeaderboardEntry("5", "Eve", "", 3100.0, 5)
        )

        recyclerView.adapter = LeaderboardAdapter(leaderboard)
    }
}
