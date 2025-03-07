package com.example.saahas.Accessibility.Services

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.example.saahas.Models.Room.VoiceRecordingDatabase
import com.example.saahas.ui.Screens.Button.Volume.VolumeButtonViewModel
import com.example.saahas.ui.Screens.Button.Volume.VolumeButtonViewModelFactory
import com.example.saahas.ui.Screens.Location.LocationViewModel

class VolumeButtonService : AccessibilityService() {

    private lateinit var viewModel: VolumeButtonViewModel

    private var buttonPressStartTime: Long = 0L
    private val holdThreshold = 2500L // 2.5 seconds for hold detection

    override fun onServiceConnected() {
        super.onServiceConnected()
        val locationViewModel = LocationViewModel(VoiceRecordingDatabase.getDatabase(applicationContext).ContactDao())
        viewModel = VolumeButtonViewModelFactory(applicationContext, locationViewModel).create(VolumeButtonViewModel::class.java)
        Log.d("VolumeButtonService", "Accessibility Service Connected")
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        when (event.keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> handleVolumeUp(event)
            KeyEvent.KEYCODE_VOLUME_DOWN -> handleVolumeDown(event)
        }
        return super.onKeyEvent(event)
    }

    private fun handleVolumeUp(event: KeyEvent) {
        when (event.action) {
            KeyEvent.ACTION_DOWN -> {
                buttonPressStartTime = System.currentTimeMillis()
                Log.d("VolumeButtonService", "Volume Up Button Pressed")
            }
            KeyEvent.ACTION_UP -> {
                val pressDuration = System.currentTimeMillis() - buttonPressStartTime
                if (pressDuration < holdThreshold) {
                    Log.d("VolumeButtonService", "Volume Up Quick Press detected")
                    viewModel.updatePressCount(applicationContext)
                } else {
                    Log.d("VolumeButtonService", "Volume Up Hold detected: Sending SOS Notification")
                    viewModel.updatePressCount(applicationContext)
                }
            }
        }
    }

    private fun handleVolumeDown(event: KeyEvent) {
        when (event.action) {
            KeyEvent.ACTION_DOWN -> {
                buttonPressStartTime = System.currentTimeMillis()
                Log.d("VolumeButtonService", "Volume Down Button Pressed")
            }
            KeyEvent.ACTION_UP -> {
                val pressDuration = System.currentTimeMillis() - buttonPressStartTime
                if (pressDuration < holdThreshold) {
                    Log.d("VolumeButtonService", "Volume Down Quick Press detected")
                    viewModel.updateVolumeDownPressCount(applicationContext)
                } else {
                    Log.d("VolumeButtonService", "Volume Down Hold detected")
                    viewModel.updateButtonHoldDuration(buttonPressStartTime, System.currentTimeMillis(), applicationContext)
                }
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Not required for button actions
    }

    override fun onInterrupt() {
        Log.d("VolumeButtonService", "Accessibility Service Interrupted")
    }
}