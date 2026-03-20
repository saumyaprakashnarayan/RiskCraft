package com.example.riskcraft

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class PinActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin)

        val pinInput = findViewById<EditText>(R.id.pinInput)
        val pinBtn = findViewById<Button>(R.id.pinBtn)
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val savedPin = prefs.getString("user_pin", null)

        pinBtn.setOnClickListener {
            val enteredPin = pinInput.text.toString()
            if (enteredPin.length != 4) {
                Toast.makeText(this, "Enter 4-digit PIN", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (enteredPin == savedPin) {
                startActivity(Intent(this, DashboardActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Wrong PIN ❌", Toast.LENGTH_SHORT).show()
                pinInput.text.clear()
            }
        }
    }
}
