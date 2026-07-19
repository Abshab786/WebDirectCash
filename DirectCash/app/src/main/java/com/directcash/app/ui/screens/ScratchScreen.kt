package com.directcash.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.directcash.app.ui.viewmodel.FirebaseViewModel
import com.directcash.app.ads.UniversalAdManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScratchScreen(
    viewModel: FirebaseViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val adminSettings by viewModel.adminSettings.collectAsState()
    val dailyScratchCount by viewModel.dailyScratchCount.collectAsState()
    val isAdsReady by viewModel.isAdsSdkReady.collectAsState()
    val isRewardedAdLoaded by viewModel.isRewardedAdLoaded.collectAsState()
    val superTaskStatus by viewModel.superTaskStatus.collectAsState()

    val remaining = (adminSettings.scratchDailyLimit - dailyScratchCount).coerceAtLeast(0)

    LaunchedEffect(superTaskStatus) {
        if (superTaskStatus == "SUCCESS") {
            Toast.makeText(context, "Reward Claimed Successfully!", Toast.LENGTH_SHORT).show()
            viewModel.resetSuperTaskStatus()
        } else if (superTaskStatus == "LIMIT_REACHED") {
            Toast.makeText(context, "Daily Limit Reached!", Toast.LENGTH_SHORT).show()
            viewModel.resetSuperTaskStatus()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("GOLDEN SCRATCH") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Remaining Today: $remaining",
                style = MaterialTheme.typography.titleMedium,
                color = Color.Gray
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            // Placeholder for Scratch Card UI
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .background(Color(0xFFFFD700), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "SCRATCH HERE",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                    Text(
                        text = "Win up to ₹${adminSettings.scratchMaxReward}",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = {
                    if (remaining <= 0) {
                        Toast.makeText(context, "Limit reached for today!", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    if (isAdsReady) {
                        if (!isRewardedAdLoaded) {
                            Toast.makeText(context, "Ad is buffering, please try again in 3 seconds...", Toast.LENGTH_SHORT).show()
                            viewModel.forceLoadAd(true)
                            return@Button
                        }
                        
                        UniversalAdManager.showRewardedAd(
                            activity = context as android.app.Activity,
                            onRewardEarned = {
                                val reward = (0.01..adminSettings.scratchMaxReward).random()
                                viewModel.claimSuperTaskReward("SCRATCH", String.format("%.2f", reward).toDouble())
                            },
                            onFailure = { error ->
                                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                            }
                        )
                    } else {
                        Toast.makeText(context, "Ad network initializing...", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853))
            ) {
                Text("Scratch Card", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }
    }
}

private fun ClosedRange<Double>.random() = 
    kotlin.random.Random.nextDouble(start, endInclusive)
