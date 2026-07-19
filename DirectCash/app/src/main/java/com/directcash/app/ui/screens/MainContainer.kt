package com.directcash.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.directcash.app.R
import com.directcash.app.ui.viewmodel.MainViewModel
import com.directcash.app.ui.viewmodel.TaskViewModel
import com.directcash.app.ui.viewmodel.FirebaseViewModel

sealed class BottomNavScreen(val route: String, val icon: ImageVector, val label: String) {
    object Home : BottomNavScreen("home", Icons.Default.Home, "Home")
    object Tasks : BottomNavScreen("tasks", Icons.Default.List, "Tasks")
    object DirectEarning : BottomNavScreen("direct_earning", Icons.Default.Home, "Earning")
    object Wallet : BottomNavScreen("wallet", Icons.Default.Wallet, "Wallet")
    object Profile : BottomNavScreen("profile", Icons.Default.AccountCircle, "Profile")
}

@Composable
fun MainContainer(
    viewModel: MainViewModel,
    onNavigateToTask: (Long) -> Unit,
    onNavigateToSpin: () -> Unit,
    onNavigateToOfferwall: (String) -> Unit,
    onNavigateToReferral: () -> Unit,
    onNavigateToDailyCheckIn: () -> Unit,
    onNavigateToSuperTaskHub: () -> Unit,
    onLogout: () -> Unit
) {
    var selectedScreen by remember { mutableStateOf<BottomNavScreen>(BottomNavScreen.Home) }
    val taskViewModel: TaskViewModel = viewModel()
    val firebaseViewModel: FirebaseViewModel = viewModel()

    Scaffold(
        bottomBar = {
            Column {
                HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFE0E0E0))
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp,
                    modifier = Modifier.height(80.dp)
                ) {
                    // Left Items
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        selected = selectedScreen == BottomNavScreen.Home,
                        onClick = { selectedScreen = BottomNavScreen.Home },
                        colors = navigationItemColors()
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.List, contentDescription = "Tasks") },
                        label = { Text("Tasks") },
                        selected = selectedScreen == BottomNavScreen.Tasks,
                        onClick = { selectedScreen = BottomNavScreen.Tasks },
                        colors = navigationItemColors()
                    )

                    // Center Item (The Brand Logo Hub)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        FloatingActionButton(
                            onClick = onNavigateToSuperTaskHub,
                            containerColor = Color.White,
                            shape = CircleShape,
                            elevation = FloatingActionButtonDefaults.elevation(4.dp),
                            modifier = Modifier
                                .size(64.dp)
                                .offset(y = (-15).dp)
                                .border(2.dp, Color(0xFF00C853), CircleShape)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.logo_premium_new),
                                contentDescription = "Direct Earning Hub",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    // Right Items
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Wallet, contentDescription = "Wallet") },
                        label = { Text("Wallet") },
                        selected = selectedScreen == BottomNavScreen.Wallet,
                        onClick = { selectedScreen = BottomNavScreen.Wallet },
                        colors = navigationItemColors()
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Profile") },
                        label = { Text("Profile") },
                        selected = selectedScreen == BottomNavScreen.Profile,
                        onClick = { selectedScreen = BottomNavScreen.Profile },
                        colors = navigationItemColors()
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize().background(Color(0xFFF9F9F9))) {
            when (selectedScreen) {
                BottomNavScreen.Home -> DashboardScreen(
                    firebaseViewModel = firebaseViewModel,
                    onTaskClick = onNavigateToTask,
                    onSpinClick = onNavigateToSpin,
                    onOfferwallClick = onNavigateToSuperTaskHub,
                    onWithdrawClick = { selectedScreen = BottomNavScreen.Wallet },
                    onReferClick = onNavigateToReferral,
                    onDailyCheckInClick = onNavigateToDailyCheckIn
                )
                BottomNavScreen.Tasks -> TasksScreen(
                    taskViewModel = taskViewModel,
                    firebaseViewModel = firebaseViewModel,
                    onTaskClick = onNavigateToTask
                )
                BottomNavScreen.DirectEarning -> DirectEarningScreen(firebaseViewModel)
                BottomNavScreen.Wallet -> WalletScreen(firebaseViewModel)
                BottomNavScreen.Profile -> ProfileScreen(
                    mainViewModel = viewModel,
                    firebaseViewModel = firebaseViewModel,
                    onLogout = onLogout
                )
            }
        }
    }
}

@Composable
fun navigationItemColors() = NavigationBarItemDefaults.colors(
    indicatorColor = Color.Transparent,
    selectedIconColor = Color(0xFF00C853),
    unselectedIconColor = Color(0xFF757575),
    selectedTextColor = Color(0xFF00C853),
    unselectedTextColor = Color(0xFF757575)
)
