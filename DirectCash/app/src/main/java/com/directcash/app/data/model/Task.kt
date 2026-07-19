package com.directcash.app.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdsClick
import androidx.compose.ui.graphics.vector.ImageVector

data class Task(
    val id: Long = 0,
    val title: String = "",
    val reward: Long = 0,
    val description: String = "Complete this task to earn rewards",
    val icon: ImageVector = Icons.Default.AdsClick,
    val url: String = ""
)
