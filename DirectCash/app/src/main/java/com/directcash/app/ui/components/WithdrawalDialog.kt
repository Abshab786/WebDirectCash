package com.directcash.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.directcash.app.ui.theme.PrimaryEmerald
import com.directcash.app.ui.theme.VibrantGreen

@Composable
fun WithdrawalDialog(
    currentBalance: Double,
    minLimit: Double = 100.0,
    onDismiss: () -> Unit,
    onConfirm: (Double, String) -> Unit,
    isValidUpi: (String) -> Boolean
) {
    var upiId by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    val amounts = listOf(100.0, 200.0, 500.0)
    
    val selectedAmount = amountStr.toDoubleOrNull() ?: 0.0
    val isAmountValid = selectedAmount >= minLimit && selectedAmount <= currentBalance
    val isUpiValid = isValidUpi(upiId)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Withdraw Funds",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // UPI ID Input
                OutlinedTextField(
                    value = upiId,
                    onValueChange = { upiId = it },
                    label = { Text("Enter UPI ID (e.g. user@bank)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    isError = upiId.isNotEmpty() && !isUpiValid,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VibrantGreen,
                        unfocusedBorderColor = Color.LightGray
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Amount Selection Chips
                Text(
                    text = "Select Amount",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.align(Alignment.Start),
                    color = Color.Gray
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    amounts.forEach { amount ->
                        FilterChip(
                            selected = amountStr == amount.toString(),
                            onClick = { amountStr = amount.toString() },
                            label = { Text("₹${amount.toInt()}") },
                            shape = RoundedCornerShape(8.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFE8F5E9),
                                selectedLabelColor = VibrantGreen
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Custom Amount Input
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Custom Amount") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    prefix = { Text("₹") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = amountStr.isNotEmpty() && !isAmountValid,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VibrantGreen,
                        unfocusedBorderColor = Color.LightGray
                    )
                )

                if (amountStr.isNotEmpty() && isAmountValid) {
                    // Dynamic Commission Display logic can be added here
                    Text(
                        text = "Note: A small processing fee will be deducted.",
                        color = Color.Gray,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.align(Alignment.Start).padding(top = 4.dp)
                    )
                }
                
                if (amountStr.isNotEmpty()) {
                    if (selectedAmount < minLimit) {
                        Text(
                            text = "Minimum withdrawal is ₹${minLimit.toInt()}",
                            color = Color.Red,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.align(Alignment.Start).padding(top = 4.dp)
                        )
                    } else if (selectedAmount > currentBalance) {
                        Text(
                            text = "Insufficient balance!",
                            color = Color.Red,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.align(Alignment.Start).padding(top = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Confirm Button
                Button(
                    onClick = { onConfirm(selectedAmount, upiId) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = isAmountValid && isUpiValid,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VibrantGreen,
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Confirm Withdrawal",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Withdrawals are processed within 24 hours",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}
