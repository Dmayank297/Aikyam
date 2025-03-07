package com.example.saahas

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.compose.AppTheme

class MainActivity : ComponentActivity() {
    private lateinit var permissionManager: PermissionManager

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize PermissionManager early in onCreate before setContent
        permissionManager = PermissionManager(this) { allGranted ->
            // Optional: Handle permission result globally if needed
            if (!allGranted) {
                // Log or handle denial; screens will request specific permissions as needed
            }
        }

        setContent {
            AppTheme {
                SaahasApp(permission = permissionManager)

            }
        }
    }
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "voice_channel",
                "Voice Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Channel for voice control service"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}