package com.example.saahas.ui.Screens.Button.AudioRecord


import android.app.Application
import androidx.lifecycle.ViewModelProvider

class AudioRecordViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AudioRecordViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AudioRecordViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}