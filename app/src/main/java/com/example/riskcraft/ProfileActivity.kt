package com.example.riskcraft

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.storage.FirebaseStorage

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var profileImage: ImageView

    private val pickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val imageUri = result.data?.data
            if (imageUri != null) {
                uploadImageToFirebase(imageUri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        profileImage = findViewById(R.id.profileImage)
        val editImageBtn = findViewById<ImageView>(R.id.editImageBtn)
        val logoutBtn = findViewById<Button>(R.id.logoutBtn)
        val changePinBtn = findViewById<Button>(R.id.changePinBtn)
        val backBtn = findViewById<ImageView>(R.id.backBtn)

        listenToUserData()
        updateWalletInfo()

        backBtn.setOnClickListener { finish() }

        editImageBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImage.launch(intent)
        }

        changePinBtn.setOnClickListener {
            startActivity(Intent(this, CreatePinActivity::class.java))
        }

        logoutBtn.setOnClickListener {
            auth.signOut()
            val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
            prefs.edit().clear().apply()
            // Clear cached profile data on logout
            getSharedPreferences("riskcraft_profile_cache", MODE_PRIVATE).edit().clear().apply()
            startActivity(Intent(this, MainActivity::class.java))
            finishAffinity()
        }
    }

    private fun updateWalletInfo() {
        val balance = WalletManager.getBalance(this)
        findViewById<TextView>(R.id.walletBalanceText)?.text = String.format("₹%,.0f", balance)
    }

    private fun listenToUserData() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        // Immediately show cached profile data (survives app data/cache clears if login re-caches)
        loadCachedProfile()

        // Always do an explicit server fetch first to guarantee fresh data
        // (handles case where login cache failed or data was cleared)
        db.collection("users").document(uid).get(com.google.firebase.firestore.Source.SERVER)
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    applyAndCacheSnapshot(doc)
                }
            }
            .addOnFailureListener { e ->
                Log.w("ProfileActivity", "Server fetch failed, trying cache: ${e.message}")
                // Fall back to Firestore local cache
                db.collection("users").document(uid).get(com.google.firebase.firestore.Source.CACHE)
                    .addOnSuccessListener { doc ->
                        if (doc != null && doc.exists()) {
                            applyAndCacheSnapshot(doc)
                        }
                    }
            }

        // Also keep a real-time listener for live updates (e.g. profile image changes)
        db.collection("users").document(uid).addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("ProfileActivity", "Listen failed.", e)
                if (e.code == FirebaseFirestoreException.Code.UNAVAILABLE) {
                    Toast.makeText(this, "Offline - showing cached data", Toast.LENGTH_SHORT).show()
                }
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                applyAndCacheSnapshot(snapshot)
            }
        }
    }

    /**
     * Applies Firestore document fields to the UI and caches them locally.
     */
    private fun applyAndCacheSnapshot(doc: com.google.firebase.firestore.DocumentSnapshot) {
        val name = doc.getString("name")
        val dob = doc.getString("dob")
        val gender = doc.getString("gender")
        val occupation = doc.getString("occupation")
        val aadhaar = doc.getString("aadhaar")
        val pan = doc.getString("pan")
        val email = doc.getString("email")
        val phone = doc.getString("phone")
        val imageUrl = doc.getString("profileImageUrl")

        setInfoItem(R.id.nameItem, "FULL NAME", name)
        setInfoItem(R.id.dobItem, "DATE OF BIRTH", dob)
        setInfoItem(R.id.genderItem, "GENDER", gender)
        setInfoItem(R.id.occupationItem, "OCCUPATION", occupation)
        setInfoItem(R.id.aadhaarItem, "AADHAAR NUMBER", aadhaar)
        setInfoItem(R.id.panItem, "PAN NUMBER", pan)
        setInfoItem(R.id.emailItem, "EMAIL ADDRESS", email)
        setInfoItem(R.id.phoneItem, "CONTACT NUMBER", phone)

        // Cache profile data locally for future fallback
        FirestoreManager.cacheProfileData(this, mapOf(
            "name" to name,
            "dob" to dob,
            "gender" to gender,
            "occupation" to occupation,
            "aadhaar" to aadhaar,
            "pan" to pan,
            "email" to email,
            "phone" to phone,
            "profileImageUrl" to imageUrl
        ))

        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(imageUrl)
                .circleCrop()
                .placeholder(R.drawable.ic_profile)
                .into(profileImage)
        }
    }

    /**
     * Loads profile fields from local SharedPreferences cache.
     * This is the fallback when Firestore data is not yet available.
     */
    private fun loadCachedProfile() {
        setInfoItem(R.id.nameItem, "FULL NAME",
            FirestoreManager.getCachedProfileField(this, "name"))
        setInfoItem(R.id.dobItem, "DATE OF BIRTH",
            FirestoreManager.getCachedProfileField(this, "dob"))
        setInfoItem(R.id.genderItem, "GENDER",
            FirestoreManager.getCachedProfileField(this, "gender"))
        setInfoItem(R.id.occupationItem, "OCCUPATION",
            FirestoreManager.getCachedProfileField(this, "occupation"))
        setInfoItem(R.id.aadhaarItem, "AADHAAR NUMBER",
            FirestoreManager.getCachedProfileField(this, "aadhaar"))
        setInfoItem(R.id.panItem, "PAN NUMBER",
            FirestoreManager.getCachedProfileField(this, "pan"))
        setInfoItem(R.id.emailItem, "EMAIL ADDRESS",
            FirestoreManager.getCachedProfileField(this, "email"))
        setInfoItem(R.id.phoneItem, "CONTACT NUMBER",
            FirestoreManager.getCachedProfileField(this, "phone"))

        val cachedImageUrl = FirestoreManager.getCachedProfileField(this, "profileImageUrl")
        if (!cachedImageUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(cachedImageUrl)
                .circleCrop()
                .placeholder(R.drawable.ic_profile)
                .into(profileImage)
        }
    }

    private fun setInfoItem(itemId: Int, labelText: String, valueText: String?) {
        val itemView = findViewById<View>(itemId)
        if (itemView != null) {
            val labelTv = itemView.findViewById<TextView>(R.id.label)
            val valueTv = itemView.findViewById<TextView>(R.id.value)
            labelTv?.text = labelText
            // Only update if we have a non-null value, or if currently showing N/A
            val currentValue = valueTv?.text?.toString()
            if (valueText != null) {
                valueTv?.text = valueText
            } else if (currentValue.isNullOrEmpty() || currentValue == "N/A") {
                valueTv?.text = "N/A"
            }
        }
    }

    private fun uploadImageToFirebase(uri: Uri) {
        val uid = auth.currentUser?.uid ?: return
        val ref = storage.reference.child("profile_images/$uid.jpg")
        Toast.makeText(this, "Uploading image...", Toast.LENGTH_SHORT).show()

        ref.putFile(uri)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { downloadUri ->
                    saveImageUrlToFirestore(downloadUri.toString())
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Upload failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveImageUrlToFirestore(url: String) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid)
            .update("profileImageUrl", url)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile picture updated! 📸", Toast.LENGTH_SHORT).show()
            }
    }
}
