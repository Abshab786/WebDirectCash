package com.directcash.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.directcash.app.R
import com.directcash.app.ui.theme.DirectCashTheme
import com.directcash.app.ui.theme.PrimaryEmerald
import com.directcash.app.ui.theme.SecondaryEmerald

@Composable
fun SplashScreen(
    onTimeout: () -> Unit
) {
    // Premium Fintech Gradient
    val gradientBrush = Brush.linearGradient(
        colors = listOf(PrimaryEmerald, SecondaryEmerald)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Official Logo
            Image(
                painter = painterResource(id = R.drawable.logo_premium_new),
                contentDescription = "App Logo",
                modifier = Modifier.size(280.dp)
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Crisp White Loading Bar
            LinearProgressIndicator(
                modifier = Modifier
                    .width(160.dp)
                    .height(6.dp),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.2f),
                strokeCap = StrokeCap.Round
            )
        }
        
        // Footer Text
        Text(
            text = "Secure Payments via UPI",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.9f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp),
            letterSpacing = 1.2.sp,
            fontWeight = FontWeight.SemiBold
        )
    }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(2000)
        onTimeout()
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun SplashScreenPreview() {
    DirectCashTheme {
        SplashScreen(onTimeout = {})
    }
}
