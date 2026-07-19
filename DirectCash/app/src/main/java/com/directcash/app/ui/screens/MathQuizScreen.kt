package com.directcash.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
fun MathQuizScreen(
    viewModel: FirebaseViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val adminSettings by viewModel.adminSettings.collectAsState()
    val isAdsReady by viewModel.isAdsSdkReady.collectAsState()
    val isRewardedAdLoaded by viewModel.isRewardedAdLoaded.collectAsState()
    val superTaskStatus by viewModel.superTaskStatus.collectAsState()

    var num1 by remember { mutableIntStateOf((1..50).random()) }
    var num2 by remember { mutableIntStateOf((1..50).random()) }
    var userInput by remember { mutableStateOf("") }

    LaunchedEffect(superTaskStatus) {
        if (superTaskStatus == "SUCCESS") {
            Toast.makeText(context, "Quiz Reward Claimed!", Toast.LENGTH_SHORT).show()
            viewModel.resetSuperTaskStatus()
            num1 = (1..50).random()
            num2 = (1..50).random()
            userInput = ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MATH QUIZ") },
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
                text = "$num1 + $num2 = ?",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(48.dp))

            OutlinedTextField(
                value = userInput,
                onValueChange = { userInput = it },
                label = { Text("Your Answer") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    val answer = userInput.toIntOrNull()
                    if (answer == (num1 + num2)) {
                        if (isAdsReady) {
                            if (!isRewardedAdLoaded) {
                                Toast.makeText(context, "Ad is buffering, please try again in 3 seconds...", Toast.LENGTH_SHORT).show()
                                viewModel.forceLoadAd(true)
                                return@Button
                            }

                            UniversalAdManager.showRewardedAd(
                                activity = context as android.app.Activity,
                                onRewardEarned = {
                                    viewModel.claimSuperTaskReward("QUIZ", adminSettings.mathQuizReward)
                                },
                                onFailure = { error ->
                                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                                }
                            )
                        } else {
                            Toast.makeText(context, "Ad network initializing...", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "Incorrect Answer!", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
            ) {
                Text("Submit Answer", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Earn ₹${adminSettings.mathQuizReward} per quiz", color = Color.Gray, fontSize = 12.sp)
        }
    }
}
