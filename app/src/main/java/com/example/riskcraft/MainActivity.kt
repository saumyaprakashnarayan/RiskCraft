package com.example.riskcraft

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class MainActivity : AppCompatActivity() {

    private var isLogin = true
    private var selectedDob: String = ""
    private val tag = "MainActivity"
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val savedPin = prefs.getString("user_pin", null)

        // Auto-login logic
        val currentUser = auth.currentUser
        if (currentUser != null) {
            if (currentUser.isEmailVerified) {
                if (savedPin != null) {
                    startActivity(Intent(this, PinActivity::class.java))
                    finish()
                    return
                } else {
                    startActivity(Intent(this, CreatePinActivity::class.java))
                    finish()
                    return
                }
            } else {
                auth.signOut()
            }
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

        // Setup Gender Spinner
        val genders = arrayOf("Male", "Female", "Other")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, genders)
        genderSpinner.adapter = adapter

        // Date of Birth Picker
        dobInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                if (currentYear - selectedYear < 18) {
                    Toast.makeText(this, "You must be 18+ to sign up", Toast.LENGTH_SHORT).show()
                } else {
                    selectedDob = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                    dobInput.setText(selectedDob)
                    dobInput.error = null
                }
            }, year, month, day)
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
                
                auth.signInWithEmailAndPassword(emailStr, passStr)
                    .addOnSuccessListener {
                        val user = auth.currentUser
                        if (user != null && user.isEmailVerified) {
                            Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                            if (savedPin != null) {
                                startActivity(Intent(this, PinActivity::class.java))
                            } else {
                                startActivity(Intent(this, CreatePinActivity::class.java))
                            }
                            finish()
                        } else {
                            Toast.makeText(this, "Please verify your email link sent to $emailStr", Toast.LENGTH_LONG).show()
                            auth.signOut()
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                    }
            } else {
                // Signup Validation
                val nameStr = name.text.toString().trim()
                val contactStr = contact.text.toString().trim()
                val aadhaarStr = aadhaar.text.toString().trim()
                val panStr = pan.text.toString().trim().uppercase()
                val occupationStr = occupation.text.toString().trim()
                val genderStr = genderSpinner.selectedItem.toString()

                if (nameStr.isEmpty() || contactStr.isEmpty() || aadhaarStr.length != 12 || 
                    panStr.length != 10 || occupationStr.isEmpty() || selectedDob.isEmpty() || 
                    emailStr.isEmpty() || passStr.isEmpty()) {
                    Toast.makeText(this, "Please fill all details correctly", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Create account and send verification link
                auth.createUserWithEmailAndPassword(emailStr, passStr)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            
                            // 1. Send verification email link
                            user?.sendEmailVerification()?.addOnCompleteListener { verifyTask ->
                                if (verifyTask.isSuccessful) {
                                    Toast.makeText(this, "Verification link sent to $emailStr", Toast.LENGTH_LONG).show()
                                }
                            }

                            // 2. Store user data in Firestore
                            val userData = hashMapOf(
                                "uid" to user?.uid,
                                "name" to nameStr,
                                "phone" to contactStr,
                                "aadhaar" to aadhaarStr,
                                "pan" to panStr,
                                "occupation" to occupationStr,
                                "gender" to genderStr,
                                "dob" to selectedDob,
                                "email" to emailStr
                            )

                            user?.uid?.let { uid ->
                                db.collection("users").document(uid)
                                    .set(userData)
                                    .addOnSuccessListener {
                                        Log.d(tag, "User profile created in Firestore")
                                        auth.signOut()
                                        Toast.makeText(this, "Account created. Verify email link to login.", Toast.LENGTH_LONG).show()
                                        
                                        // Toggle back to login
                                        isLogin = true
                                        updateUI()
                                    }
                            }
                        } else {
                            val exception = task.exception
                            if (exception is FirebaseAuthUserCollisionException) {
                                Toast.makeText(this, "Account already exists.", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this, "Error: ${exception?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
            }
        }

        switchText.setOnClickListener {
            isLogin = !isLogin
            updateUI()
        }
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

        val visibility = if (isLogin) View.GONE else View.VISIBLE
        name.visibility = visibility
        contact.visibility = visibility
        aadhaar.visibility = visibility
        pan.visibility = visibility
        occupation.visibility = visibility
        dobInput.visibility = visibility
        genderSpinner.visibility = visibility

        button.text = if (isLogin) "Login" else "Sign Up"
        switchText.text = if (isLogin) "Don't have an account? Sign Up" else "Already have an account? Login"
    }
}
