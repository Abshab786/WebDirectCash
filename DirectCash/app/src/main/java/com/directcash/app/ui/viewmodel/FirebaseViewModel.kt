package com.directcash.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.directcash.app.data.model.Task
import com.directcash.app.data.model.Transaction
import com.directcash.app.data.remote.CpaGripApiService
import com.directcash.app.data.remote.CpaGripConfig
import com.directcash.app.data.repository.FirebaseRepository
import com.directcash.app.data.repository.Referral
import com.directcash.app.data.repository.OfferwallSettings
import com.directcash.app.data.repository.AdminSettings
import com.directcash.app.ads.UniversalAdManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import android.app.Activity
import android.util.Log

class FirebaseViewModel(
    private val repository: FirebaseRepository = FirebaseRepository()
) : ViewModel() {

    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _balance = MutableStateFlow(0.0)
    val balance: StateFlow<Double> = _balance.asStateFlow()

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private val _totalEarnings = MutableStateFlow(0.0)
    val totalEarnings: StateFlow<Double> = _totalEarnings.asStateFlow()

    private val _dailySpinCount = MutableStateFlow(0)
    val dailySpinCount: StateFlow<Int> = _dailySpinCount.asStateFlow()

    private val _lastSpinDate = MutableStateFlow("")
    val lastSpinDate: StateFlow<String> = _lastSpinDate.asStateFlow()

    private val _referralCode = MutableStateFlow("")
    val referralCode: StateFlow<String> = _referralCode.asStateFlow()

    private val _currentStreak = MutableStateFlow(0)
    val currentStreak: StateFlow<Int> = _currentStreak.asStateFlow()

    private val _dailyAdLimit = MutableStateFlow(10)
    val dailyAdLimit: StateFlow<Int> = _dailyAdLimit.asStateFlow()

    private val _dailyScratchCount = MutableStateFlow(0)
    val dailyScratchCount: StateFlow<Int> = _dailyScratchCount.asStateFlow()

    private val _dailyCaptchaCount = MutableStateFlow(0)
    val dailyCaptchaCount: StateFlow<Int> = _dailyCaptchaCount.asStateFlow()

    private val _dailyQuizCount = MutableStateFlow(0)
    val dailyQuizCount: StateFlow<Int> = _dailyQuizCount.asStateFlow()

    private val _lastTaskDate = MutableStateFlow("")
    val lastTaskDate: StateFlow<String> = _lastTaskDate.asStateFlow()

    private val _lastCheckInTimestamp = MutableStateFlow(0L)
    val lastCheckInTimestamp: StateFlow<Long> = _lastCheckInTimestamp.asStateFlow()

    val userId: String? get() = repository.userId

    private val _referrals = MutableStateFlow<List<Referral>>(emptyList())
    val referrals: StateFlow<List<Referral>> = _referrals.asStateFlow()

    private val _adminSettings = MutableStateFlow(AdminSettings())
    val adminSettings: StateFlow<AdminSettings> = _adminSettings.asStateFlow()

    private val _pendingWithdrawals = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val pendingWithdrawals: StateFlow<List<Map<String, Any>>> = _pendingWithdrawals.asStateFlow()

    private val _isMaintenance = MutableStateFlow(false)
    val isMaintenance: StateFlow<Boolean> = _isMaintenance.asStateFlow()

    val isAdsSdkReady: StateFlow<Boolean> = UniversalAdManager.isSdkReady
    val isRewardedAdLoaded: StateFlow<Boolean> = UniversalAdManager.isRewardedAdLoaded
    val isInterstitialAdLoaded: StateFlow<Boolean> = UniversalAdManager.isInterstitialAdLoaded

    private val _offerwallSettings = MutableStateFlow(OfferwallSettings())
    val offerwallSettings: StateFlow<OfferwallSettings> = _offerwallSettings.asStateFlow()

    private val cpaGripApi = CpaGripApiService.create()

    private val _loadingTasks = MutableStateFlow(false)
    val loadingTasks: StateFlow<Boolean> = _loadingTasks.asStateFlow()

    private val _taskError = MutableStateFlow<String?>(null)
    val taskError: StateFlow<String?> = _taskError.asStateFlow()

    init {
        observeUserStats()
        observeManualTasks()
        fetchCpaGripTasks()
        observeTransactions()
        observeReferrals()
        observeMaintenanceStatus()
        observeOfferwallSettings()
        observeAdminSettings()
        observePendingWithdrawals()
    }

    private fun observeManualTasks() {
        viewModelScope.launch {
            repository.getTodayTasks().collect { taskList ->
                _tasks.value = taskList
            }
        }
    }

    private fun observeAdminSettings() {
        viewModelScope.launch {
            repository.observeAdminSettings().collect { settings ->
                _adminSettings.value = settings
                _isMaintenance.value = settings.maintenanceMode
            }
        }
    }

    private fun observePendingWithdrawals() {
        viewModelScope.launch {
            repository.getPendingWithdrawals().collect { list ->
                _pendingWithdrawals.value = list
            }
        }
    }

    fun updateAdminSettings(settings: AdminSettings) {
        viewModelScope.launch { repository.updateAdminSettings(settings) }
    }

    fun manageWithdrawal(requestId: String, userId: String, status: String, amount: Double) {
        viewModelScope.launch { repository.manageWithdrawal(requestId, userId, status, amount) }
    }

    fun adjustUserWallet(targetUid: String, amount: Double, isCredit: Boolean) {
        viewModelScope.launch { repository.adjustUserWallet(targetUid, amount, isCredit) }
    }

    fun suspendUserTemp(targetUid: String) {
        viewModelScope.launch { repository.suspendUserTemp(targetUid) }
    }

    fun banUserPermanent(targetUid: String) {
        viewModelScope.launch { repository.banUserPermanent(targetUid) }
    }

    private val _searchResults = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val searchResults: StateFlow<List<Map<String, Any>>> = _searchResults.asStateFlow()

    fun searchUser(query: String) {
        viewModelScope.launch {
            repository.searchUser(query).collect { results ->
                _searchResults.value = results
            }
        }
    }

    private fun observeOfferwallSettings() {
        viewModelScope.launch {
            repository.observeOfferwallSettings().collect { settings ->
                _offerwallSettings.value = settings
                // Re-fetch tasks when settings change (e.g. enabled/disabled or key change)
                fetchCpaGripTasks()
            }
        }
    }

    private fun observeMaintenanceStatus() {
        viewModelScope.launch {
            repository.observeAppMaintenance().collect { isMaint ->
                _isMaintenance.value = isMaint
            }
        }
    }

    private fun observeUserStats() {
        viewModelScope.launch {
            repository.observeUserStats().collect { stats ->
                _userName.value = stats.name
                _balance.value = stats.walletBalance
                _totalEarnings.value = stats.totalEarned
                _dailySpinCount.value = stats.dailySpinCount
                _lastSpinDate.value = stats.lastSpinDate
                _referralCode.value = stats.referralCode
                _currentStreak.value = stats.currentStreak
                _dailyAdLimit.value = stats.dailyAdLimit
                _dailyScratchCount.value = stats.dailyScratchCount
                _dailyCaptchaCount.value = stats.dailyCaptchaCount
                _dailyQuizCount.value = stats.dailyQuizCount
                _lastTaskDate.value = stats.lastTaskDate
                _lastCheckInTimestamp.value = stats.lastCheckInTimestamp
            }
        }
    }

    val referralEarnings: StateFlow<Double> = MutableStateFlow(0.0).apply {
        viewModelScope.launch {
            referrals.collect { list ->
                value = list.sumOf { it.amount }
            }
        }
    }.asStateFlow()

    val todayEarnings: StateFlow<Double> = transactions
        .map { list ->
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val todayStr = sdf.format(java.util.Date())
            list.filter { 
                it.type == com.directcash.app.data.model.TransactionType.CREDIT && 
                sdf.format(java.util.Date(it.timestamp)) == todayStr
            }.sumOf { it.amount }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    private val _unityAdStatus = MutableStateFlow<String?>(null)
    val unityAdStatus: StateFlow<String?> = _unityAdStatus.asStateFlow()

    fun claimUnityAdsReward() {
        viewModelScope.launch {
            val result = repository.processUnityAdsReward()
            _unityAdStatus.value = result
        }
    }

    private val _superTaskStatus = MutableStateFlow<String?>(null)
    val superTaskStatus: StateFlow<String?> = _superTaskStatus.asStateFlow()

    fun claimSuperTaskReward(taskType: String, rewardAmount: Double) {
        viewModelScope.launch {
            val result = repository.processSuperTaskReward(taskType, rewardAmount)
            _superTaskStatus.value = result
        }
    }

    fun resetSuperTaskStatus() {
        _superTaskStatus.value = null
    }

    fun resetUnityAdStatus() {
        _unityAdStatus.value = null
    }

    private fun observeReferrals() {
        viewModelScope.launch {
            repository.observeReferrals().collect { list ->
                _referrals.value = list
            }
        }
    }

    fun fetchCpaGripTasks() {
        // CPAGrip JSON parsing removed as per request to use direct WebView preview
        _tasks.value = emptyList()
    }

    private fun observeTransactions() {
        viewModelScope.launch {
            repository.getTransactions().collect { newTransactions ->
                _transactions.value = newTransactions
            }
        }
    }

    fun completeTask(taskId: Long, reward: Double, title: String) {
        viewModelScope.launch {
            repository.completeTask(taskId, reward, title)
        }
    }

    fun withdrawCash(upiId: String, amount: Double) {
        viewModelScope.launch {
            repository.withdrawCash(upiId, amount)
        }
    }

    private val _dailyBonusStatus = MutableStateFlow<String?>(null)
    val dailyBonusStatus: StateFlow<String?> = _dailyBonusStatus.asStateFlow()

    fun claimDailyCheckIn() {
        viewModelScope.launch {
            val result = repository.claimDailyCheckIn()
            _dailyBonusStatus.value = result
        }
    }

    fun resetDailyBonusStatus() {
        _dailyBonusStatus.value = null
    }

    private val _spinStatus = MutableStateFlow<String?>(null)
    val spinStatus: StateFlow<String?> = _spinStatus.asStateFlow()

    fun claimSpinReward(rewardAmount: Double) {
        viewModelScope.launch {
            val result = repository.processSpinReward(rewardAmount)
            _spinStatus.value = result
        }
    }

    fun resetSpinStatus() {
        _spinStatus.value = null
    }

    fun forceLoadAd(isRewarded: Boolean) {
        val config = adminSettings.value.ads
        if (isRewarded) {
            UniversalAdManager.loadUnityAd(config.rewardedPlacementId.trim())
        } else {
            UniversalAdManager.loadUnityAd(config.interstitialPlacementId.trim())
        }
    }

    fun isValidUpi(upiId: String): Boolean {
        return upiId.contains("@") && upiId.length > 3
    }

    fun logout() {
        repository.signOut()
        _userName.value = "Guest"
        _balance.value = 0.0
        _tasks.value = emptyList()
        _transactions.value = emptyList()
    }

    // Auth methods
    fun sendOtp(phoneNumber: String, activity: Activity, callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks) {
        repository.sendOtp(phoneNumber, activity, callbacks)
    }

    fun loginWithEmail(email: String, password: String, referralCode: String = "", onResult: (String) -> Unit) {
        viewModelScope.launch {
            val result = repository.loginWithEmail(email, password)
            if (result == "SUCCESS") {
                val uid = repository.userId ?: ""
                val name = email.substringBefore("@")
                repository.registerUserWithMultiLevelReferral(uid, name, referralCode)
                observeUserStats()
                fetchCpaGripTasks()
                observeTransactions()
                observeReferrals()
            }
            onResult(result)
        }
    }

    fun registerWithEmail(email: String, password: String, referralCode: String = "", onResult: (String) -> Unit) {
        viewModelScope.launch {
            val result = repository.registerWithEmail(email, password)
            if (result == "SUCCESS") {
                val uid = repository.userId ?: ""
                val name = email.substringBefore("@")
                repository.registerUserWithMultiLevelReferral(uid, name, referralCode)
                observeUserStats()
                fetchCpaGripTasks()
                observeTransactions()
                observeReferrals()
            }
            onResult(result)
        }
    }

    fun signInWithCredential(credential: PhoneAuthCredential, referralCode: String = "", onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                repository.signInWithCredential(credential)
                val uid = repository.userId ?: ""
                repository.registerUserWithMultiLevelReferral(uid, "User", referralCode)
                observeUserStats()
                fetchCpaGripTasks()
                observeTransactions()
                observeReferrals()
                onResult(true)
            } catch (e: Exception) {
                Log.e("FirebaseViewModel", "signInWithCredential failed", e)
                onResult(false)
            }
        }
    }
}
