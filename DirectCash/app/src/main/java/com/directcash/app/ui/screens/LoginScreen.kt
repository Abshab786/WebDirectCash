package com.directcash.app.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.directcash.app.R
import com.directcash.app.ui.theme.DirectCashTheme
import com.directcash.app.ui.theme.PrimaryEmerald
import com.directcash.app.ui.theme.VibrantGreen
import com.directcash.app.ui.viewmodel.FirebaseViewModel
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider

enum class LoginType {
    MOBILE, EMAIL
}

@Composable
fun LoginScreen(
    viewModel: FirebaseViewModel,
    onNavigateToOtp: (String, String, String) -> Unit, // phoneNumber, verificationId, referralCode
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as Activity

    var loginType by remember { mutableStateOf(LoginType.MOBILE) }
    var mobileNumber by remember { mutableStateOf("") }
    var emailAddress by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var referralCodeInput by remember { mutableStateOf("") }
    
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Subtle Background Patterns
        Icon(
            imageVector = Icons.Default.Payments,
            contentDescription = null,
            modifier = Modifier
                .size(150.dp)
                .align(Alignment.TopStart)
                .offset(x = (-40).dp, y = (-20).dp)
                .alpha(0.05f),
            tint = PrimaryEmerald
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Logo
            Image(
                painter = painterResource(id = R.drawable.logo_premium_new),
                contentDescription = "App Logo",
                modifier = Modifier.size(160.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Title & Subtitle
            Text(
                text = "Welcome to DirectCash",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Earn Real Cash by Completing Simple Tasks",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Premium Login Type Switcher
            LoginTypeSwitcher(
                selectedType = loginType,
                onTypeSelected = { loginType = it; errorMessage = null }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Animated Input Section
            AnimatedContent(
                targetState = loginType,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { width -> width } + fadeIn() togetherWith
                                slideOutHorizontally { width -> -width } + fadeOut()
                    } else {
                        slideInHorizontally { width -> -width } + fadeIn() togetherWith
                                slideOutHorizontally { width -> width } + fadeOut()
                    }.using(SizeTransform(clip = false))
                },
                label = "InputTransition"
            ) { type ->
                when (type) {
                    LoginType.MOBILE -> MobileInput(
                        value = mobileNumber,
                        onValueChange = { if (it.length <= 10) mobileNumber = it }
                    )
                    LoginType.EMAIL -> Column {
                        EmailInput(
                            value = emailAddress,
                            onValueChange = { emailAddress = it }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        PasswordInput(
                            value = password,
                            onValueChange = { password = it }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Referral Code Field (Optional)
            ReferralCodeInput(
                value = referralCodeInput,
                onValueChange = { referralCodeInput = it.uppercase() }
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Continue Button
            Button(
                onClick = { 
                    isLoading = true
                    errorMessage = null
                    if (loginType == LoginType.MOBILE) {
                        viewModel.sendOtp(
                            phoneNumber = mobileNumber,
                            activity = activity,
                            callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                                    viewModel.signInWithCredential(credential, referralCodeInput) { success ->
                                        isLoading = false
                                        if (success) onLoginSuccess()
                                        else errorMessage = "Auto-verification failed. Please enter OTP manually."
                                    }
                                }

                                override fun onVerificationFailed(e: FirebaseException) {
                                    isLoading = false
                                    errorMessage = e.localizedMessage ?: "Verification failed"
                                }

                                override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                                    isLoading = false
                                    onNavigateToOtp(mobileNumber, verificationId, referralCodeInput)
                                }
                            }
                        )
                    } else {
                        // First try to login, if fails try to register (standard UX for simple apps)
                        viewModel.loginWithEmail(emailAddress, password, referralCodeInput) { loginResult ->
                            if (loginResult == "SUCCESS") {
                                isLoading = false
                                onLoginSuccess()
                            } else {
                                // If login failed, try registration
                                viewModel.registerWithEmail(emailAddress, password, referralCodeInput) { regResult ->
                                    isLoading = false
                                    if (regResult == "SUCCESS") {
                                        onLoginSuccess()
                                    } else {
                                        errorMessage = regResult
                                    }
                                }
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = !isLoading && (if (loginType == LoginType.MOBILE) mobileNumber.length == 10 else emailAddress.contains("@") && password.length >= 6),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VibrantGreen,
                    contentColor = Color.White
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Continue",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.Center)
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.CenterEnd)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom trust items and wavy shape...
            Spacer(modifier = Modifier.weight(1f))
            
            // Developer label as requested in previous turn
            Text(
                text = "Developed by: AB ShAB",
                style = MaterialTheme.typography.labelSmall,
                color = Color.LightGray,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }
}

@Composable
fun PasswordInput(value: String, onValueChange: (String) -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF8F8F8)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Lock,
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            TextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { 
                    Text("Enter Password (min 6 chars)", color = Color.Gray) 
                },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = Color(0xFF4CAF50),
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                )
            )
        }
    }
}

@Composable
fun LoginTypeSwitcher(
    selectedType: LoginType,
    onTypeSelected: (LoginType) -> Unit
) {
    Box(
        modifier = Modifier
            .width(240.dp)
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFF5F5F5))
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(24.dp))
            .padding(4.dp)
    ) {
        val indicatorOffset by animateDpAsState(
            targetValue = if (selectedType == LoginType.MOBILE) 0.dp else 116.dp,
            animationSpec = tween(durationMillis = 300),
            label = "IndicatorOffset"
        )

        Box(
            modifier = Modifier
                .offset(x = indicatorOffset)
                .width(116.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(20.dp))
                .background(PrimaryEmerald)
        )

        Row(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onTypeSelected(LoginType.MOBILE) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Mobile",
                    color = if (selectedType == LoginType.MOBILE) Color.White else Color.Gray,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onTypeSelected(LoginType.EMAIL) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Email",
                    color = if (selectedType == LoginType.EMAIL) Color.White else Color.Gray,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun MobileInput(value: String, onValueChange: (String) -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF8F8F8)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.PhoneAndroid,
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "+91",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            VerticalDivider(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .height(24.dp),
                thickness = 1.dp,
                color = Color(0xFFE0E0E0)
            )
            TextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { 
                    Text("Enter Mobile Number", color = Color.Gray) 
                },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = Color(0xFF4CAF50),
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                )
            )
        }
    }
}

@Composable
fun ReferralCodeInput(value: String, onValueChange: (String) -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF1F8E9).copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.GroupAdd,
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            TextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { 
                    Text("Referral Code (Optional)", color = Color.Gray, fontSize = 14.sp) 
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = Color(0xFF4CAF50),
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                )
            )
        }
    }
}

@Composable
fun EmailInput(value: String, onValueChange: (String) -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF8F8F8)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Email,
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            TextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { 
                    Text("Enter Email Address", color = Color.Gray) 
                },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = Color(0xFF4CAF50),
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                )
            )
        }
    }
}
