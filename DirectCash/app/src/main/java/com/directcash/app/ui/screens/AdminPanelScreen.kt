package com.directcash.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.directcash.app.data.repository.UniversalAdSettings
import com.directcash.app.ui.viewmodel.FirebaseViewModel
import com.directcash.app.data.repository.AdminSettings

private val DeepSlate = Color(0xFF121212)
private val SurfaceSlate = Color(0xFF1E1E1E)
private val NeonGreen = Color(0xFF00E676)
private val ElectricCrimson = Color(0xFFFF5252)
private val GoldAmber = Color(0xFFFFD700)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    viewModel: FirebaseViewModel,
    onBack: () -> Unit
) {
    val adminSettings by viewModel.adminSettings.collectAsState()
    val pendingWithdrawals by viewModel.pendingWithdrawals.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedUser by remember { mutableStateOf<Map<String, Any>?>(null) }
    
    // Config states
    var minLimit by remember(adminSettings.minWithdrawalLimit) { mutableStateOf(adminSettings.minWithdrawalLimit.toString()) }
    var dailyAdLimit by remember(adminSettings.dailyAdLimit) { mutableStateOf(adminSettings.dailyAdLimit.toString()) }
    var videoRate by remember(adminSettings.rewardedVideoRate) { mutableStateOf(adminSettings.rewardedVideoRate.toString()) }
    var commissionFee by remember(adminSettings.processingCommissionFee) { mutableStateOf(adminSettings.processingCommissionFee.toString()) }

    val netIncome = adminSettings.totalGrossRevenue - adminSettings.totalDisbursements
    val incomeColor = if (netIncome >= 0) NeonGreen else ElectricCrimson

    Box(modifier = Modifier.fillMaxSize().background(DeepSlate)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Executive Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "ENTERPRISE CONTROL CENTER",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                    Text("P&L ANALYTICS ENGINE ACTIVE", color = NeonGreen, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.background(SurfaceSlate, CircleShape)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Exit", tint = Color.White)
                }
            }

            // Executive P&L Analytics Row
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AnalyticsCard(
                    title = "GROSS AD REVENUE",
                    value = "₹${String.format("%.2f", adminSettings.totalGrossRevenue)}",
                    icon = Icons.Default.Payments,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                AnalyticsCard(
                    title = "USER DISBURSEMENTS",
                    value = "₹${String.format("%.2f", adminSettings.totalDisbursements)}",
                    icon = Icons.Default.Outbox,
                    color = ElectricCrimson,
                    modifier = Modifier.weight(1f)
                )
                AnalyticsCard(
                    title = "NET OPERATIONAL MARGIN",
                    value = "₹${String.format("%.2f", netIncome)}",
                    icon = Icons.Default.ShowChart,
                    color = incomeColor,
                    modifier = Modifier.weight(1f),
                    isGlow = true
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Main Matrix Grid
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Column 1: System Feature Modifier
                Column(modifier = Modifier.weight(0.2f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SectionHeader("APP FEATURE MODIFIER")
                    MatrixPanel {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            FuturisticInput(
                                value = videoRate,
                                onValueChange = { videoRate = it },
                                label = "Rewarded Video Rate (₹)",
                                icon = Icons.Default.SmartDisplay
                            )
                            FuturisticInput(
                                value = dailyAdLimit,
                                onValueChange = { dailyAdLimit = it },
                                label = "Daily Ad Cap (Views)",
                                icon = Icons.Default.LockClock
                            )
                            FuturisticInput(
                                value = minLimit,
                                onValueChange = { minLimit = it },
                                label = "Min Withdrawal Boundary (₹)",
                                icon = Icons.Default.RequestQuote
                            )
                            FuturisticInput(
                                value = commissionFee,
                                onValueChange = { commissionFee = it },
                                label = "Platform Commission Fee (%)",
                                icon = Icons.Default.Percent
                            )

                            HorizontalDivider()

                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("MAINTENANCE MODE", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                    Text("SUSPEND ALL SESSIONS", color = Color.Gray, fontSize = 9.sp)
                                }
                                Switch(
                                    checked = adminSettings.maintenanceMode,
                                    onCheckedChange = { 
                                        viewModel.updateAdminSettings(adminSettings.copy(maintenanceMode = it))
                                    },
                                    colors = SwitchDefaults.colors(checkedThumbColor = NeonGreen)
                                )
                            }

                            Button(
                                onClick = {
                                    viewModel.updateAdminSettings(
                                        adminSettings.copy(
                                            minWithdrawalLimit = minLimit.toDoubleOrNull() ?: 100.0,
                                            dailyAdLimit = dailyAdLimit.toIntOrNull() ?: 10,
                                            rewardedVideoRate = videoRate.toDoubleOrNull() ?: 0.05,
                                            processingCommissionFee = commissionFee.toDoubleOrNull() ?: 5.0
                                        )
                                    )
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("COMMIT GLOBAL CHANGES", fontWeight = FontWeight.Black, color = Color.Black, fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Column 2: Super Task Hub Config (New)
                Column(modifier = Modifier.weight(0.2f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SectionHeader("SUPER TASK HUB MODIFIER")
                    MatrixPanel {
                        var scratchLimit by remember(adminSettings.scratchDailyLimit) { mutableStateOf(adminSettings.scratchDailyLimit.toString()) }
                        var scratchMax by remember(adminSettings.scratchMaxReward) { mutableStateOf(adminSettings.scratchMaxReward.toString()) }
                        var captchaRew by remember(adminSettings.captchaReward) { mutableStateOf(adminSettings.captchaReward.toString()) }
                        var quizRew by remember(adminSettings.mathQuizReward) { mutableStateOf(adminSettings.mathQuizReward.toString()) }
                        var installRew by remember(adminSettings.appInstallReward) { mutableStateOf(adminSettings.appInstallReward.toString()) }

                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            FuturisticInput(scratchLimit, { scratchLimit = it }, "Scratch Daily Limit", Icons.Default.Style)
                            FuturisticInput(scratchMax, { scratchMax = it }, "Scratch Max Reward (₹)", Icons.Default.WorkspacePremium)
                            FuturisticInput(captchaRew, { captchaRew = it }, "Captcha Reward (₹)", Icons.Default.TextFields)
                            FuturisticInput(quizRew, { quizRew = it }, "Math Quiz Reward (₹)", Icons.Default.Calculate)
                            FuturisticInput(installRew, { installRew = it }, "App Install Reward (₹)", Icons.Default.Download)
                            
                            Button(
                                onClick = {
                                    viewModel.updateAdminSettings(adminSettings.copy(
                                        scratchDailyLimit = scratchLimit.toIntOrNull() ?: 20,
                                        scratchMaxReward = scratchMax.toDoubleOrNull() ?: 1.0,
                                        captchaReward = captchaRew.toDoubleOrNull() ?: 0.02,
                                        mathQuizReward = quizRew.toDoubleOrNull() ?: 0.02,
                                        appInstallReward = installRew.toDoubleOrNull() ?: 5.0
                                    ))
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = GoldAmber),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("UPDATE HUB REWARDS", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }

                // Column 3: User Deep-Search & Penalty
                Column(modifier = Modifier.weight(0.2f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SectionHeader("USER MANAGEMENT ENGINE")
                    
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { 
                            searchQuery = it
                            if (it.length > 2) viewModel.searchUser(it)
                        },
                        placeholder = { Text("Search Name / UID...", color = Color.Gray, fontSize = 14.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SurfaceSlate,
                            unfocusedContainerColor = SurfaceSlate,
                            focusedBorderColor = NeonGreen,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray) }
                    )

                    if (selectedUser == null) {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(searchResults) { user ->
                                SearchResultItem(user) { selectedUser = user }
                            }
                        }
                    } else {
                        UserExecutiveCard(
                            user = selectedUser!!,
                            onClose = { selectedUser = null },
                            viewModel = viewModel
                        )
                    }
                }

                // Column 4: Live Payout Queue
                Column(modifier = Modifier.weight(0.2f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SectionHeader("LIVE PAYOUT QUEUE")
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(pendingWithdrawals) { req ->
                            PayoutExecutiveRow(req, viewModel)
                        }
                    }
                }

                // Column 5: Master Network Switchboard
                Column(modifier = Modifier.weight(0.2f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SectionHeader("MASTER NETWORK SWITCHBOARD")
                    MatrixPanel {
                        var activeNet by remember(adminSettings.ads.activeNetwork) { mutableStateOf(adminSettings.ads.activeNetwork) }
                        var appId by remember(adminSettings.ads.primaryAppId) { mutableStateOf(adminSettings.ads.primaryAppId) }
                        var rewardedId by remember(adminSettings.ads.rewardedPlacementId) { mutableStateOf(adminSettings.ads.rewardedPlacementId) }
                        var interstitialId by remember(adminSettings.ads.interstitialPlacementId) { mutableStateOf(adminSettings.ads.interstitialPlacementId) }
                        var bannerId by remember(adminSettings.ads.bannerPlacementId) { mutableStateOf(adminSettings.ads.bannerPlacementId) }
                        var isAdsEnabled by remember(adminSettings.ads.isAdsEnabled) { mutableStateOf(adminSettings.ads.isAdsEnabled) }

                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("ACTIVE AD NETWORK", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("UNITY", "ADMOB", "APPLOVIN").forEach { net ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(if (activeNet == net) NeonGreen else SurfaceSlate, RoundedCornerShape(8.dp))
                                            .border(1.dp, if (activeNet == net) NeonGreen else Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                            .clickable { activeNet = net }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(net, color = if (activeNet == net) Color.Black else Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            FuturisticInput(appId, { appId = it }, "App/Game ID", Icons.Default.VpnKey)
                            FuturisticInput(rewardedId, { rewardedId = it }, "Rewarded Placement ID", Icons.Default.PlayCircle)
                            FuturisticInput(interstitialId, { interstitialId = it }, "Interstitial Placement ID", Icons.Default.Fullscreen)
                            FuturisticInput(bannerId, { bannerId = it }, "Banner Placement ID", Icons.Default.VideoLabel)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("GLOBAL ADS ENABLED", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                Switch(checked = isAdsEnabled, onCheckedChange = { isAdsEnabled = it }, colors = SwitchDefaults.colors(checkedThumbColor = NeonGreen))
                            }

                            Button(
                                onClick = {
                                    viewModel.updateAdminSettings(adminSettings.copy(
                                        ads = UniversalAdSettings(
                                            activeNetwork = activeNet,
                                            primaryAppId = appId,
                                            rewardedPlacementId = rewardedId,
                                            interstitialPlacementId = interstitialId,
                                            bannerPlacementId = bannerId,
                                            isAdsEnabled = isAdsEnabled
                                        )
                                    ))
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("APPLY GLOBALLY", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnalyticsCard(title: String, value: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier, isGlow: Boolean = false) {
    Surface(
        modifier = modifier.then(
            if (isGlow) Modifier.border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            else Modifier.border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
        ),
        color = SurfaceSlate,
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, color = color, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
            Text(title, color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
        }
    }
}

@Composable
fun UserExecutiveCard(user: Map<String, Any>, onClose: () -> Unit, viewModel: FirebaseViewModel) {
    var adjustAmt by remember { mutableStateOf("") }
    val uid = user["id"].toString()
    val balance = (user["wallet_balance"] as? Double) ?: 0.0

    MatrixPanel {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("METADATA PROFILE", color = GoldAmber, fontWeight = FontWeight.Black, fontSize = 10.sp)
                Icon(Icons.Default.Close, null, tint = Color.Gray, modifier = Modifier.size(16.dp).clickable { onClose() })
            }
            
            Column {
                Text(user["name"].toString().uppercase(), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                Text(uid, color = Color.Gray, fontSize = 9.sp)
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("LIQUID BALANCE", color = Color.Gray, fontSize = 9.sp)
                    Text("₹${String.format("%.2f", balance)}", color = NeonGreen, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("STATUS", color = Color.Gray, fontSize = 9.sp)
                    val isBanned = user["is_banned"] as? Boolean ?: false
                    Text(if (isBanned) "PERM_BANNED" else "ACTIVE", color = if (isBanned) ElectricCrimson else NeonGreen, fontSize = 12.sp, fontWeight = FontWeight.Black)
                }
            }

            HorizontalDivider()

            OutlinedTextField(
                value = adjustAmt,
                onValueChange = { adjustAmt = it },
                label = { Text("Adjust Balance (₹)", color = Color.Gray, fontSize = 12.sp) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonGreen, focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { viewModel.adjustUserWallet(uid, adjustAmt.toDoubleOrNull() ?: 0.0, true) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                    shape = RoundedCornerShape(8.dp)
                ) { Text("CREDIT", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                Button(
                    onClick = { viewModel.adjustUserWallet(uid, adjustAmt.toDoubleOrNull() ?: 0.0, false) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = DeepSlate),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ElectricCrimson)
                ) { Text("DEBIT", color = ElectricCrimson, fontWeight = FontWeight.Bold, fontSize = 11.sp) }
            }

            HorizontalDivider()

            Text("PENALTY ENGINE", color = ElectricCrimson, fontWeight = FontWeight.Black, fontSize = 10.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { viewModel.suspendUserTemp(uid) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceSlate),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray),
                    shape = RoundedCornerShape(8.dp)
                ) { Text("24H LOCK", fontSize = 10.sp) }
                Button(
                    onClick = { viewModel.banUserPermanent(uid) },
                    modifier = Modifier.weight(1.5f),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCrimson),
                    shape = RoundedCornerShape(8.dp)
                ) { Text("BAN & SEIZE", fontSize = 10.sp, fontWeight = FontWeight.Black) }
            }
        }
    }
}

@Composable
fun PayoutExecutiveRow(req: Map<String, Any>, viewModel: FirebaseViewModel) {
    val amount = (req["amount"] as? Double) ?: 0.0
    val uid = (req["userId"] as? String) ?: ""
    val reqId = (req["id"] as? String) ?: ""
    val upiId = (req["upiId"] as? String) ?: ""

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SurfaceSlate,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(uid.take(16) + "...", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("UPI: $upiId", color = GoldAmber, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                }
                Text("₹$amount", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { viewModel.manageWithdrawal(reqId, uid, "APPROVED", amount) },
                    modifier = Modifier.weight(1f).height(38.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("APPROVE", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 11.sp) }
                OutlinedButton(
                    onClick = { viewModel.manageWithdrawal(reqId, uid, "REJECTED", amount) },
                    modifier = Modifier.weight(1f).height(38.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ElectricCrimson),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ElectricCrimson),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("REJECT", fontWeight = FontWeight.Black, fontSize = 11.sp) }
            }
        }
    }
}

@Composable
fun MatrixPanel(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SurfaceSlate,
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Box(modifier = Modifier.padding(20.dp)) { content() }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(title, color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp, modifier = Modifier.padding(start = 4.dp, bottom = 4.dp))
}

@Composable
fun HorizontalDivider() {
    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.05f)))
}

@Composable
fun FuturisticInput(value: String, onValueChange: (String) -> Unit, label: String, icon: ImageVector) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 4.dp, bottom = 4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = { Icon(icon, null, tint = Color.Gray, modifier = Modifier.size(16.dp)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = DeepSlate,
                unfocusedContainerColor = DeepSlate,
                focusedBorderColor = NeonGreen,
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
    }
}

@Composable
fun SearchResultItem(user: Map<String, Any>, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        color = SurfaceSlate,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(36.dp).background(DeepSlate, CircleShape), contentAlignment = Alignment.Center) {
                Text(user["name"].toString().take(1).uppercase(), color = NeonGreen, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(user["name"].toString(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(user["id"].toString().take(12) + "...", color = Color.Gray, fontSize = 9.sp)
            }
            Spacer(modifier = Modifier.weight(1f))
            Text("₹${String.format("%.2f", (user["wallet_balance"] as? Double) ?: 0.0)}", color = NeonGreen, fontWeight = FontWeight.Bold)
        }
    }
}
