package com.directcash.app.ui.viewmodel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdsClick
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Star
import androidx.lifecycle.ViewModel
import com.directcash.app.data.model.Task
import com.directcash.app.data.model.Transaction
import com.directcash.app.data.model.TransactionStatus
import com.directcash.app.data.model.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class MainViewModel : ViewModel() {
    private val _balance = MutableStateFlow(250.0)
    val balance: StateFlow<Double> = _balance.asStateFlow()

    private val _totalEarnings = MutableStateFlow(1250.0)
    val totalEarnings: StateFlow<Double> = _totalEarnings.asStateFlow()

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private val _tasks = MutableStateFlow(
        listOf(
            Task(id = 1L, title = "Watch Ad", description = "Watch a short video to earn", reward = 5L, icon = Icons.Default.AdsClick),
            Task(id = 2L, title = "Rate App", description = "Give us a 5-star rating", reward = 8L, icon = Icons.Default.Star),
            Task(id = 3L, title = "Download App", description = "Try our partner app", reward = 15L, icon = Icons.Default.Download),
            Task(id = 4L, title = "Refer Friend", description = "Invite your friends", reward = 50L, icon = Icons.Default.Download),
            Task(id = 5L, title = "Daily Bonus", description = "Check in daily", reward = 2L, icon = Icons.Default.Star),
            Task(id = 6L, title = "Survey", description = "Fill a quick survey", reward = 12L, icon = Icons.Default.AdsClick)
        )
    )
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    fun onTaskStart(task: Task) {
        // Handle task start logic
        // Simulate earning
        val rewardAmount = task.reward.toDouble()
        val newBalance = _balance.value + rewardAmount
        val newTotalEarnings = _totalEarnings.value + rewardAmount
        _balance.value = newBalance
        _totalEarnings.value = newTotalEarnings
        
        val transaction = Transaction(
            id = UUID.randomUUID().toString(),
            amount = rewardAmount,
            type = TransactionType.CREDIT,
            status = TransactionStatus.SUCCESS,
            description = "Reward for ${task.title}"
        )
        _transactions.value = listOf(transaction) + _transactions.value
    }

    fun onWithdraw(amount: Double, upiId: String) {
        if (_balance.value >= amount && isValidUpi(upiId)) {
            val newBalance = _balance.value - amount
            _balance.value = newBalance
            
            val transaction = Transaction(
                id = UUID.randomUUID().toString(),
                amount = amount,
                type = TransactionType.DEBIT,
                status = TransactionStatus.PENDING,
                description = "Withdrawal to $upiId"
            )
            _transactions.value = listOf(transaction) + _transactions.value
        }
    }

    fun isValidUpi(upiId: String): Boolean {
        return upiId.contains("@") && upiId.length > 3
    }
}
