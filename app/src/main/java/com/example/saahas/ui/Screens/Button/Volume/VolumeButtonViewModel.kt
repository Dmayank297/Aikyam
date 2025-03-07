package com.example.saahas.ui.Screens.Button.Volume

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saahas.Accessibility.Actions.HoldAction
import com.example.saahas.Accessibility.Actions.PressAction
import com.example.saahas.Models.Room.VoiceRecordingDatabase
import com.example.saahas.Voice.Helper.VoiceIntegrationFile
import com.example.saahas.Voice.Service.BuzzerService
import com.example.saahas.ui.Screens.Location.LocationViewModel
import kotlinx.coroutines.launch

class VolumeButtonViewModel(
    private val context: Context,
    private val locationViewModel: LocationViewModel
) : ViewModel() {

    private val _volumeUpButtonPressCount = MutableLiveData(0)
    val volumeUpButtonPressCount: LiveData<Int> get() = _volumeUpButtonPressCount

    private val _volumeDownButtonPressCount = MutableLiveData(0)
    val volumeDownButtonPressCount: LiveData<Int> get() = _volumeDownButtonPressCount

    private val _volumeDownButtonHoldDuration = MutableLiveData<Long>()
    val buttonHoldDuration: LiveData<Long> get() = _volumeDownButtonHoldDuration

    private var lastVolumeUpPressTime: Long = 0L
    private var lastVolumeDownPressTime: Long = 0L
    private var lastScreenOffTime: Long = 0L
    private val pressTimeout = 3000L // 3 seconds for multiple presses
    private val longPressThreshold = 2500L // 2.5 seconds for hold
    private var isHoldTriggered = false

    private val voiceIntegration = VoiceIntegrationFile(
        context,
        BuzzerService(),
        locationViewModel
    )

    private suspend fun getFirstContactPhoneNumber(): String? {
        val contactDao = VoiceRecordingDatabase.getDatabase(context).ContactDao()
        return contactDao.getAllContacts().firstOrNull()?.phoneNumber
    }

    private fun performPressAction(context: Context) {
        viewModelScope.launch {
            val phoneNumber = getFirstContactPhoneNumber()
            if (phoneNumber != null) {
                PressAction.sendSOSNotification(context, phoneNumber)
                Log.d("VolumeButton", "Press action triggered: SOS notification sent to $phoneNumber")
            } else {
                Log.w("VolumeButton", "No contacts available for SOS")
            }
            resetPressCount()
        }
    }

    private fun performHoldAction(context: Context) {
        viewModelScope.launch {
            val phoneNumber = getFirstContactPhoneNumber()
            if (phoneNumber != null) {
                HoldAction.makeCall(context, phoneNumber)
                Log.d("VolumeButton", "Hold action triggered: Calling $phoneNumber")
            } else {
                Log.w("VolumeButton", "No contacts available for call")
            }
            resetHoldState()
        }
    }

    private fun performVoiceAction() {
        Log.d("VolumeButton", "Starting voice system")
        voiceIntegration.startListening()
    }

    fun updatePressCount(context: Context) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastVolumeUpPressTime > pressTimeout) {
            _volumeUpButtonPressCount.value = 0
            Log.d("VolumeButton", "Volume Up press timeout exceeded. Resetting count.")
        }
        _volumeUpButtonPressCount.value = (_volumeUpButtonPressCount.value ?: 0) + 1
        lastVolumeUpPressTime = currentTime
        Log.d("VolumeButton", "Volume Up Press Count Updated: ${_volumeUpButtonPressCount.value}")

        if (_volumeUpButtonPressCount.value == 2) {
            performPressAction(context)
        }
    }

    fun updateVolumeDownPressCount(context: Context) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastVolumeDownPressTime > pressTimeout) {
            _volumeDownButtonPressCount.value = 0
            Log.d("VolumeButton", "Volume Down press timeout exceeded. Resetting count.")
        }
        _volumeDownButtonPressCount.value = (_volumeDownButtonPressCount.value ?: 0) + 1
        lastVolumeDownPressTime = currentTime
        Log.d("VolumeButton", "Volume Down Press Count Updated: ${_volumeDownButtonPressCount.value}")

        if (_volumeDownButtonPressCount.value == 2) {
            performVoiceAction()
        }
    }

    fun updateButtonHoldDuration(holdStartTime: Long, currentTime: Long, context: Context) {
        val holdDuration = currentTime - holdStartTime
        if (holdDuration >= longPressThreshold && !isHoldTriggered) {
            _volumeDownButtonHoldDuration.value = holdDuration
            isHoldTriggered = true
            performHoldAction(context)
            Log.d("VolumeButton", "Hold Action Triggered after $holdDuration ms")
        }
    }

    fun checkAndTriggerActions(context: Context) {
        if (_volumeUpButtonPressCount.value == 2 && !isHoldTriggered) {
            performPressAction(context)
        } else if (isHoldTriggered) {
            performHoldAction(context)
        } else if (_volumeDownButtonPressCount.value == 2) {
            performVoiceAction()
        }
    }

    private fun resetPressCount() {
        _volumeUpButtonPressCount.value = 0
        Log.d("VolumeButton", "Volume Up press count reset.")
    }

    private fun resetVolumeDownPressCount() {
        _volumeDownButtonPressCount.value = 0
        Log.d("VolumeButton", "Volume Down press count reset.")
    }

    private fun resetHoldState() {
        isHoldTriggered = false
        _volumeDownButtonHoldDuration.value = 0
        Log.d("VolumeButton", "Hold state reset.")
    }

    fun saveScreenOffTime(timestamp: Long) {
        lastScreenOffTime = timestamp
        Log.d("VolumeButton", "Screen OFF time saved: $timestamp")
    }

    fun getLastScreenOffTime(): Long = lastScreenOffTime
    fun getHoldThreshold(): Long = longPressThreshold

    override fun onCleared() {
        super.onCleared()
        voiceIntegration.cleanup()
    }
}