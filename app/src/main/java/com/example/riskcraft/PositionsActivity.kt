package com.example.riskcraft

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.riskcraft.adapter.PositionsAdapter
import com.example.riskcraft.model.Position

class PositionsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_positions)

        val recyclerView = findViewById<RecyclerView>(R.id.positionsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Dummy positions (later replaced by live prices)
        val positions = listOf(
            Position("HDFCBANK", 20, 1480.0, 1540.0),
            Position("INFY", 10, 1560.0, 1525.0),
            Position("RELIANCE", 5, 2450.0, 2510.0)
        )

        recyclerView.adapter = PositionsAdapter(positions)
    }
}
