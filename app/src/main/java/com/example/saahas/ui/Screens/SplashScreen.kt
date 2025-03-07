package com.example.saahas.ui.Screens

import android.app.Application
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.saahas.ONBOARDING
import com.example.saahas.R
import com.example.saahas.SIGN_IN
import com.example.saahas.SOS_SCREEN
import com.example.saahas.SPLASH_SCREEN
import com.example.saahas.ui.Authentication.AuthViewModel
import com.example.saahas.ui.Authentication.AuthViewModelFactory
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(openAndPopUp: (String, String) -> Unit) {
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(context.applicationContext as android.app.Application))
    val sharedPreferences = remember { context.getSharedPreferences("app_prefs", Application.MODE_PRIVATE) }

    LaunchedEffect(true) {
        delay(1000)

        val hasSeenOnboarding = sharedPreferences.getBoolean("has_seen_onboarding", false)
        val token = authViewModel.getToken()

        if (token != null) {
            // If token exists, go directly to SOS Screen
            openAndPopUp(SOS_SCREEN, SPLASH_SCREEN)
        } else if (!hasSeenOnboarding) {
            // If onboarding hasn't been seen, show it and mark it as seen
            sharedPreferences.edit().putBoolean("has_seen_onboarding", true).apply()
            openAndPopUp(ONBOARDING, SPLASH_SCREEN)
        } else {
            // If onboarding has been seen, go to Sign In
            openAndPopUp(SIGN_IN, SPLASH_SCREEN)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                modifier = Modifier.fillMaxSize(),
                painter = painterResource(id = R.drawable.splashimg),
                contentDescription = "App Logo",
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
