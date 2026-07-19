package com.directcash.app.data.model

import java.util.Date

enum class TransactionType {
    CREDIT, DEBIT
}

enum class TransactionStatus {
    SUCCESS, PENDING, FAILED
}

data class Transaction(
    val id: String,
    val amount: Double,
    val type: TransactionType,
    val status: TransactionStatus,
    val timestamp: Long = System.currentTimeMillis(),
    val description: String
)
