package com.directcash.app.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.directcash.app.ads.UniversalAdManager
import com.directcash.app.ui.theme.VibrantGreen
import com.directcash.app.ui.viewmodel.FirebaseViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

data class WheelSection(
    val amount: Double,
    val label: String,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpinScreen(
    viewModel: FirebaseViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as Activity
    val scope = rememberCoroutineScope()
    
    val dailySpinCount by viewModel.dailySpinCount.collectAsState()
    val lastSpinDate by viewModel.lastSpinDate.collectAsState()
    val spinStatus by viewModel.spinStatus.collectAsState()
    val isAdsSdkReady by viewModel.isAdsSdkReady.collectAsState()
    val isRewardedAdLoaded by viewModel.isRewardedAdLoaded.collectAsState()
    
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val remainingSpins = if (lastSpinDate == today) 5 - dailySpinCount else 5

    // Slices configuration as requested
    val sections = listOf(
        WheelSection(1.0, "₹1", Color(0xFFFF5252)),           // Red
        WheelSection(2.0, "₹2", Color(0xFF448AFF)),           // Blue
        WheelSection(0.0, "TRY AGAIN", Color(0xFFFFD740)),     // Yellow (Better Luck Slot 1)
        WheelSection(5.0, "₹5", Color(0xFF69F0AE)),           // Green
        WheelSection(3.0, "₹3", Color(0xFFFFAB40)),           // Orange
        WheelSection(0.0, "TRY AGAIN", Color(0xFFE040FB))      // Purple (Better Luck Slot 2)
    )

    val rotation = remember { Animatable(0f) }
    var isSpinning by remember { mutableStateOf(false) }

    LaunchedEffect(spinStatus) {
        spinStatus?.let { status ->
            when (status) {
                "SUCCESS" -> { /* Logic handled after animation completes */ }
                "LIMIT_REACHED" -> Toast.makeText(context, "Daily limit reached!", Toast.LENGTH_SHORT).show()
                "ERROR" -> Toast.makeText(context, "Error claiming reward.", Toast.LENGTH_SHORT).show()
            }
            viewModel.resetSpinStatus()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Spin & Win", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF9F9F9)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Spins Remaining Today: $remainingSpins",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = VibrantGreen
                )
                
                Spacer(modifier = Modifier.height(48.dp))
                
                // Wheel UI
                Box(
                    modifier = Modifier.size(320.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .rotate(rotation.value)
                    ) {
                        val canvasSize = size
                        val radius = canvasSize.minDimension / 2
                        val center = Offset(canvasSize.width / 2, canvasSize.height / 2)
                        val anglePerSection = 360f / sections.size

                        sections.forEachIndexed { index, section ->
                            drawArc(
                                color = section.color,
                                startAngle = index * anglePerSection,
                                sweepAngle = anglePerSection,
                                useCenter = true,
                                size = Size(radius * 2, radius * 2),
                                topLeft = Offset(center.x - radius, center.y - radius)
                            )

                            // Draw text on slices with enhanced visibility
                            val textAngle = (index * anglePerSection + anglePerSection / 2) * (PI / 180).toFloat()
                            val textRadius = radius * 0.65f
                            val x = center.x + cos(textAngle) * textRadius
                            val y = center.y + sin(textAngle) * textRadius

                            drawContext.canvas.nativeCanvas.save()
                            drawContext.canvas.nativeCanvas.rotate(
                                (index * anglePerSection + anglePerSection / 2) + 90,
                                x,
                                y
                            )
                            
                            // High-contrast, Bold, Larger Text
                            drawContext.canvas.nativeCanvas.drawText(
                                section.label,
                                x,
                                y,
                                android.graphics.Paint().apply {
                                    color = android.graphics.Color.BLACK
                                    textSize = 52f // Even larger text size
                                    textAlign = android.graphics.Paint.Align.CENTER
                                    typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                                }
                            )
                            drawContext.canvas.nativeCanvas.restore()
                        }

                        // Wheel Border
                        drawCircle(
                            color = Color(0xFF212121),
                            radius = radius,
                            center = center,
                            style = Stroke(width = 10.dp.toPx())
                        )
                    }
                    
                    // Arrow Pointer
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier
                            .size(70.dp)
                            .offset(y = (-30).dp)
                    )
                    
                    // Center Decoration
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.Center)
                            .background(Color.White, CircleShape)
                            .padding(4.dp)
                            .background(Color(0xFF212121), CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(64.dp))

                Button(
                    onClick = {
                        if (remainingSpins > 0 && !isSpinning) {
                            if (!isRewardedAdLoaded) {
                                Toast.makeText(context, "Ad is buffering, please try again in 3 seconds...", Toast.LENGTH_SHORT).show()
                                viewModel.forceLoadAd(true)
                                return@Button
                            }

                            UniversalAdManager.showRewardedAd(
                                activity = activity,
                                onRewardEarned = {
                                    isSpinning = true
                                    val randomSectionIndex = (sections.indices).random()
                                    val selectedSection = sections[randomSectionIndex]
                                    
                                    val anglePerSection = 360f / sections.size
                                    // Calculate rotation to align the selected slice with the pointer (at top, 270 degrees)
                                    val targetRotation = 270f - (randomSectionIndex * anglePerSection + anglePerSection / 2)
                                    val finalRotation = rotation.value + (360f * 6) + targetRotation 
                                    
                                    scope.launch {
                                        rotation.animateTo(
                                            targetValue = finalRotation,
                                            animationSpec = tween(
                                                durationMillis = 4000,
                                                easing = FastOutSlowInEasing
                                            )
                                        )
                                        isSpinning = false
                                        
                                        // Reward logic fix: land on 'Better Luck' credits ₹0
                                        viewModel.claimSpinReward(selectedSection.amount)
                                        
                                        if (selectedSection.amount > 0) {
                                            Toast.makeText(context, "🎉 Congratulations! You won ₹${"%.0f".format(selectedSection.amount)}", Toast.LENGTH_LONG).show()
                                        } else {
                                            // Handle 'Better Luck Next Time' (Try Again)
                                            Toast.makeText(context, "Better luck next time! Try again.", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                },
                                onFailure = { error ->
                                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                                }
                            )
                        } else if (remainingSpins <= 0) {
                            Toast.makeText(context, "Daily limit reached! Come back tomorrow.", Toast.LENGTH_LONG).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(0.75f).height(60.dp),
                    enabled = !isSpinning && isAdsSdkReady,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isAdsSdkReady) VibrantGreen else Color.Gray
                    )
                ) {
                    if (isSpinning) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(28.dp))
                    } else if (!isAdsSdkReady) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("INITIALIZING...", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Text("SPIN NOW", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
        }
    }
}
