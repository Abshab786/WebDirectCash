package com.directcash.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.directcash.app.ui.viewmodel.FirebaseViewModel
import com.directcash.app.ads.UniversalAdManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptchaScreen(
    viewModel: FirebaseViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val adminSettings by viewModel.adminSettings.collectAsState()
    val isAdsReady by viewModel.isAdsSdkReady.collectAsState()
    val isRewardedAdLoaded by viewModel.isRewardedAdLoaded.collectAsState()
    val superTaskStatus by viewModel.superTaskStatus.collectAsState()

    var captchaText by remember { mutableStateOf(generateCaptcha()) }
    var userInput by remember { mutableStateOf("") }

    LaunchedEffect(superTaskStatus) {
        if (superTaskStatus == "SUCCESS") {
            Toast.makeText(context, "Captcha Reward Claimed!", Toast.LENGTH_SHORT).show()
            viewModel.resetSuperTaskStatus()
            captchaText = generateCaptcha()
            userInput = ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SOLVE CAPTCHA") },
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
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEEEEEE)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = captchaText,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 8.sp,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    IconButton(onClick = { captchaText = generateCaptcha() }) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = userInput,
                onValueChange = { userInput = it },
                label = { Text("Enter Captcha") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (userInput == captchaText) {
                        if (isAdsReady) {
                            if (!isRewardedAdLoaded) {
                                Toast.makeText(context, "Ad is buffering, please try again in 3 seconds...", Toast.LENGTH_SHORT).show()
                                viewModel.forceLoadAd(true)
                                return@Button
                            }

                            UniversalAdManager.showRewardedAd(
                                activity = context as android.app.Activity,
                                onRewardEarned = {
                                    viewModel.claimSuperTaskReward("CAPTCHA", adminSettings.captchaReward)
                                },
                                onFailure = { error ->
                                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                                }
                            )
                        } else {
                            Toast.makeText(context, "Ad network initializing...", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "Invalid Captcha!", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0))
            ) {
                Text("Verify & Earn", fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Earn ₹${adminSettings.captchaReward} per captcha", color = Color.Gray, fontSize = 12.sp)
        }
    }
}

private fun generateCaptcha(): String {
    val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    return (1..6).map { chars.random() }.joinToString("")
}
