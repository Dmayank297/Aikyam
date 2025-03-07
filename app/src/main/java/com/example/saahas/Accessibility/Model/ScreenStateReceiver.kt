package com.example.saahas.Accessibility.Model

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class ScreenStateReceiver : BroadcastReceiver() {
    companion object {
        var isScreenOn = true // Track screen state statically for simplicity
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("ScreenStateReceiver", "Intent action: ${intent?.action}")

        when (intent?.action) {
            Intent.ACTION_SCREEN_OFF -> {
                isScreenOn = false
                val currentTime = System.currentTimeMillis()
                Log.d("ScreenStateReceiver", "Screen OFF detected. Timestamp: $currentTime")
                // Notify service if needed (we’ll use this in Step 4)
            }
            Intent.ACTION_SCREEN_ON -> {
                isScreenOn = true
                Log.d("ScreenStateReceiver", "Screen ON detected.")
            }
            Intent.ACTION_USER_PRESENT -> {
                Log.d("ScreenStateReceiver", "User unlocked the device.")
            }
        }
    }
}