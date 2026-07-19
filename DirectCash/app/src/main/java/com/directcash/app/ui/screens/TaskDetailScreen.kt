package com.directcash.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.AdsClick
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.directcash.app.data.model.Task
import com.directcash.app.ui.theme.PrimaryEmerald
import com.directcash.app.ui.theme.VibrantGreen
import com.directcash.app.ui.viewmodel.MainViewModel
import com.directcash.app.ui.viewmodel.TaskViewModel
import com.directcash.app.ui.viewmodel.FirebaseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    taskId: Long,
    mainViewModel: MainViewModel,
    taskViewModel: TaskViewModel,
    firebaseViewModel: FirebaseViewModel,
    onBack: () -> Unit,
    onComplete: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val firestoreTasks by taskViewModel.tasks.collectAsState()
    val cpaGripTasks by firebaseViewModel.tasks.collectAsState()
    
    val task = firestoreTasks.find { it.id == taskId } ?: cpaGripTasks.find { it.id == taskId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Task Details", fontWeight = FontWeight.Bold) },
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
        if (task == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Task not found")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(Color(0xFFF0FDF4), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = task.icon,
                        contentDescription = null,
                        tint = VibrantGreen,
                        modifier = Modifier.size(50.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = task.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    color = Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Reward: ₹${"%.2f".format(task.reward.toDouble())}",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.titleMedium,
                        color = VibrantGreen,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
                    shape = RoundedCornerShape(16.dp),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = PrimaryEmerald, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Instructions", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = task.description + ". Complete the required actions and click the button below to claim your reward instantly.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.DarkGray,
                            lineHeight = 22.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        if (task.url.isNotEmpty()) {
                            try {
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(task.url))
                                android.widget.Toast.makeText(context, "Redirecting...", android.widget.Toast.LENGTH_SHORT).show()
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                android.widget.Toast.makeText(context, "Cannot open this link.", android.widget.Toast.LENGTH_SHORT).show()
                            }
                            
                            // User clicked tracking link. For CPAGrip, we don't have a reliable automatic verification 
                            // in-app without a postback. We will credit the reward when they click "Complete".
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)) // Using Green for Start
                ) {
                    Icon(Icons.Default.AdsClick, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Step 1: Open Task Link", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = {
                        // For CPAGrip tasks, we should ideally wait for Postback.
                        // But for Manual/Firestore tasks, we credit them now.
                        firebaseViewModel.completeTask(task.id, task.reward.toDouble(), task.title)
                        android.widget.Toast.makeText(context, "Verification in progress...", android.widget.Toast.LENGTH_SHORT).show()
                        onComplete()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(2.dp, VibrantGreen)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = VibrantGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Step 2: Confirm Completion", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = VibrantGreen)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "By completing, you agree to our terms of service.",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
