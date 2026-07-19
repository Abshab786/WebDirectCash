package com.directcash.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import android.os.Build
import android.Manifest
import android.util.Log
import com.directcash.app.ads.UniversalAdManager
import com.google.android.gms.ads.MobileAds
import com.unity3d.ads.UnityAds
import com.unity3d.ads.metadata.MetaData
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.directcash.app.navigation.NavRoute
import com.directcash.app.ui.screens.*
import com.directcash.app.ui.theme.DirectCashTheme
import com.directcash.app.ui.viewmodel.MainViewModel
import com.directcash.app.ui.viewmodel.TaskViewModel
import com.directcash.app.ui.viewmodel.FirebaseViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Request Notification Permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {}.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        enableEdgeToEdge()
        setContent {
            DirectCashTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val backStack = rememberNavBackStack(NavRoute.Splash as NavKey)
                    val mainViewModel: MainViewModel = viewModel()
                    val taskViewModel: TaskViewModel = viewModel()
                    val firebaseViewModel: FirebaseViewModel = viewModel()
                    
                    val isMaintenance by firebaseViewModel.isMaintenance.collectAsState()
                    val adminSettings by firebaseViewModel.adminSettings.collectAsState()
                    val isAdsReady by firebaseViewModel.isAdsSdkReady.collectAsState()

                    LaunchedEffect(adminSettings.ads) {
                        // Strict Snapshot Timing & Fallback Logic
                        // If Firestore is slow (> 2s), use the hardcoded backup ID immediately
                        val currentAdsConfig = adminSettings.ads
                        val finalAdsConfig = if (currentAdsConfig.primaryAppId.isEmpty()) {
                            Log.w("MainActivity", "Firestore settings slow (>2s), using backup Game ID")
                            currentAdsConfig.copy(primaryAppId = "800081138")
                        } else {
                            currentAdsConfig
                        }
                        
                        UniversalAdManager.initialize(this@MainActivity, finalAdsConfig)
                    }

                    // Global Loading State until essential configs are ready
                    var showInitialLoader by remember { mutableStateOf(true) }
                    
                    LaunchedEffect(adminSettings.ads, isAdsReady) {
                        // Safety: If ads are enabled, wait for SDK, but only up to 4 seconds
                        if (adminSettings.ads.isAdsEnabled) {
                            if (isAdsReady) {
                                showInitialLoader = false
                            }
                        } else {
                            showInitialLoader = false
                        }
                        
                        // Hard timeout to prevent stuck loader
                        kotlinx.coroutines.delay(4000)
                        showInitialLoader = false
                    }

                    if (showInitialLoader && FirebaseAuth.getInstance().currentUser != null) {
                        Box(modifier = Modifier.fillMaxSize().background(Color.White), contentAlignment = androidx.compose.ui.Alignment.Center) {
                            CircularProgressIndicator(color = Color(0xFF00C853))
                        }
                    } else {
                        LaunchedEffect(isMaintenance) {
                            if (isMaintenance) {
                                while (backStack.size > 0) {
                                    backStack.removeAt(backStack.size - 1)
                                }
                                backStack.add(NavRoute.Maintenance as NavKey)
                            }
                        }

                        NavDisplay(
                            backStack = backStack,
                            onBack = { 
                                if (backStack.size > 1 && !isMaintenance) {
                                    backStack.removeAt(backStack.size - 1)
                                } else if (!isMaintenance) {
                                    finish()
                                }
                            }
                        ) { key ->
                            when (key) {
                                is NavRoute.Maintenance -> NavEntry(key) {
                                    MaintenanceScreen()
                                }
                                is NavRoute.Splash -> NavEntry(key) {
                                    SplashScreen(
                                        onTimeout = {
                                            backStack.removeAt(backStack.size - 1)
                                            if (FirebaseAuth.getInstance().currentUser != null) {
                                                backStack.add(NavRoute.Dashboard as NavKey)
                                            } else {
                                                backStack.add(NavRoute.Login as NavKey)
                                            }
                                        }
                                    )
                                }
                                is NavRoute.Login -> NavEntry(key) {
                                    LoginScreen(
                                        viewModel = firebaseViewModel,
                                        onNavigateToOtp = { phoneNumber, verificationId, referralCode ->
                                            backStack.add(NavRoute.Otp(phoneNumber, verificationId, referralCode) as NavKey)
                                        },
                                        onLoginSuccess = {
                                            while (backStack.isNotEmpty()) {
                                                backStack.removeAt(backStack.size - 1)
                                            }
                                            backStack.add(NavRoute.Dashboard as NavKey)
                                        }
                                    )
                                }
                                is NavRoute.Otp -> NavEntry(key) {
                                    val otpRoute = key as NavRoute.Otp
                                    OtpScreen(
                                        phoneNumber = otpRoute.phoneNumber,
                                        verificationId = otpRoute.verificationId,
                                        referralCode = otpRoute.referralCode,
                                        viewModel = firebaseViewModel,
                                        onVerifySuccess = {
                                            while (backStack.isNotEmpty()) {
                                                backStack.removeAt(backStack.size - 1)
                                            }
                                            backStack.add(NavRoute.Dashboard as NavKey)
                                        },
                                        onBack = {
                                            backStack.removeAt(backStack.size - 1)
                                        }
                                    )
                                }
                                is NavRoute.Dashboard -> NavEntry(key) {
                                    MainContainer(
                                        viewModel = mainViewModel,
                                        onNavigateToTask = { taskId ->
                                            backStack.add(NavRoute.TaskDetail(taskId) as NavKey)
                                        },
                                        onNavigateToSpin = {
                                            backStack.add(NavRoute.Spin as NavKey)
                                        },
                                        onNavigateToOfferwall = {
                                            backStack.add(NavRoute.OfferwallSelection as NavKey)
                                        },
                                        onNavigateToReferral = {
                                            backStack.add(NavRoute.Referral as NavKey)
                                        },
                                        onNavigateToDailyCheckIn = {
                                            backStack.add(NavRoute.DailyCheckIn as NavKey)
                                        },
                                        onNavigateToSuperTaskHub = {
                                            backStack.add(NavRoute.SuperTaskHub as NavKey)
                                        },
                                        onLogout = {
                                            while (backStack.isNotEmpty()) {
                                                backStack.removeAt(backStack.size - 1)
                                            }
                                            backStack.add(NavRoute.Login as NavKey)
                                        }
                                    )
                                }
                                is NavRoute.Spin -> NavEntry(key) {
                                    SpinScreen(
                                        viewModel = firebaseViewModel,
                                        onBack = { backStack.removeAt(backStack.size - 1) }
                                    )
                                }
                                is NavRoute.TaskDetail -> NavEntry(key) {
                                    TaskDetailScreen(
                                        taskId = (key as NavRoute.TaskDetail).taskId,
                                        mainViewModel = mainViewModel,
                                        taskViewModel = taskViewModel,
                                        firebaseViewModel = firebaseViewModel,
                                        onBack = { backStack.removeAt(backStack.size - 1) },
                                        onComplete = {
                                            backStack.removeAt(backStack.size - 1)
                                        }
                                    )
                                }
                                is NavRoute.Offerwall -> NavEntry(key) {
                                    OfferwallScreen(
                                        url = (key as NavRoute.Offerwall).url,
                                        onBack = { backStack.removeAt(backStack.size - 1) }
                                    )
                                }
                                is NavRoute.Referral -> NavEntry(key) {
                                    ReferralScreen(
                                        viewModel = firebaseViewModel,
                                        onBack = { backStack.removeAt(backStack.size - 1) }
                                    )
                                }
                                is NavRoute.DailyCheckIn -> NavEntry(key) {
                                    DailyCheckInScreen(
                                        viewModel = firebaseViewModel,
                                        onBack = { backStack.removeAt(backStack.size - 1) }
                                    )
                                }
                                is NavRoute.AdminPanel -> NavEntry(key) {
                                    AdminPanelScreen(
                                        viewModel = firebaseViewModel,
                                        onBack = { backStack.removeAt(backStack.size - 1) }
                                    )
                                }
                                is NavRoute.SuperTaskHub -> NavEntry(key) {
                                    SuperTaskHubScreen(
                                        viewModel = firebaseViewModel,
                                        onBack = { backStack.removeAt(backStack.size - 1) },
                                        onNavigateToOfferwall = {
                                            backStack.add(NavRoute.OfferwallSelection as NavKey)
                                        },
                                        onNavigateToScratch = {
                                            backStack.add(NavRoute.Scratch as NavKey)
                                        },
                                        onNavigateToCaptcha = {
                                            backStack.add(NavRoute.Captcha as NavKey)
                                        },
                                        onNavigateToQuiz = {
                                            backStack.add(NavRoute.MathQuiz as NavKey)
                                        }
                                    )
                                }
                                is NavRoute.OfferwallSelection -> NavEntry(key) {
                                    OfferwallSelectionScreen(
                                        viewModel = firebaseViewModel,
                                        onBack = { backStack.removeAt(backStack.size - 1) },
                                        onNavigateToOfferwall = { url ->
                                            backStack.add(NavRoute.Offerwall(url) as NavKey)
                                        }
                                    )
                                }
                                is NavRoute.Scratch -> NavEntry(key) {
                                    ScratchScreen(
                                        viewModel = firebaseViewModel,
                                        onBack = { backStack.removeAt(backStack.size - 1) }
                                    )
                                }
                                is NavRoute.Captcha -> NavEntry(key) {
                                    CaptchaScreen(
                                        viewModel = firebaseViewModel,
                                        onBack = { backStack.removeAt(backStack.size - 1) }
                                    )
                                }
                                is NavRoute.MathQuiz -> NavEntry(key) {
                                    MathQuizScreen(
                                        viewModel = firebaseViewModel,
                                        onBack = { backStack.removeAt(backStack.size - 1) }
                                    )
                                }
                                else -> NavEntry(key) {
                                    Surface(modifier = Modifier.fillMaxSize()) {}
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
