package com.directcash.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.directcash.app.ui.viewmodel.FirebaseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfferwallSelectionScreen(
    viewModel: FirebaseViewModel,
    onBack: () -> Unit,
    onNavigateToOfferwall: (String) -> Unit
) {
    val offerwallSettings by viewModel.offerwallSettings.collectAsState()
    val userId = viewModel.userId ?: "guest"

    // Loading State: Show loader if essential data (like CPAGrip URL) is missing while we wait for Firestore
    val isLoading = offerwallSettings.cpagripUrl.isEmpty() && 
                    !offerwallSettings.isPubscaleEnabled && 
                    !offerwallSettings.isMonlixEnabled

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("OFFERWALLS & INSTALLS") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF00C853))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // CPAGrip
                if (offerwallSettings.isCpagripEnabled) {
                    item {
                        OfferwallItem(
                            name = "CPAGrip Premium",
                            description = "High paying app installs & surveys",
                            color = Color(0xFFE8F5E9)
                        ) {
                            val url = if (offerwallSettings.cpagripUrl.contains("?")) {
                                "${offerwallSettings.cpagripUrl}&tracking_id=$userId"
                            } else {
                                "${offerwallSettings.cpagripUrl}?tracking_id=$userId"
                            }
                            onNavigateToOfferwall(url)
                        }
                    }
                }

                // PubScale
                if (offerwallSettings.isPubscaleEnabled) {
                    item {
                        OfferwallItem(
                            name = "PubScale AI",
                            description = "Smart programmatic tasks",
                            color = Color(0xFFE3F2FD)
                        ) {
                            onNavigateToOfferwall("https://sdk.pubscale.com/v1/offerwall?app_id=${offerwallSettings.pubscaleAppId}&user_id=$userId")
                        }
                    }
                }

                // CPAlead
                if (offerwallSettings.isCpaleadEnabled) {
                    item {
                        OfferwallItem(
                            name = "CPAlead Hub",
                            description = "Simple tasks for quick rewards",
                            color = Color(0xFFFFF3E0)
                        ) {
                            onNavigateToOfferwall("https://www.cpalead.com/mobile/locker.php?pub=${offerwallSettings.cpaleadSourceId}&subid=$userId")
                        }
                    }
                }

                // Monlix
                if (offerwallSettings.isMonlixEnabled) {
                    item {
                        OfferwallItem(
                            name = "Monlix Engine",
                            description = "Premium surveys & wall",
                            color = Color(0xFFF3E5F5)
                        ) {
                            onNavigateToOfferwall("https://offers.monlix.com/?appid=${offerwallSettings.monlixApiKey}&userid=$userId")
                        }
                    }
                }

                // Notik
                if (offerwallSettings.isNotikEnabled) {
                    item {
                        OfferwallItem(
                            name = "Notik Offers",
                            description = "Exclusive app rewards",
                            color = Color(0xFFE0F2F1)
                        ) {
                            onNavigateToOfferwall("https://notik.me/offers?pubid=YOUR_PUB_ID&app=YOUR_APP_ID&user=$userId")
                        }
                    }
                }

                // Adsterra
                if (offerwallSettings.isAdsterraEnabled) {
                    item {
                        OfferwallItem(
                            name = "Adsterra CPA",
                            description = "Fast earning tasks",
                            color = Color(0xFFFCE4EC)
                        ) {
                            onNavigateToOfferwall("https://publishers.adsterra.com/preview/${offerwallSettings.adsterraCpaToken}?subid=$userId")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OfferwallItem(name: String, description: String, color: Color, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = color,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Download, null, tint = Color.DarkGray)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(description, fontSize = 12.sp, color = Color.Gray)
            }
            Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
        }
    }
}
