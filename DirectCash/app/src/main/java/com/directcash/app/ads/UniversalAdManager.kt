package com.directcash.app.ads

import android.app.Activity
import android.util.Log
import com.directcash.app.data.repository.UniversalAdSettings
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.unity3d.ads.IUnityAdsInitializationListener
import com.unity3d.ads.IUnityAdsLoadListener
import com.unity3d.ads.IUnityAdsShowListener
import com.unity3d.ads.UnityAds
import com.unity3d.ads.metadata.MetaData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object UniversalAdManager {
    private const val TAG = "UniversalAdManager"
    private var currentConfig: UniversalAdSettings? = null
    
    private val _isSdkReady = MutableStateFlow(false)
    val isSdkReady: StateFlow<Boolean> = _isSdkReady.asStateFlow()

    private val _isRewardedAdLoaded = MutableStateFlow(false)
    val isRewardedAdLoaded: StateFlow<Boolean> = _isRewardedAdLoaded.asStateFlow()

    private val _isInterstitialAdLoaded = MutableStateFlow(false)
    val isInterstitialAdLoaded: StateFlow<Boolean> = _isInterstitialAdLoaded.asStateFlow()

    // AdMob State
    private var admobRewardedAd: RewardedAd? = null
    private var admobInterstitialAd: InterstitialAd? = null

    fun initialize(activity: Activity, config: UniversalAdSettings) {
        if (currentConfig == config && _isSdkReady.value) return
        
        currentConfig = config
        _isSdkReady.value = false
        
        if (!config.isAdsEnabled) {
            Log.d(TAG, "Ads are globally disabled.")
            _isSdkReady.value = true
            return
        }

        // Fallback Logic: If primaryAppId is empty, use a hardcoded fallback
        val appIdToUse = if (config.primaryAppId.isEmpty()) {
            Log.w(TAG, "Primary App ID is empty, using hardcoded fallback.")
            "800081138" // Updated to requested fallback Unity Game ID
        } else {
            config.primaryAppId.trim() // Explicitly trimming
        }

        Log.d(TAG, "Initializing with Network: ${config.activeNetwork} ID: $appIdToUse")

        try {
            when (config.activeNetwork.uppercase().trim()) {
                "UNITY" -> {
                    val gdprMetaData = MetaData(activity)
                    gdprMetaData.set("gdpr.consent", true)
                    gdprMetaData.commit()

                    UnityAds.initialize(activity.applicationContext, appIdToUse, false, object : IUnityAdsInitializationListener {
                        override fun onInitializationComplete() {
                            Log.d(TAG, "Unity Ads Initialized Successfully")
                            _isSdkReady.value = true
                            // Immediate Pre-load after init
                            loadUnityAd(config.rewardedPlacementId.trim())
                            loadUnityAd(config.interstitialPlacementId.trim())
                        }
                        override fun onInitializationFailed(error: UnityAds.UnityAdsInitializationError?, message: String?) {
                            Log.e(TAG, "Unity Init Failed: $message")
                            _isSdkReady.value = true
                        }
                    })
                    
                    // Safety Timeout: If SDK doesn't respond in 5 seconds, unlock UI anyway
                    activity.runOnUiThread {
                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                            if (!_isSdkReady.value) {
                                Log.w(TAG, "Initialization timeout reached, forcing ready state.")
                                _isSdkReady.value = true
                            }
                        }, 5000)
                    }
                }
                "ADMOB" -> initAdMob(activity, config)
                "APPLOVIN" -> {
                    initAppLovin(activity, config)
                    _isSdkReady.value = true
                }
                else -> {
                    Log.w(TAG, "Unknown Network: ${config.activeNetwork}")
                    _isSdkReady.value = true
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Initialization error wrapping", e)
            _isSdkReady.value = true 
        }
    }

    fun loadUnityAd(placementId: String) {
        if (placementId.isEmpty() || !UnityAds.isInitialized) return
        
        Log.d(TAG, "Requesting Unity Ad Load: $placementId")
        UnityAds.load(placementId, object : IUnityAdsLoadListener {
            override fun onUnityAdsAdLoaded(placementId: String?) {
                Log.d(TAG, "Unity Ad Loaded: $placementId")
                if (placementId == currentConfig?.rewardedPlacementId?.trim()) _isRewardedAdLoaded.value = true
                if (placementId == currentConfig?.interstitialPlacementId?.trim()) _isInterstitialAdLoaded.value = true
            }
            override fun onUnityAdsFailedToLoad(placementId: String?, error: UnityAds.UnityAdsLoadError?, message: String?) {
                Log.e(TAG, "Unity Load Failed ($placementId): $message")
                if (placementId == currentConfig?.rewardedPlacementId?.trim()) _isRewardedAdLoaded.value = false
                if (placementId == currentConfig?.interstitialPlacementId?.trim()) _isInterstitialAdLoaded.value = false
                
                // Auto-retry load on failure after a short delay
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    loadUnityAd(placementId ?: "")
                }, 10000)
            }
        })
    }

    private fun initAdMob(activity: Activity, config: UniversalAdSettings) {
        MobileAds.initialize(activity) {
            _isSdkReady.value = true
            loadAdMobRewarded(activity, config.rewardedPlacementId)
            loadAdMobInterstitial(activity, config.interstitialPlacementId)
        }
    }

    private fun loadAdMobRewarded(activity: Activity, placementId: String) {
        if (placementId.isEmpty()) return
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(activity, placementId, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d(TAG, "AdMob Rewarded Load Failed: ${adError.message}")
                admobRewardedAd = null
            }
            override fun onAdLoaded(ad: RewardedAd) {
                Log.d(TAG, "AdMob Rewarded Ad Loaded")
                admobRewardedAd = ad
            }
        })
    }

    private fun loadAdMobInterstitial(activity: Activity, placementId: String) {
        if (placementId.isEmpty()) return
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(activity, placementId, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d(TAG, "AdMob Interstitial Load Failed: ${adError.message}")
                admobInterstitialAd = null
            }
            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                Log.d(TAG, "AdMob Interstitial Ad Loaded")
                admobInterstitialAd = interstitialAd
            }
        })
    }

    private fun initAppLovin(activity: Activity, config: UniversalAdSettings) {
        // Placeholder for AppLovin
        Log.d(TAG, "AppLovin Init (Placeholder) with ID: ${config.primaryAppId}")
    }

    fun showRewardedAd(activity: Activity, onRewardEarned: () -> Unit, onFailure: (String) -> Unit) {
        val config = currentConfig ?: run {
            onFailure("Ad Config not loaded")
            return
        }
        if (!config.isAdsEnabled) {
            onFailure("Ads are disabled")
            return
        }

        when (config.activeNetwork.uppercase()) {
            "UNITY" -> {
                if (!UnityAds.isInitialized) {
                    onFailure("Unity Ads not initialized")
                    return
                }
                
                // Explicitly check if ad is loaded before showing
                UnityAds.show(activity, config.rewardedPlacementId, object : IUnityAdsShowListener {
                    override fun onUnityAdsShowStart(p0: String?) {}
                    override fun onUnityAdsShowClick(p0: String?) {}
                    override fun onUnityAdsShowComplete(p0: String?, p1: UnityAds.UnityAdsShowCompletionState?) {
                        if (p1 == UnityAds.UnityAdsShowCompletionState.COMPLETED) {
                            onRewardEarned()
                        }
                        _isRewardedAdLoaded.value = false // Reset state
                        loadUnityAd(config.rewardedPlacementId.trim()) // Auto-reload next ad
                    }
                    override fun onUnityAdsShowFailure(p0: String?, p1: UnityAds.UnityAdsShowError?, p2: String?) {
                        _isRewardedAdLoaded.value = false // Reset state
                        loadUnityAd(config.rewardedPlacementId.trim()) // Auto-reload next ad
                        if (p1 == UnityAds.UnityAdsShowError.NOT_INITIALIZED || p1 == UnityAds.UnityAdsShowError.NOT_READY) {
                            onFailure("Ad is buffering, please try again in 3 seconds...")
                        } else {
                            onFailure(p2 ?: "Unity Show Error")
                        }
                    }
                })
            }
            "ADMOB" -> {
                admobRewardedAd?.let { ad ->
                    ad.show(activity) {
                        onRewardEarned()
                    }
                    admobRewardedAd = null
                    loadAdMobRewarded(activity, config.rewardedPlacementId)
                } ?: run {
                    onFailure("AdMob Rewarded not ready")
                    loadAdMobRewarded(activity, config.rewardedPlacementId)
                }
            }
            else -> onFailure("Rewarded not supported for ${config.activeNetwork}")
        }
    }

    fun showInterstitialAd(activity: Activity, onAdClosed: () -> Unit, onFailure: (String) -> Unit) {
        val config = currentConfig ?: run {
            onFailure("Ad Config not loaded")
            return
        }
        if (!config.isAdsEnabled) {
            onAdClosed()
            return
        }

        when (config.activeNetwork.uppercase()) {
            "UNITY" -> {
                if (!UnityAds.isInitialized) {
                    onAdClosed()
                    return
                }
                UnityAds.show(activity, config.interstitialPlacementId, object : IUnityAdsShowListener {
                    override fun onUnityAdsShowStart(p0: String?) {}
                    override fun onUnityAdsShowClick(p0: String?) {}
                    override fun onUnityAdsShowComplete(p0: String?, p1: UnityAds.UnityAdsShowCompletionState?) {
                        onAdClosed()
                        _isInterstitialAdLoaded.value = false
                        loadUnityAd(config.interstitialPlacementId.trim())
                    }
                    override fun onUnityAdsShowFailure(p0: String?, p1: UnityAds.UnityAdsShowError?, p2: String?) {
                        _isInterstitialAdLoaded.value = false
                        loadUnityAd(config.interstitialPlacementId.trim())
                        onFailure(p2 ?: "Unity Show Error")
                        onAdClosed()
                    }
                })
            }
            "ADMOB" -> {
                admobInterstitialAd?.let { ad ->
                    ad.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            onAdClosed()
                            admobInterstitialAd = null
                            loadAdMobInterstitial(activity, config.interstitialPlacementId)
                        }
                        override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                            onFailure(error.message)
                            onAdClosed()
                        }
                    }
                    ad.show(activity)
                } ?: run {
                    onFailure("AdMob Interstitial not ready")
                    onAdClosed()
                    loadAdMobInterstitial(activity, config.interstitialPlacementId)
                }
            }
            else -> {
                onFailure("Interstitial not supported for ${config.activeNetwork}")
                onAdClosed()
            }
        }
    }
}
