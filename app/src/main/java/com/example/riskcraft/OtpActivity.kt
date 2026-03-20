   package com.example.riskcraft

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit

class OtpActivity : AppCompatActivity() {

    private val tag = "OtpActivity"
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var storedVerificationId: String? = null
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val phone = intent.getStringExtra("phone") ?: ""

        val otpInput = findViewById<EditText>(R.id.otpInput)
        val verifyBtn = findViewById<Button>(R.id.verifyBtn)
        val subtitle = findViewById<TextView>(R.id.otpSubtitle)

        if (phone.isNotEmpty()) {
            subtitle.text = "We sent a 6-digit code to +91$phone"
            sendOtp(phone)
        } else {
            Toast.makeText(this, "Phone number missing", Toast.LENGTH_SHORT).show()
            finish()
        }

        verifyBtn.setOnClickListener {
            val otp = otpInput.text.toString()
            if (otp.length == 6) {
                if (storedVerificationId != null) {
                    verifyOtp(otp)
                } else {
                    Toast.makeText(this, "Verification ID not found. Please resend OTP.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Enter valid 6-digit OTP", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendOtp(phone: String) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber("+91$phone")
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            Log.d(tag, "onVerificationCompleted")
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            Log.w(tag, "onVerificationFailed", e)
            val message = when (e) {
                is FirebaseAuthInvalidCredentialsException -> "Invalid phone number format."
                is FirebaseTooManyRequestsException -> "SMS quota exceeded. Try again later."
                else -> e.message ?: "Verification failed"
            }
            Toast.makeText(this@OtpActivity, message, Toast.LENGTH_LONG).show()
        }

        override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
            Log.d(tag, "onCodeSent")
            storedVerificationId = verificationId
            resendToken = token
            Toast.makeText(this@OtpActivity, "OTP Sent ✅", Toast.LENGTH_SHORT).show()
        }
    }

    private fun verifyOtp(code: String) {
        val credential = PhoneAuthProvider.getCredential(storedVerificationId!!, code)
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        val email = intent.getStringExtra("email") ?: ""
        val password = intent.getStringExtra("password") ?: ""

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.linkWithCredential(credential)
                        ?.addOnSuccessListener {
                            saveUserDataToFirestore(user.uid)
                        }
                        ?.addOnFailureListener {
                            saveUserDataToFirestore(user?.uid ?: "")
                        }
                } else {
                    val exception = task.exception
                    if (exception is FirebaseAuthUserCollisionException) {
                        Toast.makeText(this, "Email already exists. Try logging in.", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this, "Signup Error: ${exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
    }

    private fun saveUserDataToFirestore(uid: String) {
        if (uid.isEmpty()) return

        val userData = hashMapOf(
            "uid" to uid,
            "name" to intent.getStringExtra("name"),
            "phone" to intent.getStringExtra("phone"),
            "aadhaar" to intent.getStringExtra("aadhaar"),
            "pan" to intent.getStringExtra("pan"),
            "occupation" to intent.getStringExtra("occupation"),
            "gender" to intent.getStringExtra("gender"),
            "dob" to intent.getStringExtra("dob"),
            "email" to intent.getStringExtra("email"),
            "walletBalance" to WalletManager.STARTING_BALANCE,
            "totalProfit" to 0.0,
            "totalTrades" to 0,
            "completedChallenges" to emptyList<String>(),
            "joinedAt" to com.google.firebase.Timestamp.now()
        )

        db.collection("users").document(uid)
            .set(userData)
            .addOnSuccessListener {
                Toast.makeText(this, "Account Created Successfully! 🎉", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, CreatePinActivity::class.java))
                finishAffinity()
            }
            .addOnFailureListener { e ->
                Log.e(tag, "Firestore error", e)
                Toast.makeText(this, "Data Save Error: ${e.message}", Toast.LENGTH_LONG).show()
                startActivity(Intent(this, CreatePinActivity::class.java))
                finishAffinity()
            }
    }
}
