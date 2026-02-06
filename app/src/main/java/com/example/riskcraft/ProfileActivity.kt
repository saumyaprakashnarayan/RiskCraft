package com.example.riskcraft

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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

        loadUserData()

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
            startActivity(Intent(this, MainActivity::class.java))
            finishAffinity()
        }
    }

    private fun loadUserData() {
        val uid = auth.currentUser?.uid ?: return
        
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    setInfoItem(R.id.nameItem, "FULL NAME", doc.getString("name"))
                    setInfoItem(R.id.dobItem, "DATE OF BIRTH", doc.getString("dob"))
                    setInfoItem(R.id.genderItem, "GENDER", doc.getString("gender"))
                    setInfoItem(R.id.occupationItem, "OCCUPATION", doc.getString("occupation"))
                    setInfoItem(R.id.aadhaarItem, "AADHAAR NUMBER", doc.getString("aadhaar"))
                    setInfoItem(R.id.panItem, "PAN NUMBER", doc.getString("pan"))
                    setInfoItem(R.id.emailItem, "EMAIL ADDRESS", doc.getString("email"))
                    setInfoItem(R.id.phoneItem, "CONTACT NUMBER", doc.getString("phone"))

                    val imageUrl = doc.getString("profileImageUrl")
                    if (!imageUrl.isNullOrEmpty()) {
                        Glide.with(this).load(imageUrl).placeholder(R.drawable.ic_profile).into(profileImage)
                    }
                }
            }
    }

    private fun setInfoItem(itemId: Int, label: String, value: String?) {
        findViewById<android.view.View>(itemId).apply {
            findViewById<TextView>(R.id.label).text = label
            findViewById<TextView>(R.id.value).text = value ?: "N/A"
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
                Glide.with(this).load(url).into(profileImage)
                Toast.makeText(this, "Profile picture updated!", Toast.LENGTH_SHORT).show()
            }
    }
}
