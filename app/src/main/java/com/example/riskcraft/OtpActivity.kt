package com.example.riskcraft

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import java.util.concurrent.TimeUnit

class OtpActivity : AppCompatActivity() {

    private val tag = "OtpActivity"
    private lateinit var auth: FirebaseAuth
    private var storedVerificationId: String? = null
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp)

        auth = FirebaseAuth.getInstance()

        val phone = intent.getStringExtra("phone") ?: ""
        val email = intent.getStringExtra("email") ?: ""
        val password = intent.getStringExtra("password") ?: ""

        val otpInput = findViewById<EditText>(R.id.otpInput)
        val verifyBtn = findViewById<Button>(R.id.verifyBtn)

        if (phone.isNotEmpty()) {
            sendOtp(phone)
        } else {
            Toast.makeText(this, "Phone number missing", Toast.LENGTH_SHORT).show()
            finish()
        }

        verifyBtn.setOnClickListener {
            val otp = otpInput.text.toString()
            if (otp.length == 6) {
                if (storedVerificationId != null) {
                    verifyOtp(otp, email, password)
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
            Log.d(tag, "onVerificationCompleted:$credential")
            val email = intent.getStringExtra("email") ?: ""
            val password = intent.getStringExtra("password") ?: ""
            signInWithPhoneAuthCredential(credential, email, password)
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

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            Log.d(tag, "onCodeSent:$verificationId")
            storedVerificationId = verificationId
            resendToken = token
            Toast.makeText(this@OtpActivity, "OTP Sent", Toast.LENGTH_SHORT).show()
        }
    }

    private fun verifyOtp(code: String, email: String, pass: String) {
        val credential = PhoneAuthProvider.getCredential(storedVerificationId!!, code)
        signInWithPhoneAuthCredential(credential, email, pass)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential, email: String, pass: String) {
        // Step 1: Sign in with Email first (if it's a new account, we create it)
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Step 2: Link the Phone Credential to the newly created Email account
                    val user = auth.currentUser
                    user?.linkWithCredential(credential)
                        ?.addOnSuccessListener {
                            Toast.makeText(this, "Signup Successful!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, CreatePinActivity::class.java))
                            finish()
                        }
                        ?.addOnFailureListener { e ->
                            Log.e(tag, "Linking failed", e)
                            // If linking fails, the user is still created with Email
                            Toast.makeText(this, "Email created, but Phone link failed: ${e.message}", Toast.LENGTH_LONG).show()
                            startActivity(Intent(this, CreatePinActivity::class.java))
                            finish()
                        }
                } else {
                    val exception = task.exception
                    if (exception is FirebaseAuthUserCollisionException) {
                        // User already exists with this email, maybe try linking if not already linked
                        Toast.makeText(this, "Account already exists. Please Login.", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this, "Signup Error: ${exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
    }
}
