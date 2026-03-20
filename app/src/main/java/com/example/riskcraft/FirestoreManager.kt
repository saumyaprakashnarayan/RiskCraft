package com.example.riskcraft

import android.content.Context
import android.util.Log
import com.example.riskcraft.model.LeaderboardEntry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source

/**
 * FirestoreManager handles all cloud data storage:
 *
 * Firestore Structure:
 * ─────────────────────
 * users/{uid}
 *   ├── name, email, phone, dob, gender, occupation, aadhaar, pan
 *   ├── profileImageUrl
 *   ├── walletBalance (Double)
 *   ├── totalProfit (Double)
 *   ├── totalTrades (Int)
 *   ├── joinedAt (Timestamp)
 *   └── completedChallenges (List<String>) — IDs of completed challenges
 *
 * challenges/{challengeId}
 *   ├── title, description, reward, type
 *   ├── targetValue (Double) — e.g., 10% profit or 20 trades
 *   ├── days (Int)
 *   └── active (Boolean)
 *
 * leaderboard — computed from users collection, sorted by totalProfit
 */
object FirestoreManager {

    private const val TAG = "FirestoreManager"
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // ─── WALLET SYNC ───────────────────────────────────────────────

    /**
     * Syncs local wallet data (balance, profit, trade count) to Firestore
     * so leaderboard and profile show real values.
     */
    fun syncWalletToCloud(context: Context) {
        val uid = auth.currentUser?.uid ?: return
        val balance = WalletManager.getBalance(context)
        val profit = WalletManager.getTotalPnL(context)
        val trades = WalletManager.getOrders(context).size

        db.collection("users").document(uid).update(
            mapOf(
                "walletBalance" to balance,
                "totalProfit" to profit,
                "totalTrades" to trades
            )
        ).addOnFailureListener { e ->
            Log.w(TAG, "Wallet sync failed: ${e.message}")
            // If document doesn't exist yet (field missing), use set with merge
            db.collection("users").document(uid).set(
                mapOf(
                    "walletBalance" to balance,
                    "totalProfit" to profit,
                    "totalTrades" to trades
                ), com.google.firebase.firestore.SetOptions.merge()
            )
        }
    }

    // ─── LEADERBOARD ───────────────────────────────────────────────

    /**
     * Fetches top users by totalProfit from Firestore.
     * Falls back to hardcoded demo data if no users exist yet.
     */
    fun fetchLeaderboard(callback: (List<LeaderboardEntry>) -> Unit) {
        db.collection("users")
            .orderBy("totalProfit", Query.Direction.DESCENDING)
            .limit(20)
            .get()
            .addOnSuccessListener { snapshot ->
                val entries = mutableListOf<LeaderboardEntry>()
                var rank = 1
                for (doc in snapshot.documents) {
                    val name = doc.getString("name") ?: "Unknown"
                    val profit = doc.getDouble("totalProfit") ?: 0.0
                    val imageUrl = doc.getString("profileImageUrl") ?: ""
                    entries.add(LeaderboardEntry(doc.id, name, imageUrl, profit, rank))
                    rank++
                }

                if (entries.isEmpty()) {
                    // Return demo data if no users have traded yet
                    callback(getDemoLeaderboard())
                } else {
                    callback(entries)
                }
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Leaderboard fetch failed: ${e.message}")
                callback(getDemoLeaderboard())
            }
    }

    private fun getDemoLeaderboard(): List<LeaderboardEntry> {
        return listOf(
            LeaderboardEntry("demo1", "Arjun Mehta", "", 245000.0, 1),
            LeaderboardEntry("demo2", "Priya Sharma", "", 198500.0, 2),
            LeaderboardEntry("demo3", "Rahul Verma", "", 156700.0, 3),
            LeaderboardEntry("demo4", "Sneha Patel", "", 124300.0, 4),
            LeaderboardEntry("demo5", "Vikram Singh", "", 98700.0, 5),
            LeaderboardEntry("demo6", "Ananya Gupta", "", 87500.0, 6),
            LeaderboardEntry("demo7", "Karthik Nair", "", 76200.0, 7),
            LeaderboardEntry("demo8", "Divya Reddy", "", 65400.0, 8),
            LeaderboardEntry("demo9", "Manish Kumar", "", 54100.0, 9),
            LeaderboardEntry("demo10", "Pooja Desai", "", 42800.0, 10)
        )
    }

    // ─── CHALLENGES ────────────────────────────────────────────────

    /**
     * Checks which challenges the user has completed based on their trading data.
     * Updates Firestore with completed challenge IDs.
     */
    fun evaluateChallenges(context: Context, callback: (Map<String, Boolean>) -> Unit) {
        val uid = auth.currentUser?.uid ?: return callback(emptyMap())
        val profit = WalletManager.getTotalPnL(context)
        val profitPercent = if (WalletManager.getInvestedValue(context) > 0)
            (profit / WalletManager.getInvestedValue(context)) * 100 else 0.0
        val trades = WalletManager.getOrders(context).size
        val positions = WalletManager.getPositions(context).size

        // Evaluate each challenge
        val results = mutableMapOf<String, Boolean>()
        results["1"] = profitPercent >= 10     // Profit Master: 10% profit
        results["2"] = trades >= 20            // Volume Trader: 20 trades
        results["3"] = profit > 0              // Consistency King: positive P&L
        results["4"] = positions >= 5          // Diversifier: 5 different stocks

        // Save completed ones to Firestore
        val completed = results.filter { it.value }.keys.toList()
        db.collection("users").document(uid).set(
            mapOf("completedChallenges" to completed),
            com.google.firebase.firestore.SetOptions.merge()
        )

        callback(results)
    }

    /**
     * Fetches completed challenge IDs for the current user from Firestore.
     */
    fun getCompletedChallenges(callback: (List<String>) -> Unit) {
        val uid = auth.currentUser?.uid ?: return callback(emptyList())

        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                @Suppress("UNCHECKED_CAST")
                val completed = doc.get("completedChallenges") as? List<String> ?: emptyList()
                callback(completed)
            }
            .addOnFailureListener {
                callback(emptyList())
            }
    }

    // ─── USER PROFILE CACHE ──────────────────────────────────────

    private const val PROFILE_PREFS = "riskcraft_profile_cache"
    private val PROFILE_FIELDS = listOf("name", "phone", "aadhaar", "pan", "occupation", "gender", "dob", "email", "profileImageUrl")

    /**
     * Fetches user profile from Firestore SERVER (not cache) and saves to SharedPreferences.
     * Call this after login to ensure profile data survives app data clears.
     */
    fun fetchAndCacheProfile(context: Context, onComplete: (() -> Unit)? = null) {
        val uid = auth.currentUser?.uid ?: return

        db.collection("users").document(uid).get(Source.SERVER)
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    val prefs = context.getSharedPreferences(PROFILE_PREFS, Context.MODE_PRIVATE)
                    val editor = prefs.edit()
                    for (field in PROFILE_FIELDS) {
                        val value = doc.getString(field)
                        if (value != null) {
                            editor.putString(field, value)
                        }
                    }
                    editor.apply()
                    Log.d(TAG, "Profile cached locally after login")
                }
                onComplete?.invoke()
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Failed to fetch profile from server: ${e.message}")
                onComplete?.invoke()
            }
    }

    /**
     * Returns cached profile field from SharedPreferences.
     * Used as fallback when Firestore returns null.
     */
    fun getCachedProfileField(context: Context, field: String): String? {
        val prefs = context.getSharedPreferences(PROFILE_PREFS, Context.MODE_PRIVATE)
        return prefs.getString(field, null)
    }

    /**
     * Saves a single profile field to the local cache.
     */
    fun cacheProfileField(context: Context, field: String, value: String?) {
        if (value != null) {
            val prefs = context.getSharedPreferences(PROFILE_PREFS, Context.MODE_PRIVATE)
            prefs.edit().putString(field, value).apply()
        }
    }

    /**
     * Saves all profile fields from a Firestore snapshot to local cache.
     */
    fun cacheProfileData(context: Context, data: Map<String, String?>) {
        val prefs = context.getSharedPreferences(PROFILE_PREFS, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        for ((field, value) in data) {
            if (value != null) {
                editor.putString(field, value)
            }
        }
        editor.apply()
    }

    // ─── USER PROFILE INIT ────────────────────────────────────────

    /**
     * Called after signup to initialize wallet fields in user document.
     */
    fun initializeUserProfile(uid: String) {
        db.collection("users").document(uid).set(
            mapOf(
                "walletBalance" to WalletManager.STARTING_BALANCE,
                "totalProfit" to 0.0,
                "totalTrades" to 0,
                "completedChallenges" to emptyList<String>(),
                "joinedAt" to com.google.firebase.Timestamp.now()
            ), com.google.firebase.firestore.SetOptions.merge()
        )
    }
}
