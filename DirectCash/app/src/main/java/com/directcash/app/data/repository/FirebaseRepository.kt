package com.directcash.app.data.repository

import com.directcash.app.data.model.Task
import com.directcash.app.data.model.Transaction
import com.directcash.app.data.model.TransactionStatus
import com.directcash.app.data.model.TransactionType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit
import android.app.Activity
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdsClick
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

data class UserStats(
    val name: String = "",
    val walletBalance: Double = 0.0,
    val totalEarned: Double = 0.0,
    val dailySpinCount: Int = 0,
    val lastSpinDate: String = "",
    val referralCode: String = "",
    val currentStreak: Int = 0,
    val lastCheckInTimestamp: Long = 0L,
    val referredBy: String = "",
    val referredByLevel2: String = "",
    val referredByLevel3: String = "",
    val dailyAdLimit: Int = 10,
    val dailyScratchCount: Int = 0,
    val dailyCaptchaCount: Int = 0,
    val dailyQuizCount: Int = 0,
    val lastTaskDate: String = ""
)

data class OfferwallSettings(
    val pubscaleAppId: String = "",
    val pubscaleSecretKey: String = "",
    val monlixApiKey: String = "",
    val pointsRatio: Double = 1000.0,
    val isPubscaleEnabled: Boolean = false,
    val isMonlixEnabled: Boolean = false,
    val isNotikEnabled: Boolean = false,
    val cpagripUserId: String = "",
    val cpagripPubKey: String = "",
    val cpagripUrl: String = "",
    val cpaleadSourceId: String = "",
    val cpaleadApiKey: String = "",
    val adsterraPublisherId: String = "",
    val adsterraCpaToken: String = "",
    val isCpagripEnabled: Boolean = false,
    val isCpaleadEnabled: Boolean = false,
    val isAdsterraEnabled: Boolean = false
)

data class Referral(
    val id: String = "",
    val name: String = "",
    val amount: Double = 0.0,
    val timestamp: Long = 0L
)

data class UniversalAdSettings(
    val activeNetwork: String = "UNITY",
    val primaryAppId: String = "",
    val rewardedPlacementId: String = "Rewarded_Android",
    val interstitialPlacementId: String = "Interstitial_Android",
    val bannerPlacementId: String = "Banner_Android",
    val isAdsEnabled: Boolean = true
)

data class AdminSettings(
    val minWithdrawalLimit: Double = 100.0,
    val maintenanceMode: Boolean = false,
    val dailyAdLimit: Int = 10,
    val totalSeizedFunds: Double = 0.0,
    val rewardedVideoRate: Double = 0.05,
    val processingCommissionFee: Double = 5.0,
    val totalGrossRevenue: Double = 0.0,
    val totalDisbursements: Double = 0.0,
    // Super Task Hub Settings
    val scratchDailyLimit: Int = 20,
    val scratchMaxReward: Double = 1.0,
    val offerwallMultiplier: Double = 1.0,
    val captchaReward: Double = 0.02,
    val mathQuizReward: Double = 0.02,
    val appInstallReward: Double = 5.0,
    val isOfferwallEnabled: Boolean = true,
    // Ad Settings
    val ads: UniversalAdSettings = UniversalAdSettings()
)

class FirebaseRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    val userId: String? get() = auth.currentUser?.uid

    /**
     * Helper to safely extract double values from Firestore documents.
     */
    private fun getSafeDouble(doc: com.google.firebase.firestore.DocumentSnapshot?, field: String, default: Double): Double {
        val value = doc?.get(field)
        return when (value) {
            is Number -> {
                val d = value.toDouble()
                if (d.isNaN() || d.isInfinite()) default else d
            }
            else -> default
        }
    }

    /**
     * Helper to safely extract integer values from Firestore documents.
     */
    private fun getSafeInt(doc: com.google.firebase.firestore.DocumentSnapshot?, field: String, default: Int): Int {
        val value = doc?.get(field)
        return when (value) {
            is Number -> value.toInt()
            else -> default
        }
    }

    /**
     * Consolidated real-time stats observer.
     */
    fun observeUserStats(): Flow<UserStats> = callbackFlow {
        val uid = userId
        if (uid == null) {
            trySend(UserStats(name = "Guest"))
            awaitClose { }
            return@callbackFlow
        }
        
        val listener = db.collection("users")
            .whereEqualTo("uid", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                
                if (snapshot != null && !snapshot.isEmpty) {
                    val doc = snapshot.documents[0]
                    val stats = UserStats(
                        name = doc.getString("name") ?: "User",
                        walletBalance = getSafeDouble(doc, "wallet_balance", 0.0),
                        totalEarned = getSafeDouble(doc, "total_earned", 0.0),
                        dailySpinCount = getSafeInt(doc, "daily_spin_count", 0),
                        lastSpinDate = doc.getString("last_spin_date") ?: "",
                        referralCode = doc.getString("referral_code") ?: "",
                        currentStreak = getSafeInt(doc, "current_streak", 0),
                        lastCheckInTimestamp = doc.getTimestamp("last_check_in_date")?.toDate()?.time ?: 0L,
                        referredBy = doc.getString("referred_by") ?: "",
                        referredByLevel2 = doc.getString("referred_by_level2") ?: "",
                        referredByLevel3 = doc.getString("referred_by_level3") ?: "",
                        dailyAdLimit = getSafeInt(doc, "daily_ad_limit", 10),
                        dailyScratchCount = getSafeInt(doc, "daily_scratch_count", 0),
                        dailyCaptchaCount = getSafeInt(doc, "daily_captcha_count", 0),
                        dailyQuizCount = getSafeInt(doc, "daily_quiz_count", 0),
                        lastTaskDate = doc.getString("last_task_date") ?: ""
                    )
                    trySend(stats)
                } else {
                    trySend(UserStats(name = "User"))
                }
            }
        awaitClose { listener.remove() }
    }

    fun observeAppMaintenance(): Flow<Boolean> = callbackFlow {
        val listener = db.collection("admin_config").document("settings")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                val isMaintenance = snapshot?.getBoolean("is_maintenance") ?: false
                trySend(isMaintenance)
            }
        awaitClose { listener.remove() }
    }

    fun observeOfferwallSettings(): Flow<OfferwallSettings> = callbackFlow {
        val listener = db.collection("admin_config").document("offerwall_settings")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val settings = OfferwallSettings(
                        pubscaleAppId = snapshot.getString("pubscale_app_id") ?: "",
                        pubscaleSecretKey = snapshot.getString("pubscale_secret_key") ?: "",
                        monlixApiKey = snapshot.getString("monlix_api_key") ?: "",
                        pointsRatio = getSafeDouble(snapshot, "global_app_currency_ratio", 1000.0),
                        isPubscaleEnabled = snapshot.getBoolean("enable_pubscale") ?: false,
                        isMonlixEnabled = snapshot.getBoolean("enable_monlix") ?: false,
                        isNotikEnabled = snapshot.getBoolean("enable_notik") ?: false,
                        cpagripUserId = snapshot.getString("cpagrip_user_id") ?: "",
                        cpagripPubKey = snapshot.getString("cpagrip_pubkey") ?: "",
                        cpagripUrl = snapshot.getString("cpagrip_url") ?: "https://filestrue.com/script_include.php?id=1903788",
                        cpaleadSourceId = snapshot.getString("cpalead_source_id") ?: "",
                        cpaleadApiKey = snapshot.getString("cpalead_api_key") ?: "",
                        adsterraPublisherId = snapshot.getString("adsterra_publisher_id") ?: "",
                        adsterraCpaToken = snapshot.getString("adsterra_cpa_token") ?: "",
                        isCpagripEnabled = snapshot.getBoolean("enable_cpagrip") ?: false,
                        isCpaleadEnabled = snapshot.getBoolean("enable_cpalead") ?: false,
                        isAdsterraEnabled = snapshot.getBoolean("enable_adsterra") ?: false
                    )
                    trySend(settings)
                } else {
                    trySend(OfferwallSettings())
                }
            }
        awaitClose { listener.remove() }
    }

    fun getTodayTasks(): Flow<List<Task>> = callbackFlow {
        val uid = userId
        val listener = db.collection("tasks")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                
                scope.launch {
                    try {
                        val completedTasks = if (uid != null) {
                            db.collection("users").document(uid).collection("completed_tasks")
                                .get().await().documents.map { it.id }
                        } else emptyList()

                        val tasks = snapshot?.documents?.mapNotNull { doc ->
                            val taskId = getSafeInt(doc, "id", 0).toLong()
                            if (completedTasks.contains(taskId.toString())) return@mapNotNull null
                        
                            Task(
                                id = taskId,
                                title = doc.getString("title") ?: "New Task",
                                reward = getSafeInt(doc, "reward", 0).toLong(),
                                description = doc.getString("description") ?: "Complete this task to earn rewards",
                                url = doc.getString("url") ?: ""
                            )
                        } ?: emptyList()
                        trySend(tasks)
                    } catch (e: Exception) {
                        trySend(emptyList())
                    }
                }
            }
        awaitClose { listener.remove() }
    }

    fun getTransactions(): Flow<List<Transaction>> = callbackFlow {
        val uid = userId
        if (uid == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }
        val listener = db.collection("withdrawals")
            .whereEqualTo("userId", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                
                try {
                    val withdrawals = snapshot?.documents?.mapNotNull { doc ->
                        val amount = getSafeDouble(doc, "amount", 0.0)
                        val statusStr = doc.getString("status") ?: "Pending"
                        val status = try { TransactionStatus.valueOf(statusStr.uppercase()) } catch (e: Exception) { TransactionStatus.PENDING }
                        val timestamp = doc.getTimestamp("timestamp")?.toDate()?.time ?: System.currentTimeMillis()
                        val upiId = doc.getString("upiId") ?: "UPI"

                        Transaction(
                            id = doc.id,
                            amount = amount,
                            type = TransactionType.DEBIT,
                            status = status,
                            timestamp = timestamp,
                            description = "Withdrawal to $upiId"
                        )
                    }?.sortedByDescending { it.timestamp } ?: emptyList()
                    trySend(withdrawals)
                } catch (e: Exception) {
                    trySend(emptyList())
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun completeTask(taskId: Long, reward: Double, taskTitle: String) {
        val uid = userId ?: return
        val userRef = db.collection("users").document(uid)
        val completedTaskRef = userRef.collection("completed_tasks").document(taskId.toString())
        
        try {
            db.runTransaction { transaction ->
                val taskSnapshot = transaction.get(completedTaskRef)
                if (taskSnapshot.exists()) return@runTransaction // Already completed

                val userSnapshot = transaction.get(userRef)
                val currentBalance = userSnapshot.getDouble("wallet_balance") ?: 0.0
                val currentTotalEarned = userSnapshot.getDouble("total_earned") ?: 0.0

                transaction.update(userRef, "wallet_balance", currentBalance + reward)
                transaction.update(userRef, "total_earned", currentTotalEarned + reward)
                
                transaction.set(completedTaskRef, mapOf(
                    "timestamp" to FieldValue.serverTimestamp(),
                    "reward" to reward,
                    "title" to taskTitle
                ))

                // Add to withdrawals collection as a 'CREDIT' transaction for dashboard history
                val creditRef = db.collection("withdrawals").document() // Rename collection if needed, using existing structure
                transaction.set(creditRef, mapOf(
                    "userId" to uid,
                    "amount" to reward,
                    "type" to "CREDIT",
                    "status" to "SUCCESS",
                    "timestamp" to FieldValue.serverTimestamp(),
                    "description" to "Completed: $taskTitle"
                ))
            }.await()
        } catch (e: Exception) {
            Log.e("FIRESTORE_TEST", "completeTask failed for taskId: $taskId", e)
        }
    }

    fun observeAdminSettings(): Flow<AdminSettings> = callbackFlow {
        var lastSettings: com.google.firebase.firestore.DocumentSnapshot? = null
        var lastSeized: com.google.firebase.firestore.DocumentSnapshot? = null
        var lastAds: com.google.firebase.firestore.DocumentSnapshot? = null

        fun updateFlow(
            settings: com.google.firebase.firestore.DocumentSnapshot?, 
            seized: com.google.firebase.firestore.DocumentSnapshot?,
            ads: com.google.firebase.firestore.DocumentSnapshot?
        ) {
            if (settings != null) lastSettings = settings
            if (seized != null) lastSeized = seized
            if (ads != null) lastAds = ads
            
            val s = lastSettings
            val sz = lastSeized
            val a = lastAds
            
            // Robust parsing with NaN/Null handling
            fun getSafeDouble(doc: com.google.firebase.firestore.DocumentSnapshot?, field: String, default: Double): Double {
                val value = doc?.get(field)
                return when (value) {
                    is Number -> if (value.toDouble().isNaN()) default else value.toDouble()
                    else -> default
                }
            }

            fun getSafeInt(doc: com.google.firebase.firestore.DocumentSnapshot?, field: String, default: Int): Int {
                val value = doc?.get(field)
                return when (value) {
                    is Number -> value.toInt()
                    else -> default
                }
            }

            trySend(AdminSettings(
                minWithdrawalLimit = getSafeDouble(s, "min_withdrawal_limit", 100.0),
                maintenanceMode = s?.getBoolean("is_maintenance") ?: false,
                dailyAdLimit = getSafeInt(s, "daily_ad_limit", 10),
                totalSeizedFunds = getSafeDouble(sz, "total_seized_funds", 0.0),
                rewardedVideoRate = getSafeDouble(s, "rewarded_video_rate", 0.05),
                processingCommissionFee = getSafeDouble(s, "processing_commission_fee", 5.0),
                totalGrossRevenue = getSafeDouble(s, "total_gross_revenue", 0.0),
                totalDisbursements = getSafeDouble(s, "total_disbursements", 0.0),
                scratchDailyLimit = getSafeInt(s, "scratch_daily_limit", 20),
                scratchMaxReward = getSafeDouble(s, "scratch_max_reward", 1.0),
                offerwallMultiplier = getSafeDouble(s, "offerwall_multiplier", 1.0),
                captchaReward = getSafeDouble(s, "captcha_reward", 0.02),
                mathQuizReward = getSafeDouble(s, "math_quiz_reward", 0.02),
                appInstallReward = getSafeDouble(s, "app_install_reward", 5.0),
                isOfferwallEnabled = s?.getBoolean("is_offerwall_enabled") ?: true,
                ads = UniversalAdSettings(
                    activeNetwork = a?.getString("active_network")?.trim() ?: "UNITY",
                    primaryAppId = a?.getString("primary_game_or_app_id")?.trim() ?: "",
                    rewardedPlacementId = a?.getString("rewarded_placement_id")?.trim() ?: "Rewarded_Android",
                    interstitialPlacementId = a?.getString("interstitial_placement_id")?.trim() ?: "Interstitial_Android",
                    bannerPlacementId = a?.getString("banner_placement_id")?.trim() ?: "Banner_Android",
                    isAdsEnabled = a?.getBoolean("is_ads_enabled") ?: true
                )
            ))
        }

        val settingsListener = db.collection("admin_config").document("settings")
            .addSnapshotListener { snapshot, _ -> if (snapshot != null) updateFlow(snapshot, null, null) }
            
        val seizedListener = db.collection("admin_config").document("seized_wallet")
            .addSnapshotListener { snapshot, _ -> if (snapshot != null) updateFlow(null, snapshot, null) }

        val adsListener = db.collection("admin_config").document("universal_ads")
            .addSnapshotListener { snapshot, _ -> if (snapshot != null) updateFlow(null, null, snapshot) }

        awaitClose { 
            settingsListener.remove()
            seizedListener.remove()
            adsListener.remove()
        }
    }

    suspend fun updateAdminSettings(settings: AdminSettings) {
        val batch = db.batch()
        
        val settingsRef = db.collection("admin_config").document("settings")
        batch.set(settingsRef, mapOf(
            "min_withdrawal_limit" to settings.minWithdrawalLimit,
            "is_maintenance" to settings.maintenanceMode,
            "daily_ad_limit" to settings.dailyAdLimit,
            "rewarded_video_rate" to settings.rewardedVideoRate,
            "processing_commission_fee" to settings.processingCommissionFee,
            "total_gross_revenue" to settings.totalGrossRevenue,
            "total_disbursements" to settings.totalDisbursements,
            "scratch_daily_limit" to settings.scratchDailyLimit,
            "scratch_max_reward" to settings.scratchMaxReward,
            "offerwall_multiplier" to settings.offerwallMultiplier,
            "captcha_reward" to settings.captchaReward,
            "math_quiz_reward" to settings.mathQuizReward,
            "app_install_reward" to settings.appInstallReward,
            "is_offerwall_enabled" to settings.isOfferwallEnabled
        ), com.google.firebase.firestore.SetOptions.merge())

        val adsRef = db.collection("admin_config").document("universal_ads")
        batch.set(adsRef, mapOf(
            "active_network" to settings.ads.activeNetwork,
            "primary_game_or_app_id" to settings.ads.primaryAppId,
            "rewarded_placement_id" to settings.ads.rewardedPlacementId,
            "interstitial_placement_id" to settings.ads.interstitialPlacementId,
            "banner_placement_id" to settings.ads.bannerPlacementId,
            "is_ads_enabled" to settings.ads.isAdsEnabled
        ), com.google.firebase.firestore.SetOptions.merge())

        batch.commit().await()
    }

    suspend fun manageWithdrawal(requestId: String, userId: String, status: String, amount: Double) {
        val requestRef = db.collection("withdrawals").document(requestId)
        val userRef = db.collection("users").document(userId)
        val adminSettingsRef = db.collection("admin_config").document("settings")

        db.runTransaction { transaction ->
            if (status == "APPROVED") {
                transaction.update(requestRef, "status", "SUCCESS")
                transaction.update(adminSettingsRef, "total_disbursements", FieldValue.increment(amount))

                // Log the payout transaction explicitly
                val payoutLogRef = db.collection("payout_logs").document()
                transaction.set(payoutLogRef, mapOf(
                    "requestId" to requestId,
                    "userId" to userId,
                    "amount" to amount,
                    "timestamp" to FieldValue.serverTimestamp(),
                    "type" to "PAYOUT_APPROVED"
                ))
            } else if (status == "REJECTED") {
                transaction.update(requestRef, "status", "REJECTED")
                transaction.update(userRef, "wallet_balance", FieldValue.increment(amount))
                // Log rejection
                val payoutLogRef = db.collection("payout_logs").document()
                transaction.set(payoutLogRef, mapOf(
                    "requestId" to requestId,
                    "userId" to userId,
                    "amount" to amount,
                    "timestamp" to FieldValue.serverTimestamp(),
                    "type" to "PAYOUT_REJECTED"
                ))
            }
        }.await()
    }

    suspend fun banUserAndSeizeFunds(targetUid: String) {
        val userRef = db.collection("users").document(targetUid)
        val seizedWalletRef = db.collection("admin_config").document("seized_wallet")

        db.runTransaction { transaction ->
            val userSnapshot = transaction.get(userRef)
            val balance = userSnapshot.getDouble("wallet_balance") ?: 0.0

            transaction.update(userRef, "is_banned", true)
            
            if (balance > 0) {
                transaction.update(userRef, "wallet_balance", 0.0)
                transaction.update(seizedWalletRef, "total_seized_funds", FieldValue.increment(balance))
            }
        }.await()
    }

    suspend fun adjustUserWallet(targetUid: String, amount: Double, isCredit: Boolean) {
        val userRef = db.collection("users").document(targetUid)
        db.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            val currentBalance = snapshot.getDouble("wallet_balance") ?: 0.0
            val newBalance = if (isCredit) currentBalance + amount else (currentBalance - amount).coerceAtLeast(0.0)
            transaction.update(userRef, "wallet_balance", newBalance)
            
            // Log this adjustment in payouts/transactions log for audit
            val logRef = db.collection("admin_logs").document()
            transaction.set(logRef, mapOf(
                "type" to if (isCredit) "CREDIT_ADJUSTMENT" else "DEBIT_ADJUSTMENT",
                "targetUid" to targetUid,
                "amount" to amount,
                "timestamp" to FieldValue.serverTimestamp(),
                "adminId" to userId
            ))
        }.await()
    }

    suspend fun suspendUserTemp(targetUid: String) {
        val userRef = db.collection("users").document(targetUid)
        val expiry = System.currentTimeMillis() + (24 * 60 * 60 * 1000) // 24 Hours
        userRef.update(mapOf(
            "ban_expires_timestamp" to expiry,
            "is_temp_suspended" to true
        )).await()
    }

    suspend fun banUserPermanent(targetUid: String) {
        val userRef = db.collection("users").document(targetUid)
        val seizedWalletRef = db.collection("admin_config").document("seized_wallet")

        db.runTransaction { transaction ->
            val userSnapshot = transaction.get(userRef)
            val balance = userSnapshot.getDouble("wallet_balance") ?: 0.0

            transaction.update(userRef, mapOf(
                "is_banned" to true,
                "wallet_balance" to 0.0,
                "permanent_ban_timestamp" to FieldValue.serverTimestamp()
            ))
            
            if (balance > 0) {
                transaction.set(seizedWalletRef, mapOf(
                    "total_seized_funds" to FieldValue.increment(balance)
                ), com.google.firebase.firestore.SetOptions.merge())
            }
        }.await()
    }

    fun searchUser(queryStr: String): Flow<List<Map<String, Any>>> = callbackFlow {
        val q = db.collection("users")
            .whereGreaterThanOrEqualTo("name", queryStr)
            .whereLessThanOrEqualTo("name", queryStr + "\uf8ff")
            .limit(10)
        
        val listener = q.addSnapshotListener { snapshot, _ ->
            val users = snapshot?.documents?.map { doc ->
                val data = doc.data?.toMutableMap() ?: mutableMapOf()
                data["id"] = doc.id
                data
            } ?: emptyList()
            trySend(users)
        }
        awaitClose { listener.remove() }
    }

    fun getPendingWithdrawals(): Flow<List<Map<String, Any>>> = callbackFlow {
        val listener = db.collection("withdrawals")
            .whereEqualTo("status", "Pending")
            .addSnapshotListener { snapshot, _ ->
                val list = snapshot?.documents?.map { doc ->
                    val data = doc.data?.toMutableMap() ?: mutableMapOf()
                    data["id"] = doc.id
                    data
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun withdrawCash(upiId: String, amount: Double) {
        val uid = userId ?: return
        val userRef = db.collection("users").document(uid)
        val withdrawalRef = db.collection("withdrawals").document()

        try {
            db.runTransaction { transaction ->
                val snapshot = transaction.get(userRef)
                val currentBalance = if (snapshot.exists()) snapshot.getDouble("wallet_balance") ?: 0.0 else 0.0
                if (currentBalance >= amount) {
                    transaction.update(userRef, "wallet_balance", currentBalance - amount)
                    
                    val withdrawalData = mapOf(
                        "amount" to amount,
                        "status" to "Pending",
                        "timestamp" to FieldValue.serverTimestamp(),
                        "upiId" to upiId,
                        "userId" to uid
                    )
                    transaction.set(withdrawalRef, withdrawalData)
                }
            }.await()
        } catch (e: Exception) {
            Log.e("FIRESTORE_TEST", "withdrawCash failed", e)
        }
    }

    fun observeReferrals(): Flow<List<Referral>> = callbackFlow {
        val uid = userId
        if (uid == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }
        val listener = db.collection("users").document(uid).collection("referrals")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FIRESTORE_TEST", "observeReferrals listener error", error)
                    return@addSnapshotListener
                }
                try {
                    val referrals = snapshot?.documents?.mapNotNull { doc ->
                        Referral(
                            id = doc.id,
                            name = doc.getString("name") ?: "Friend",
                            amount = doc.getDouble("amount") ?: 10.0,
                            timestamp = doc.getTimestamp("timestamp")?.toDate()?.time ?: 0L
                        )
                    } ?: emptyList()
                    trySend(referrals)
                } catch (e: Exception) {
                    Log.e("FIRESTORE_TEST", "Error processing referrals", e)
                }
            }
        awaitClose { listener.remove() }
    }

    /**
     * Bulletproof, rock-solid Firestore transaction logic for 3-Level Referral Matrix.
     * Explicitly fetches parent fields sequentially using IDs to guarantee that all 3 levels credit properly.
     */
    suspend fun registerUserWithMultiLevelReferral(
        newUserId: String,
        newUserName: String,
        referralCodeEntered: String?
    ) {
        val usersRef = db.collection("users")
        val newUserRef = usersRef.document(newUserId)

        try {
            // Check if user already exists
            val existingSnapshot = newUserRef.get().await()
            if (existingSnapshot.exists()) return

            if (referralCodeEntered.isNullOrBlank()) {
                val uniqueCode = newUserName.take(3).uppercase().padEnd(3, 'X') + (100..999).random()
                val basicUserData = hashMapOf(
                    "uid" to newUserId,
                    "name" to newUserName,
                    "wallet_balance" to 0.0,
                    "total_earned" to 0.0,
                    "referred_by" to "",
                    "referred_by_level2" to "",
                    "referred_by_level3" to "",
                    "referral_code" to uniqueCode,
                    "current_streak" to 0,
                    "last_check_in_date" to null,
                    "daily_spin_count" to 0,
                    "last_spin_date" to "",
                    "daily_ad_limit" to 10
                )
                newUserRef.set(basicUserData).await()
                return
            }

            // Step 1: Find the Level 1 Parent outside the transaction to get their ID
            val lvl1Query = usersRef.whereEqualTo("referral_code", referralCodeEntered.uppercase()).get().await()
            val lvl1InitialDoc = lvl1Query.documents.firstOrNull()

            if (lvl1InitialDoc == null || !lvl1InitialDoc.exists()) {
                // Invalid code, treat as normal signup
                registerUserWithMultiLevelReferral(newUserId, newUserName, null)
                return
            }

            val parentId = lvl1InitialDoc.id

            db.runTransaction { transaction ->
                // Fetch L1 Parent Document inside transaction for consistency
                val lvl1DocRef = usersRef.document(parentId)
                val lvl1Snapshot = transaction.get(lvl1DocRef)
                
                var grandfatherId = ""
                var greatGrandfatherId = ""

                if (lvl1Snapshot.exists()) {
                    // Extract existing hierarchy structural fields from Level 1 Parent
                    grandfatherId = lvl1Snapshot.getString("referred_by") ?: ""
                    greatGrandfatherId = lvl1Snapshot.getString("referred_by_level2") ?: ""

                    // Reward Level 1 Parent (₹10)
                    val currentLvl1Bal = lvl1Snapshot.getDouble("wallet_balance") ?: 0.0
                    val currentLvl1Total = lvl1Snapshot.getDouble("total_earned") ?: 0.0
                    transaction.update(lvl1DocRef, "wallet_balance", currentLvl1Bal + 10.0)
                    transaction.update(lvl1DocRef, "total_earned", currentLvl1Total + 10.0)

                    // Add to Level 1 referrals list
                    val l1ReferralRef = lvl1DocRef.collection("referrals").document(newUserId)
                    transaction.set(l1ReferralRef, mapOf(
                        "name" to newUserName,
                        "amount" to 10.0,
                        "timestamp" to FieldValue.serverTimestamp()
                    ))

                    // Reward Level 2 Grandfather (₹3) if present
                    if (grandfatherId.isNotBlank()) {
                        val lvl2DocRef = usersRef.document(grandfatherId)
                        val lvl2Snapshot = transaction.get(lvl2DocRef)
                        if (lvl2Snapshot.exists()) {
                            val currentLvl2Bal = lvl2Snapshot.getDouble("wallet_balance") ?: 0.0
                            val currentLvl2Total = lvl2Snapshot.getDouble("total_earned") ?: 0.0
                            transaction.update(lvl2DocRef, "wallet_balance", currentLvl2Bal + 3.0)
                            transaction.update(lvl2DocRef, "total_earned", currentLvl2Total + 3.0)
                        }
                    }

                    // Reward Level 3 Great-Grandfather (₹1) if present
                    if (greatGrandfatherId.isNotBlank()) {
                        val lvl3DocRef = usersRef.document(greatGrandfatherId)
                        val lvl3Snapshot = transaction.get(lvl3DocRef)
                        if (lvl3Snapshot.exists()) {
                            val currentLvl3Bal = lvl3Snapshot.getDouble("wallet_balance") ?: 0.0
                            val currentLvl3Total = lvl3Snapshot.getDouble("total_earned") ?: 0.0
                            transaction.update(lvl3DocRef, "wallet_balance", currentLvl3Bal + 1.0)
                            transaction.update(lvl3DocRef, "total_earned", currentLvl3Total + 1.0)
                        }
                    }
                }

                // Step 2: Save the newborn user document with full mapped hierarchy parameters
                val uniqueCodeForNewUser = newUserName.take(3).uppercase().padEnd(3, 'X') + (100..999).random()
                val userDataMap = hashMapOf(
                    "uid" to newUserId,
                    "name" to newUserName,
                    "wallet_balance" to 0.0,
                    "total_earned" to 0.0,
                    "referred_by" to parentId,
                    "referred_by_level2" to grandfatherId,
                    "referred_by_level3" to greatGrandfatherId,
                    "referral_code" to uniqueCodeForNewUser,
                    "current_streak" to 0,
                    "last_check_in_date" to null,
                    "daily_spin_count" to 0,
                    "last_spin_date" to "",
                    "daily_ad_limit" to 10
                )
                transaction.set(newUserRef, userDataMap, com.google.firebase.firestore.SetOptions.merge())
            }.await()
            Log.d("FIRESTORE_TEST", "New user registered with bulletproof 3-level referral: $newUserId")
        } catch (e: Exception) {
            Log.e("FIRESTORE_TEST", "registerUserWithMultiLevelReferral failed", e)
        }
    }

    suspend fun claimDailyCheckIn(): String {
        val uid = userId ?: return "ERROR"
        val userRef = db.collection("users").document(uid)
        
        return try {
            db.runTransaction { transaction ->
                val snapshot = transaction.get(userRef)
                val now = System.currentTimeMillis()
                
                // Explicitly mapping to Firestore keys
                val lastCheckInDate = snapshot.getTimestamp("last_check_in_date")?.toDate()
                var currentStreak = snapshot.getLong("current_streak")?.toInt() ?: 0

                val today = java.util.Calendar.getInstance().apply {
                    set(java.util.Calendar.HOUR_OF_DAY, 0)
                    set(java.util.Calendar.MINUTE, 0)
                    set(java.util.Calendar.SECOND, 0)
                    set(java.util.Calendar.MILLISECOND, 0)
                }.time

                if (lastCheckInDate != null && !lastCheckInDate.before(today)) {
                    throw Exception("ALREADY_CLAIMED")
                }

                // Streak break check: If last check in was more than 48 hours ago
                if (lastCheckInDate != null && (now - lastCheckInDate.time) > (48 * 60 * 60 * 1000)) {
                    currentStreak = 0
                }

                val nextStreak = (currentStreak % 7) + 1
                val reward = when (nextStreak) {
                    1 -> 1.0
                    2 -> 1.5
                    3 -> 2.0
                    4 -> 2.5
                    5 -> 3.0
                    6 -> 4.0
                    7 -> (10..50).random().toDouble()
                    else -> 1.0
                }

                val updates = mutableMapOf<String, Any>(
                    "wallet_balance" to FieldValue.increment(reward),
                    "total_earned" to FieldValue.increment(reward),
                    "last_check_in_date" to FieldValue.serverTimestamp(),
                    "current_streak" to nextStreak
                )

                if (snapshot.exists()) {
                    transaction.update(userRef, updates)
                } else {
                    updates["uid"] = uid
                    updates["name"] = "User"
                    updates["referral_code"] = uid.take(8).uppercase()
                    transaction.set(userRef, updates)
                }
                "SUCCESS|$reward"
            }.await()
        } catch (e: Exception) {
            Log.e("FIRESTORE_TEST", "claimDailyCheckIn error", e)
            if (e.message == "ALREADY_CLAIMED") "ALREADY_CLAIMED" else "ERROR"
        }
    }

    suspend fun processSpinReward(rewardAmount: Double): String {
        val uid = userId ?: return "ERROR"
        val userRef = db.collection("users").document(uid)
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())

        Log.d("FIRESTORE_TEST", "Processing spin reward for doc: $uid")
        return try {
            db.runTransaction { transaction ->
                val snapshot = transaction.get(userRef)
                
                // 1. Check for null/missing fields and initialize them
                val lastSpinDate = if (snapshot.exists()) snapshot.getString("last_spin_date") ?: "" else ""
                var spinCount = if (snapshot.exists()) snapshot.getLong("daily_spin_count")?.toInt() ?: 0 else 0
                val currentBalance = if (snapshot.exists()) snapshot.getDouble("wallet_balance") ?: 0.0 else 0.0
                val currentTotalEarned = if (snapshot.exists()) snapshot.getDouble("total_earned") ?: 0.0 else 0.0

                // 2. Daily limit check
                if (lastSpinDate == today) {
                    if (spinCount >= 5) {
                        throw Exception("LIMIT_REACHED")
                    }
                    spinCount += 1
                } else {
                    spinCount = 1
                }

                // 3. Prepare updates
                val updates = mutableMapOf<String, Any>(
                    "wallet_balance" to currentBalance + rewardAmount,
                    "total_earned" to currentTotalEarned + rewardAmount,
                    "daily_spin_count" to spinCount,
                    "last_spin_date" to today
                )

                // 4. Update or Set document
                if (snapshot.exists()) {
                    transaction.update(userRef, updates as Map<String, Any>)
                } else {
                    updates["uid"] = uid
                    updates["name"] = "Ajay"
                    transaction.set(userRef, updates as Map<String, Any>)
                }

                // Log Transaction
                val creditRef = db.collection("withdrawals").document()
                transaction.set(creditRef, mapOf(
                    "userId" to uid,
                    "amount" to rewardAmount,
                    "type" to "CREDIT",
                    "status" to "SUCCESS",
                    "timestamp" to FieldValue.serverTimestamp(),
                    "description" to "Spin & Win Reward"
                ))
                
                "SUCCESS"
            }.await()
        } catch (e: Exception) {
            Log.e("FIRESTORE_TEST", "processSpinReward failed: ${e.message}", e)
            if (e.message == "LIMIT_REACHED") "LIMIT_REACHED" else "ERROR"
        }
    }

    suspend fun processUnityAdsReward(): String {
        val uid = userId ?: return "ERROR"
        val userRef = db.collection("users").document(uid)
        val adminSettingsRef = db.collection("admin_config").document("settings")
        
        return try {
            db.runTransaction { transaction ->
                val snapshot = transaction.get(userRef)
                if (!snapshot.exists()) throw Exception("USER_NOT_FOUND")

                val adminSnapshot = transaction.get(adminSettingsRef)
                val rewardRate = adminSnapshot.getDouble("rewarded_video_rate") ?: 0.05
                val grossInc = adminSnapshot.getDouble("ad_gross_income_per_view") ?: 0.15 // Example gross

                val currentLimit = snapshot.getLong("daily_ad_limit")?.toInt() ?: 10
                if (currentLimit <= 0) throw Exception("LIMIT_REACHED")

                val walletBalance = snapshot.getDouble("wallet_balance") ?: 0.0
                val totalEarned = snapshot.getDouble("total_earned") ?: 0.0
                val referredBy = snapshot.getString("referred_by") ?: ""
                val referredByL2 = snapshot.getString("referred_by_level2") ?: ""

                // 1. Credit User: Dynamic Rate
                transaction.update(userRef, "wallet_balance", walletBalance + rewardRate)
                transaction.update(userRef, "total_earned", totalEarned + rewardRate)
                transaction.update(userRef, "daily_ad_limit", currentLimit - 1)
                
                // Track Gross Revenue
                transaction.update(adminSettingsRef, "total_gross_revenue", FieldValue.increment(grossInc))

                // 2. Reward Level 1 Parent: ₹0.02 (Adjust if needed)
                if (referredBy.isNotBlank()) {
                    val pRef = db.collection("users").document(referredBy)
                    transaction.update(pRef, "wallet_balance", FieldValue.increment(0.02))
                    transaction.update(pRef, "total_earned", FieldValue.increment(0.02))
                }

                // 3. Reward Level 2 Grandparent: ₹0.01
                if (referredByL2.isNotBlank()) {
                    val gpRef = db.collection("users").document(referredByL2)
                    transaction.update(gpRef, "wallet_balance", FieldValue.increment(0.01))
                    transaction.update(gpRef, "total_earned", FieldValue.increment(0.01))
                }

                // Log Transaction for User
                val creditRef = db.collection("withdrawals").document()
                transaction.set(creditRef, mapOf(
                    "userId" to uid,
                    "amount" to rewardRate,
                    "type" to "CREDIT",
                    "status" to "SUCCESS",
                    "timestamp" to FieldValue.serverTimestamp(),
                    "description" to "Ad View Reward"
                ))

                "SUCCESS"
            }.await()
        } catch (e: Exception) {
            Log.e("UnityAdsReward", "processUnityAdsReward failed", e)
            e.message ?: "ERROR"
        }
    }

    suspend fun processSuperTaskReward(taskType: String, rewardAmount: Double): String {
        val uid = userId ?: return "ERROR"
        val userRef = db.collection("users").document(uid)
        val adminSettingsRef = db.collection("admin_config").document("settings")
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())

        return try {
            db.runTransaction { transaction ->
                val snapshot = transaction.get(userRef)
                if (!snapshot.exists()) throw Exception("USER_NOT_FOUND")

                val adminSnapshot = transaction.get(adminSettingsRef)
                
                val lastTaskDate = snapshot.getString("last_task_date") ?: ""
                val currentScratch = if (lastTaskDate == today) snapshot.getLong("daily_scratch_count")?.toInt() ?: 0 else 0
                val currentCaptcha = if (lastTaskDate == today) snapshot.getLong("daily_captcha_count")?.toInt() ?: 0 else 0
                val currentQuiz = if (lastTaskDate == today) snapshot.getLong("daily_quiz_count")?.toInt() ?: 0 else 0

                val scratchLimit = adminSnapshot.getLong("scratch_daily_limit")?.toInt() ?: 20
                
                val updates = mutableMapOf<String, Any>(
                    "wallet_balance" to FieldValue.increment(rewardAmount),
                    "total_earned" to FieldValue.increment(rewardAmount),
                    "last_task_date" to today
                )

                when (taskType) {
                    "SCRATCH" -> {
                        if (currentScratch >= scratchLimit) throw Exception("LIMIT_REACHED")
                        updates["daily_scratch_count"] = currentScratch + 1
                    }
                    "CAPTCHA" -> {
                        // Assuming captcha has no hard limit or a very high one
                        updates["daily_captcha_count"] = currentCaptcha + 1
                    }
                    "QUIZ" -> {
                        updates["daily_quiz_count"] = currentQuiz + 1
                    }
                }

                transaction.update(userRef, updates)

                // Log Transaction
                val creditRef = db.collection("withdrawals").document()
                transaction.set(creditRef, mapOf(
                    "userId" to uid,
                    "amount" to rewardAmount,
                    "type" to "CREDIT",
                    "status" to "SUCCESS",
                    "timestamp" to FieldValue.serverTimestamp(),
                    "description" to "Super Task: $taskType"
                ))

                "SUCCESS"
            }.await()
        } catch (e: Exception) {
            Log.e("SuperTaskReward", "processSuperTaskReward failed", e)
            e.message ?: "ERROR"
        }
    }

    fun signOut() {
        auth.signOut()
    }

    // Phone Authentication
    fun sendOtp(
        phoneNumber: String,
        activity: Activity,
        callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    ) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber("+91$phoneNumber")
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    suspend fun signInWithCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential).await()
    }

    // Email/Password Authentication
    suspend fun loginWithEmail(email: String, password: String): String {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            "SUCCESS"
        } catch (e: Exception) {
            Log.e("FirebaseAuth", "loginWithEmail failed", e)
            e.localizedMessage ?: "Login failed"
        }
    }

    suspend fun registerWithEmail(email: String, password: String): String {
        return try {
            auth.createUserWithEmailAndPassword(email, password).await()
            "SUCCESS"
        } catch (e: Exception) {
            Log.e("FirebaseAuth", "registerWithEmail failed", e)
            e.localizedMessage ?: "Registration failed"
        }
    }
}
