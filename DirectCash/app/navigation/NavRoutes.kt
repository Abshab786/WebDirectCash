package com.directcash.app.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface NavRoute : NavKey {
    @Serializable
    data object Splash : NavRoute
    
    @Serializable
    data object Login : NavRoute
    
    @Serializable
    data object Dashboard : NavRoute
}
