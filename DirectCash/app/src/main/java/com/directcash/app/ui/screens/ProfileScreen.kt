package com.directcash.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.directcash.app.ui.theme.PrimaryEmerald
import com.directcash.app.ui.theme.SecondaryEmerald
import com.directcash.app.ui.theme.VibrantGreen
import com.directcash.app.ui.viewmodel.MainViewModel
import com.directcash.app.ui.viewmodel.FirebaseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    mainViewModel: MainViewModel,
    firebaseViewModel: FirebaseViewModel,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val name by firebaseViewModel.userName.collectAsState()
    val totalEarnings by firebaseViewModel.totalEarnings.collectAsState()

    var showLegalSheet by remember { mutableStateOf(false) }
    var legalSheetTitle by remember { mutableStateOf("") }
    var legalSheetContent by remember { mutableStateOf("") }

    val sheetState = rememberModalBottomSheetState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9F9F9))
    ) {
        // Profile Header with Dynamic Data
        ProfileHeader(
            name = name.ifEmpty { "Ajay" }, 
            phone = "Verified User" 
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Earnings Summary Card
            item {
                EarningsSummaryCard(totalEarnings = totalEarnings)
            }

            // Support Section
            item {
                ProfileSection(title = "Support & Help") {
                    ProfileMenuItem(
                        icon = Icons.Rounded.Email,
                        label = "Email Support",
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:ab.shabofficial@gmail.com")
                                putExtra(Intent.EXTRA_SUBJECT, "DirectCash App Support")
                            }
                            context.startActivity(Intent.createChooser(intent, "Send Email"))
                        }
                    )
                    ProfileMenuItem(
                        icon = Icons.Rounded.Language,
                        label = "Visit Website",
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://abshab.com"))
                            context.startActivity(intent)
                        }
                    )
                }
            }

            // Legal Section
            item {
                ProfileSection(title = "Legal & About") {
                    ProfileMenuItem(
                        icon = Icons.Rounded.Info,
                        label = "Privacy Policy",
                        onClick = {
                            legalSheetTitle = "Privacy Policy"
                            legalSheetContent = "DirectCash respects your privacy. We securely process your authentication via Firebase and never share your personal data or wallet information with third parties. Ads are served securely via Google AdMob."
                            showLegalSheet = true
                        }
                    )
                    ProfileMenuItem(
                        icon = Icons.Rounded.Description,
                        label = "Terms & Conditions",
                        onClick = {
                            legalSheetTitle = "Terms & Conditions"
                            legalSheetContent = "By using DirectCash, you agree that rewards are granted only upon full completion of tasks and ads. Any attempt to use VPNs, bots, or manipulate the spin counter will lead to immediate account suspension without payout."
                            showLegalSheet = true
                        }
                    )
                }
            }

            // Settings Section
            item {
                ProfileSection(title = "Settings") {
                    var notificationsEnabled by remember { mutableStateOf(true) }
                    ProfileMenuToggle(
                        icon = Icons.Rounded.Notifications,
                        label = "Notifications",
                        checked = notificationsEnabled,
                        onCheckedChange = { notificationsEnabled = it }
                    )
                }
            }

            // Logout Button
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        firebaseViewModel.logout()
                        onLogout()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFEBEE),
                        contentColor = Color.Red
                    )
                ) {
                    Icon(imageVector = Icons.Rounded.Logout, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Logout", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }
            }

            // Developer Label
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Developed by: AB ShAB",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Version 1.0.0",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.LightGray,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // In-App Legal Bottom Sheet
    if (showLegalSheet) {
        ModalBottomSheet(
            onDismissRequest = { showLegalSheet = false },
            sheetState = sheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 48.dp)
            ) {
                Text(
                    text = legalSheetTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = PrimaryEmerald
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = legalSheetContent,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.DarkGray,
                    lineHeight = 24.sp
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { showLegalSheet = false },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = VibrantGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("I Understand", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ProfileHeader(name: String, phone: String) {
    val gradientBrush = Brush.linearGradient(
        colors = listOf(PrimaryEmerald, SecondaryEmerald)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(gradientBrush)
            .padding(top = 48.dp, bottom = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color.White.copy(alpha = 0.2f), CircleShape)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name.take(1).uppercase(),
                    color = Color.White,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = name,
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = phone,
                color = Color.White.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun EarningsSummaryCard(totalEarnings: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "My Lifetime Earnings",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Text(
                    text = "₹${"%.2f".format(totalEarnings)}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = VibrantGreen
                )
            }
            Icon(
                imageVector = Icons.Rounded.TrendingUp,
                contentDescription = null,
                tint = VibrantGreen,
                modifier = Modifier.size(40.dp)
            )
        }
    }
}

@Composable
fun ProfileSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = Color.Gray,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
fun ProfileMenuItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(Color(0xFFF1F8E9), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = PrimaryEmerald, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = Color.LightGray
        )
    }
}

@Composable
fun ProfileMenuToggle(icon: ImageVector, label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(Color(0xFFF1F8E9), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = PrimaryEmerald, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = VibrantGreen
            )
        )
    }
}
