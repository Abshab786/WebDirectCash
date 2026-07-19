package com.directcash.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.directcash.app.ui.components.TaskItemComponent
import com.directcash.app.ui.theme.PrimaryEmerald
import com.directcash.app.ui.theme.SecondaryEmerald
import com.directcash.app.ui.theme.VibrantGreen
import com.directcash.app.ui.viewmodel.FirebaseViewModel

@Composable
fun DashboardScreen(
    firebaseViewModel: FirebaseViewModel,
    onTaskClick: (Long) -> Unit,
    onSpinClick: () -> Unit,
    onOfferwallClick: () -> Unit,
    onWithdrawClick: () -> Unit,
    onReferClick: () -> Unit,
    onDailyCheckInClick: () -> Unit
) {
    val name by firebaseViewModel.userName.collectAsState()
    val balance by firebaseViewModel.balance.collectAsState()
    val totalEarnings by firebaseViewModel.totalEarnings.collectAsState()
    val transactions by firebaseViewModel.transactions.collectAsState()
    val tasks by firebaseViewModel.tasks.collectAsState()
    val loadingTasks by firebaseViewModel.loadingTasks.collectAsState()
    val taskError by firebaseViewModel.taskError.collectAsState()
    val dailyBonusStatus by firebaseViewModel.dailyBonusStatus.collectAsState()
    val referralEarnings by firebaseViewModel.referralEarnings.collectAsState()
    val todayEarnings by firebaseViewModel.todayEarnings.collectAsState()

    val context = androidx.compose.ui.platform.LocalContext.current

    // Derived values for summary
    val tasksCompletedCount = transactions.filter { it.type == com.directcash.app.data.model.TransactionType.CREDIT }.size
    val totalWithdrawn = transactions.filter { it.type == com.directcash.app.data.model.TransactionType.DEBIT && it.status == com.directcash.app.data.model.TransactionStatus.SUCCESS }.sumOf { it.amount }

    LaunchedEffect(dailyBonusStatus) {
        dailyBonusStatus?.let { status ->
            when (status) {
                "SUCCESS" -> android.widget.Toast.makeText(context, "₹2.00 Daily Bonus Claimed!", android.widget.Toast.LENGTH_SHORT).show()
                "ALREADY_CLAIMED" -> android.widget.Toast.makeText(context, "You have already claimed your reward today!", android.widget.Toast.LENGTH_SHORT).show()
                "ERROR" -> android.widget.Toast.makeText(context, "Error claiming bonus. Please try again.", android.widget.Toast.LENGTH_SHORT).show()
            }
            firebaseViewModel.resetDailyBonusStatus()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF001A0F)), // Dark Emerald background for the top part
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // 1. Dynamic Header
        item {
            HeaderSection(name = name)
        }

        // 2. Premium Balance Card
        item {
            PremiumBalanceCard(
                balance = balance,
                todayEarning = todayEarnings,
                totalEarned = totalEarnings,
                onWithdrawClick = onWithdrawClick
            )
        }

        // White background section starts here
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .background(Color.White)
                    .padding(top = 24.dp)
            ) {
                // 3. Achievements Banner
                AchievementsBanner()
                
                Spacer(modifier = Modifier.height(24.dp))

                // 4. 2x2 Grid Categories
                CategoriesGrid(
                    onDailyCheckIn = onDailyCheckInClick,
                    onSpinClick = onSpinClick,
                    onOfferwallClick = onOfferwallClick,
                    onReferClick = onReferClick
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 5. CPA Offers Banner
                CpaOffersBanner(onClick = onOfferwallClick)

                Spacer(modifier = Modifier.height(24.dp))

                // 6. Top Tasks List Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Top Tasks for You",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = "View All",
                        style = MaterialTheme.typography.labelMedium,
                        color = VibrantGreen,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { /* View All */ }
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // 6. Tasks List items
        if (loadingTasks) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp).background(Color.White), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = VibrantGreen)
                }
            }
        } else if (taskError != null) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(32.dp).background(Color.White),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = taskError!!, color = Color.Red, textAlign = TextAlign.Center)
                    TextButton(onClick = { firebaseViewModel.fetchCpaGripTasks() }) {
                        Text("Retry", color = VibrantGreen, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            items(tasks) { task ->
                Box(modifier = Modifier.background(Color.White)) {
                    TaskItemComponent(
                        task = task,
                        onStartClick = { 
                            if (task.url.isNotEmpty()) {
                                try {
                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(task.url))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    android.widget.Toast.makeText(context, "Cannot open this link.", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                            firebaseViewModel.completeTask(task.id, task.reward.toDouble(), task.title)
                        }
                    )
                }
            }
        }

        // 7. Bottom Summary Row
        item {
            Box(modifier = Modifier.background(Color.White).padding(bottom = 16.dp)) {
                BottomSummaryRow(
                    todayEarning = todayEarnings,
                    tasksCompleted = tasksCompletedCount,
                    referralEarnings = referralEarnings,
                    totalWithdrawn = totalWithdrawn
                )
            }
        }
    }
}

@Composable
fun HeaderSection(name: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Hi, ${name.ifEmpty { "User" }} 👋",
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Welcome to DirectCash",
                color = VibrantGreen,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = "Notifications",
            tint = Color.White,
            modifier = Modifier
                .size(32.dp)
                .clickable { /* Notifications */ }
        )
    }
}

@Composable
fun PremiumBalanceCard(
    balance: Double,
    todayEarning: Double,
    totalEarned: Double,
    onWithdrawClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF00C853), Color(0xFF004D40))
                    )
                )
                .padding(24.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Available Balance",
                                color = Color.White.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.Visibility, contentDescription = null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(14.dp))
                        }
                        Text(
                            text = "₹${"%.2f".format(balance)}",
                            color = Color.White,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 32.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Wallet Balance: ₹${"%.2f".format(balance)}",
                            color = Color.White.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    
                    Column(horizontalAlignment = Alignment.End) {
                        StatItem(label = "Today's Earning", value = "₹${"%.2f".format(todayEarning)}", icon = Icons.Rounded.TrendingUp)
                        Spacer(modifier = Modifier.height(12.dp))
                        StatItem(label = "Total Earned", value = "₹${"%.2f".format(totalEarned)}", icon = Icons.Rounded.AccountBalanceWallet)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onWithdrawClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = PrimaryEmerald, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Withdraw to UPI", color = PrimaryEmerald, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = PrimaryEmerald, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = VibrantGreen, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = label, color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall)
            Text(text = value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

@Composable
fun AchievementsBanner() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF191C19)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFF2C2F2C), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Diamond, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Achieve more, earn more!", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("Complete tasks and unlock exclusive rewards.", color = Color.Gray, fontSize = 11.sp)
            }
            OutlinedButton(
                onClick = { /* View Achievements */ },
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text("View Achievements", color = Color.White, fontSize = 10.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
            }
        }
    }
}

@Composable
fun CategoriesGrid(
    onDailyCheckIn: () -> Unit,
    onSpinClick: () -> Unit,
    onOfferwallClick: () -> Unit,
    onReferClick: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CategoryTile(
                title = "Daily Check-in",
                subtitle = "Earn bonus daily",
                buttonText = "Check-in",
                color = Color(0xFFE8F5E9),
                contentColor = Color(0xFF2E7D32),
                icon = Icons.Rounded.CalendarToday,
                onClick = onDailyCheckIn,
                modifier = Modifier.weight(1f)
            )
            CategoryTile(
                title = "Spin & Win",
                subtitle = "Win exciting rewards",
                buttonText = "Spin Now",
                color = Color(0xFFF3E5F5),
                contentColor = Color(0xFF7B1FA2),
                icon = Icons.Rounded.ControlPoint, // Circular icon
                onClick = onSpinClick,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CategoryTile(
                title = "Premium Offers",
                subtitle = "High paying offers",
                buttonText = "Explore",
                color = Color(0xFFFFF3E0),
                contentColor = Color(0xFFEF6C00),
                icon = Icons.Rounded.CardGiftcard,
                onClick = onOfferwallClick,
                modifier = Modifier.weight(1f)
            )
            CategoryTile(
                title = "Refer & Earn",
                subtitle = "Invite & earn more",
                buttonText = "Invite",
                color = Color(0xFFE3F2FD),
                contentColor = Color(0xFF1565C0),
                icon = Icons.Rounded.Group,
                onClick = onReferClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun CategoryTile(
    title: String,
    subtitle: String,
    buttonText: String,
    color: Color,
    contentColor: Color,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Black)
            Text(subtitle, fontSize = 10.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = contentColor),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.height(30.dp).fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(buttonText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, modifier = Modifier.size(12.dp))
                }
            }
        }
    }
}

@Composable
fun CpaOffersBanner(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF002E1A))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF004D40), CircleShape)
                    .border(1.dp, VibrantGreen, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Loop, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Premium CPA Offers", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text("High paying offers from CPALead Network", color = Color.Gray, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Earn up to ₹250+", color = VibrantGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004D40)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text("Explore Offers", fontSize = 10.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, modifier = Modifier.size(12.dp))
            }
        }
    }
}

@Composable
fun BottomSummaryRow(
    todayEarning: Double,
    tasksCompleted: Int,
    referralEarnings: Double,
    totalWithdrawn: Double
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF002E1A))
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SummaryItem(label = "Today's Earnings", value = "₹${"%.2f".format(todayEarning)}", icon = Icons.Rounded.AccountBalanceWallet)
            VerticalDivider()
            SummaryItem(label = "Tasks Completed", value = "$tasksCompleted", icon = Icons.Rounded.List)
            VerticalDivider()
            SummaryItem(label = "Referral Earnings", value = "₹${"%.2f".format(referralEarnings)}", icon = Icons.Rounded.Group)
            VerticalDivider()
            SummaryItem(label = "Total Withdrawn", value = "₹${"%.2f".format(totalWithdrawn)}", icon = Icons.Rounded.Payments)
        }
    }
}

@Composable
fun SummaryItem(label: String, value: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = VibrantGreen, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text(text = label, color = Color.Gray, fontSize = 9.sp, textAlign = TextAlign.Center)
    }
}

@Composable
fun VerticalDivider() {
    Box(modifier = Modifier.width(1.dp).height(40.dp).background(Color.Gray.copy(alpha = 0.2f)))
}
