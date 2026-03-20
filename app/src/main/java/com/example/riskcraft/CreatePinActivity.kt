package com.example.riskcraft

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class CreatePinActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_pin)

        val pinInput = findViewById<EditText>(R.id.createPinInput)
        val saveBtn = findViewById<Button>(R.id.savePinBtn)
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)

        saveBtn.setOnClickListener {
            val pin = pinInput.text.toString()
            if (pin.length != 4) {
                Toast.makeText(this, "PIN must be exactly 4 digits", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            prefs.edit().putString("user_pin", pin).apply()
            Toast.makeText(this, "PIN set successfully! 🔒", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        }
    }
}
