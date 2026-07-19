package com.directcash.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.directcash.app.data.model.Task
import com.directcash.app.ui.theme.BorderGray
import com.directcash.app.ui.theme.VibrantGreen

@Composable
fun TaskItemComponent(
    task: Task,
    onStartClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .border(1.dp, BorderGray, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(Color(0xFFF0FDF4), CircleShape), // Very light green tint
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = task.icon,
                    contentDescription = null,
                    tint = VibrantGreen,
                    modifier = Modifier.size(26.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF757575)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Reward: ₹${"%.2f".format(task.reward.toDouble())}",
                    style = MaterialTheme.typography.labelLarge,
                    color = VibrantGreen,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Button(
                onClick = onStartClick,
                shape = RoundedCornerShape(50), // Capsule button
                colors = ButtonDefaults.buttonColors(
                    containerColor = VibrantGreen,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 0.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text(
                    text = "Start", 
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
