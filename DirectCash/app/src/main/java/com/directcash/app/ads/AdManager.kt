package com.directcash.app.ads

import android.app.Activity
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

object AdManager {
    private const val TAG = "AdManager"
    // REAL AD UNIT ID PROVIDED BY USER
    private const val REWARDED_AD_UNIT_ID = "ca-app-pub-8399518364077302/2917050751"

    private var rewardedAd: RewardedAd? = null
    private var isAdLoading = false

    fun loadRewardedAd(activity: Activity) {
        if (rewardedAd != null || isAdLoading) return
        
        isAdLoading = true
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(activity, REWARDED_AD_UNIT_ID, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d(TAG, "Ad failed to load: ${adError.message}")
                rewardedAd = null
                isAdLoading = false
            }

            override fun onAdLoaded(ad: RewardedAd) {
                Log.d(TAG, "Ad was loaded.")
                rewardedAd = ad
                isAdLoading = false
            }
        })
    }

    fun showRewardedAd(activity: Activity, onRewardEarned: () -> Unit, onAdClosed: () -> Unit) {
        if (rewardedAd != null) {
            rewardedAd?.show(activity) { rewardItem ->
                Log.d(TAG, "User earned the reward.")
                onRewardEarned()
            }
            rewardedAd = null // Reset after showing
            loadRewardedAd(activity) // Preload next
        } else {
            Log.d(TAG, "The rewarded ad wasn't ready yet.")
            loadRewardedAd(activity)
            onAdClosed() // Or handle as ad not ready
        }
    }
}
