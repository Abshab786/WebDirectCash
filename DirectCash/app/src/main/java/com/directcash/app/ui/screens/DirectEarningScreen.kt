package com.directcash.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.unity3d.ads.IUnityAdsLoadListener
import com.unity3d.ads.IUnityAdsShowListener
import com.unity3d.ads.UnityAds
import com.directcash.app.ui.theme.VibrantGreen
import com.directcash.app.ui.viewmodel.FirebaseViewModel

@Composable
fun DirectEarningScreen(
    firebaseViewModel: FirebaseViewModel
) {
    val context = LocalContext.current
    val adStatus by firebaseViewModel.unityAdStatus.collectAsState()
    val dailyLimit by firebaseViewModel.dailyAdLimit.collectAsState()
    var isAdLoading by remember { mutableStateOf(false) }

    LaunchedEffect(adStatus) {
        adStatus?.let {
            if (it == "SUCCESS") {
                Toast.makeText(context, "₹0.05 Credited Successfully!", Toast.LENGTH_SHORT).show()
            } else if (it == "LIMIT_REACHED") {
                Toast.makeText(context, "Daily ad limit reached!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Reward Error: $it", Toast.LENGTH_SHORT).show()
            }
            firebaseViewModel.resetUnityAdStatus()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Earning Hub",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        
        Text(
            text = "Watch ads and complete quick offers",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color(0xFFE8F5E9), RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = VibrantGreen,
                        modifier = Modifier.size(48.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Watch Video Ad",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "Earn ₹0.05 per video",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                Text(
                    text = "Daily Limit: $dailyLimit",
                    style = MaterialTheme.typography.labelSmall,
                    color = VibrantGreen,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = {
                        if (dailyLimit <= 0) {
                            Toast.makeText(context, "No more ads for today!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        
                        isAdLoading = true
                        
                        UnityAds.load("Rewarded_Android", object : IUnityAdsLoadListener {
                            override fun onUnityAdsAdLoaded(placementId: String?) {
                                isAdLoading = false
                                UnityAds.show(context as android.app.Activity, "Rewarded_Android", object : IUnityAdsShowListener {
                                    override fun onUnityAdsShowStart(placementId: String?) {}
                                    override fun onUnityAdsShowClick(placementId: String?) {}
                                    override fun onUnityAdsShowComplete(
                                        placementId: String?,
                                        state: UnityAds.UnityAdsShowCompletionState?
                                    ) {
                                        if (state == UnityAds.UnityAdsShowCompletionState.COMPLETED) {
                                            firebaseViewModel.claimUnityAdsReward()
                                        }
                                    }
                                    override fun onUnityAdsShowFailure(
                                        placementId: String?,
                                        error: UnityAds.UnityAdsShowError?,
                                        message: String?
                                    ) {
                                        isAdLoading = false
                                        Toast.makeText(context, "Ad failed to show: $message", Toast.LENGTH_SHORT).show()
                                    }
                                })
                            }

                            override fun onUnityAdsFailedToLoad(
                                placementId: String?,
                                error: UnityAds.UnityAdsLoadError?,
                                message: String?
                            ) {
                                isAdLoading = false
                                Toast.makeText(context, "Ad not ready: $message", Toast.LENGTH_SHORT).show()
                            }
                        })
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = !isAdLoading,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VibrantGreen)
                ) {
                    if (isAdLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Watch Now", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Additional earning methods can be added here
    }
}
