package com.directcash.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.directcash.app.ui.viewmodel.FirebaseViewModel
import com.directcash.app.ads.UniversalAdManager
import com.unity3d.ads.UnityAds

private val DeepSlate = Color(0xFF121212)
private val SurfaceSlate = Color(0xFF1E1E1E)
private val NeonGreen = Color(0xFF00E676)
private val GoldAmber = Color(0xFFFFD700)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuperTaskHubScreen(
    viewModel: FirebaseViewModel,
    onBack: () -> Unit,
    onNavigateToOfferwall: () -> Unit,
    onNavigateToScratch: () -> Unit,
    onNavigateToCaptcha: () -> Unit,
    onNavigateToQuiz: () -> Unit
) {
    val context = LocalContext.current
    val adminSettings by viewModel.adminSettings.collectAsState()
    val offerwallSettings by viewModel.offerwallSettings.collectAsState()
    val isAdsSdkReady by viewModel.isAdsSdkReady.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SUPER TASK HUB", fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepSlate,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = DeepSlate
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Module A: Watch Video Hub
            item {
                TaskModuleCard(
                    title = "PREMIUM VIDEO HUB",
                    subtitle = if (isAdsSdkReady) "Earn ₹${adminSettings.rewardedVideoRate} per ad" else "Initializing Ad Network...",
                    icon = Icons.Default.PlayCircle,
                    accentColor = if (isAdsSdkReady) NeonGreen else Color.Gray
                ) {
                    if (isAdsSdkReady) {
                        UniversalAdManager.showRewardedAd(
                            activity = context as android.app.Activity,
                            onRewardEarned = {
                                viewModel.claimUnityAdsReward()
                            },
                            onFailure = { error ->
                                Toast.makeText(context, "Ad Failed: $error", Toast.LENGTH_SHORT).show()
                            }
                        )
                    } else {
                        Toast.makeText(context, "Please wait, ad network is initializing...", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            // Module B: Scratch Card Matrix
            item {
                TaskModuleCard(
                    title = "GOLDEN SCRATCH MATRIX",
                    subtitle = "Win up to ₹${adminSettings.scratchMaxReward}",
                    icon = Icons.Default.AutoAwesome,
                    accentColor = GoldAmber
                ) {
                    onNavigateToScratch()
                }
            }

            // Module C: Offerwall & Installs
            item {
                TaskModuleCard(
                    title = "OFFERWALL & INSTALLS",
                    subtitle = "Programmatic Tasks & App Installs",
                    icon = Icons.Default.Download,
                    accentColor = Color(0xFF2196F3)
                ) {
                    onNavigateToOfferwall()
                }
            }

            // Module D: Captcha & Quiz Engine
            item {
                TaskModuleCard(
                    title = "CAPTCHA ENGINE",
                    subtitle = "Type & Earn Instantly",
                    icon = Icons.Default.Abc,
                    accentColor = Color(0xFF9C27B0)
                ) {
                    onNavigateToCaptcha()
                }
            }

            item {
                TaskModuleCard(
                    title = "MATH QUIZ HUB",
                    subtitle = "Solve & Earn Rewards",
                    icon = Icons.Default.Calculate,
                    accentColor = Color(0xFF2196F3)
                ) {
                    onNavigateToQuiz()
                }
            }
        }
    }
}

@Composable
fun TaskModuleCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(20.dp)),
        color = SurfaceSlate,
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(accentColor.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = accentColor, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                Text(subtitle, color = Color.Gray, fontSize = 11.sp)
            }
            Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
        }
    }
}
