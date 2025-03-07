package com.example.saahas.ui.Screens.Button.Volume

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.saahas.ui.Screens.Location.LocationViewModel

class VolumeButtonViewModelFactory(
    private val context: Context,
    private val locationViewModel: LocationViewModel
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VolumeButtonViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return VolumeButtonViewModel(context, locationViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}