package com.example.riskcraft

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*

class MainActivity : AppCompatActivity() {

    private var isLogin = true
    private var selectedDob: String = ""
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = Firebase.firestore

        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val savedPin = prefs.getString("user_pin", null)

        val currentUser = auth.currentUser
        if (currentUser != null) {
            if (savedPin != null) {
                startActivity(Intent(this, PinActivity::class.java))
            } else {
                startActivity(Intent(this, CreatePinActivity::class.java))
            }
            finish()
            return
        }

        setContentView(R.layout.activity_main)

        val name = findViewById<EditText>(R.id.nameInput)
        val contact = findViewById<EditText>(R.id.contactInput)
        val aadhaar = findViewById<EditText>(R.id.aadhaarInput)
        val pan = findViewById<EditText>(R.id.panInput)
        val occupation = findViewById<EditText>(R.id.occupationInput)
        val dobInput = findViewById<EditText>(R.id.dobInput)
        val email = findViewById<EditText>(R.id.emailInput)
        val password = findViewById<EditText>(R.id.passwordInput)
        val button = findViewById<Button>(R.id.actionButton)
        val switchText = findViewById<TextView>(R.id.switchText)
        val genderSpinner = findViewById<Spinner>(R.id.genderSpinner)
        val formTitle = findViewById<TextView>(R.id.formTitle)
        val formSubtitle = findViewById<TextView>(R.id.formSubtitle)

        val genders = arrayOf("Male", "Female", "Other")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, genders)
        genderSpinner.adapter = adapter

        dobInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePicker = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                if (currentYear - selectedYear < 18) {
                    Toast.makeText(this, "You must be 18+ to sign up", Toast.LENGTH_SHORT).show()
                } else {
                    selectedDob = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                    dobInput.setText(selectedDob)
                    dobInput.error = null
                }
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
            datePicker.show()
        }

        button.setOnClickListener {
            val emailStr = email.text.toString().trim()
            val passStr = password.text.toString().trim()

            if (isLogin) {
                if (emailStr.isEmpty() || passStr.isEmpty()) {
                    Toast.makeText(this, "Enter email and password", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                button.isEnabled = false
                button.text = "Logging In..."

                auth.signInWithEmailAndPassword(emailStr, passStr)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Login Successful ✅", Toast.LENGTH_SHORT).show()
                        // Fetch and cache profile data from Firestore server
                        // so profile survives app data/cache clears
                        FirestoreManager.fetchAndCacheProfile(this) {
                            if (savedPin != null) {
                                startActivity(Intent(this, PinActivity::class.java))
                            } else {
                                startActivity(Intent(this, CreatePinActivity::class.java))
                            }
                            finish()
                        }
                    }
                    .addOnFailureListener { e ->
                        button.isEnabled = true
                        button.text = "Login"
                        Toast.makeText(this, "Login failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                val nameStr = name.text.toString().trim()
                val contactStr = contact.text.toString().trim()
                val aadhaarStr = aadhaar.text.toString().trim()
                val panStr = pan.text.toString().trim().uppercase()
                val occupationStr = occupation.text.toString().trim()
                val genderStr = genderSpinner.selectedItem.toString()

                // Per-field validation with specific error messages
                var hasError = false

                if (nameStr.isEmpty()) {
                    name.error = "Name is required"
                    hasError = true
                }
                if (contactStr.length != 10) {
                    contact.error = "Enter valid 10-digit phone number"
                    hasError = true
                }
                if (aadhaarStr.length != 12) {
                    aadhaar.error = "Enter valid 12-digit Aadhaar number"
                    hasError = true
                }
                if (panStr.length != 10) {
                    pan.error = "Enter valid 10-character PAN (e.g. ABCDE1234F)"
                    hasError = true
                }
                if (occupationStr.isEmpty()) {
                    occupation.error = "Occupation is required"
                    hasError = true
                }
                if (selectedDob.isEmpty()) {
                    dobInput.error = "Tap to select your date of birth"
                    hasError = true
                }
                if (emailStr.isEmpty()) {
                    email.error = "Email is required"
                    hasError = true
                }
                if (passStr.length < 6) {
                    password.error = "Password must be at least 6 characters"
                    hasError = true
                }

                if (hasError) {
                    Toast.makeText(this, "Please fix the highlighted fields", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                // Create account directly with email/password (no OTP)
                button.isEnabled = false
                button.text = "Creating Account..."

                auth.createUserWithEmailAndPassword(emailStr, passStr)
                    .addOnSuccessListener {
                        val uid = auth.currentUser?.uid ?: ""
                        val userData = hashMapOf(
                            "uid" to uid,
                            "name" to nameStr,
                            "phone" to contactStr,
                            "aadhaar" to aadhaarStr,
                            "pan" to panStr,
                            "occupation" to occupationStr,
                            "gender" to genderStr,
                            "dob" to selectedDob,
                            "email" to emailStr,
                            "walletBalance" to WalletManager.STARTING_BALANCE,
                            "totalProfit" to 0.0,
                            "totalTrades" to 0,
                            "completedChallenges" to emptyList<String>(),
                            "joinedAt" to com.google.firebase.Timestamp.now()
                        )

                        // Write to Firestore — use addOnCompleteListener so we
                        // proceed regardless of server confirmation (data syncs later via offline cache)
                        db.collection("users").document(uid).set(userData)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    android.util.Log.d("MainActivity", "User data saved to Firestore")
                                } else {
                                    android.util.Log.w("MainActivity", "Firestore write queued offline: ${task.exception?.message}")
                                }
                            }

                        // Don't wait for Firestore write — proceed immediately
                        auth.currentUser?.sendEmailVerification()
                        auth.signOut()
                        Toast.makeText(this, "Account Created! 🎉 Please sign in.", Toast.LENGTH_SHORT).show()

                        // Switch to login mode
                        isLogin = true
                        updateUI()

                        // Clear all fields
                        findViewById<EditText>(R.id.emailInput).text.clear()
                        findViewById<EditText>(R.id.passwordInput).text.clear()
                        findViewById<EditText>(R.id.nameInput).text.clear()
                        findViewById<EditText>(R.id.contactInput).text.clear()
                        findViewById<EditText>(R.id.aadhaarInput).text.clear()
                        findViewById<EditText>(R.id.panInput).text.clear()
                        findViewById<EditText>(R.id.occupationInput).text.clear()
                        findViewById<EditText>(R.id.dobInput).text.clear()
                        button.isEnabled = true
                        button.text = "Sign In"
                    }
                    .addOnFailureListener { e ->
                        button.isEnabled = true
                        button.text = "Sign Up"
                        Toast.makeText(this, "Signup failed: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
        }

        switchText.setOnClickListener {
            isLogin = !isLogin
            updateUI()
        }

        updateUI()
    }

    private fun updateUI() {
        val name = findViewById<EditText>(R.id.nameInput)
        val contact = findViewById<EditText>(R.id.contactInput)
        val aadhaar = findViewById<EditText>(R.id.aadhaarInput)
        val pan = findViewById<EditText>(R.id.panInput)
        val occupation = findViewById<EditText>(R.id.occupationInput)
        val dobInput = findViewById<EditText>(R.id.dobInput)
        val genderSpinner = findViewById<Spinner>(R.id.genderSpinner)
        val button = findViewById<Button>(R.id.actionButton)
        val switchText = findViewById<TextView>(R.id.switchText)
        val formTitle = findViewById<TextView>(R.id.formTitle)
        val formSubtitle = findViewById<TextView>(R.id.formSubtitle)

        val visibility = if (isLogin) View.GONE else View.VISIBLE
        name.visibility = visibility
        contact.visibility = visibility
        aadhaar.visibility = visibility
        pan.visibility = visibility
        occupation.visibility = visibility
        dobInput.visibility = visibility
        genderSpinner.visibility = visibility

        if (isLogin) {
            formTitle.text = "Welcome Back"
            formSubtitle.text = "Sign in to continue trading"
            button.text = "Sign In"
            switchText.text = "Don't have an account? Sign Up"
        } else {
            formTitle.text = "Create Account"
            formSubtitle.text = "Join RiskCraft and start trading"
            button.text = "Sign Up"
            switchText.text = "Already have an account? Sign In"
        }
    }
}
