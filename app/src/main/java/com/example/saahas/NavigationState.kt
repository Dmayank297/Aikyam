package com.example.saahas

import android.util.Log
import androidx.compose.runtime.Stable
import androidx.navigation.NavHostController

@Stable
class NavigationState(val navController: NavHostController) {
    fun popUp() {
        navController.popBackStack()
    }

    fun navigate(route: String) {
        navController.navigate(route) { launchSingleTop = true }
    }

    fun navigateAndPopUp(route: String, popUp: String) {
        Log.d("SaahasNav", "Navigating to $route, popping up to $popUp")
        Log.d("SaahasNav", "Current destination: ${navController.currentDestination?.route}")
        navController.navigate(route) {
            launchSingleTop = true
            popUpTo(popUp) { inclusive = true }
        }
        Log.d("SaahasNav", "Navigation executed")
    }

    fun clearAndNavigate(route: String) {
        navController.navigate(route) {
            launchSingleTop = true
            popUpTo(0) { inclusive = true }
        }
    }
}
