package com.example.riskcraft

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.riskcraft.adapter.LeaderboardAdapter

class LeaderboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)

        val backBtn = findViewById<ImageView>(R.id.backBtn)
        backBtn.setOnClickListener { finish() }

        val recyclerView = findViewById<RecyclerView>(R.id.leaderboardRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Sync current user's wallet data to Firestore first
        FirestoreManager.syncWalletToCloud(this)

        // Fetch leaderboard from Firestore (falls back to demo data)
        FirestoreManager.fetchLeaderboard { entries ->
            runOnUiThread {
                recyclerView.adapter = LeaderboardAdapter(entries)
            }
        }
    }
}
