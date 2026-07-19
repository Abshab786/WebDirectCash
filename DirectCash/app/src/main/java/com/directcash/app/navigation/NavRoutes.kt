package com.directcash.app.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import com.directcash.app.data.model.Task

@Serializable
sealed interface NavRoute : NavKey {
    @Serializable
    data object Splash : NavRoute
    
    @Serializable
    data object Login : NavRoute

    @Serializable
    data class Otp(val phoneNumber: String, val verificationId: String, val referralCode: String = "") : NavRoute
    
    @Serializable
    data object Dashboard : NavRoute

    @Serializable
    data object Spin : NavRoute

    @Serializable
    data class TaskDetail(val taskId: Long) : NavRoute

    @Serializable
    data class Offerwall(val url: String) : NavRoute

    @Serializable
    data object Referral : NavRoute

    @Serializable
    data object DailyCheckIn : NavRoute

    @Serializable
    data object Maintenance : NavRoute

    @Serializable
    data object AdminPanel : NavRoute

    @Serializable
    data object SuperTaskHub : NavRoute

    @Serializable
    data object Captcha : NavRoute

    @Serializable
    data object MathQuiz : NavRoute

    @Serializable
    data object Scratch : NavRoute

    @Serializable
    data object OfferwallSelection : NavRoute
}
