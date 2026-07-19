package com.directcash.app.ui.screens

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.directcash.app.ui.theme.PrimaryEmerald
import com.directcash.app.ui.theme.VibrantGreen
import com.directcash.app.ui.viewmodel.FirebaseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyCheckInScreen(
    viewModel: FirebaseViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val currentStreak by viewModel.currentStreak.collectAsState()
    val dailyBonusStatus by viewModel.dailyBonusStatus.collectAsState()
    val lastCheckInTimestamp by viewModel.lastCheckInTimestamp.collectAsState()

    val today = java.util.Calendar.getInstance().apply {
        set(java.util.Calendar.HOUR_OF_DAY, 0)
        set(java.util.Calendar.MINUTE, 0)
        set(java.util.Calendar.SECOND, 0)
        set(java.util.Calendar.MILLISECOND, 0)
    }.timeInMillis

    val isAlreadyClaimed = lastCheckInTimestamp >= today

    LaunchedEffect(dailyBonusStatus) {
        dailyBonusStatus?.let { status ->
            if (status.startsWith("SUCCESS")) {
                val reward = status.split("|").getOrNull(1) ?: ""
                Toast.makeText(context, "₹$reward Claimed Successfully!", Toast.LENGTH_SHORT).show()
            } else if (status == "ALREADY_CLAIMED") {
                Toast.makeText(context, "Already claimed today! Come back tomorrow.", Toast.LENGTH_SHORT).show()
            } else if (status == "ERROR") {
                Toast.makeText(context, "Error claiming bonus. Please try again.", Toast.LENGTH_SHORT).show()
            }
            viewModel.resetDailyBonusStatus()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daily Check-In", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF9F9F9))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Keep your streak alive!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Check-in every day to earn more. Complete 7 days for a Mega Reward!",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(48.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(6) { index ->
                    val day = index + 1
                    CheckInBox(
                        day = day,
                        reward = when (day) {
                            1 -> "₹1"
                            2 -> "₹1.5"
                            3 -> "₹2"
                            4 -> "₹2.5"
                            5 -> "₹3"
                            6 -> "₹4"
                            else -> ""
                        },
                        isCompleted = day <= currentStreak && (day < currentStreak || isAlreadyClaimed),
                        isCurrent = day == currentStreak + 1 && !isAlreadyClaimed,
                        isLocked = day > currentStreak + 1 || (day == currentStreak + 1 && isAlreadyClaimed)
                    )
                }
                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(3) }) {
                    CheckInBox(
                        day = 7,
                        reward = "MEGA GIFT",
                        isCompleted = 7 <= currentStreak && (7 < currentStreak || isAlreadyClaimed),
                        isCurrent = 7 == currentStreak + 1 && !isAlreadyClaimed,
                        isLocked = 7 > currentStreak + 1 || (7 == currentStreak + 1 && isAlreadyClaimed),
                        isMega = true
                    )
                }
            }

            Button(
                onClick = { viewModel.claimDailyCheckIn() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isAlreadyClaimed,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VibrantGreen)
            ) {
                val buttonText = if (isAlreadyClaimed) "Already Claimed" else "Claim Day ${(currentStreak % 7) + 1} Reward"
                Text(
                    text = buttonText,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun CheckInBox(
    day: Int,
    reward: String,
    isCompleted: Boolean,
    isCurrent: Boolean,
    isLocked: Boolean,
    isMega: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val modifier = if (isCurrent) Modifier.scale(scale) else Modifier

    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(if (isMega) 3f else 1f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isCompleted -> Color(0xFFE8F5E9)
                isCurrent -> Color.White
                else -> Color(0xFFEEEEEE)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCurrent) 8.dp else 0.dp),
        border = if (isCurrent) androidx.compose.foundation.BorderStroke(2.dp, VibrantGreen) else null
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Day $day",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isLocked) Color.Gray else PrimaryEmerald,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (isCompleted) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = VibrantGreen, modifier = Modifier.size(24.dp))
                } else if (isMega) {
                    Text(text = "🎁", fontSize = 32.sp)
                } else if (isLocked) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(20.dp))
                } else {
                    Text(text = reward, fontWeight = FontWeight.ExtraBold, color = Color.Black, fontSize = 18.sp)
                }
            }
        }
    }
}
