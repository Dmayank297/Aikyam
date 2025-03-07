package com.example.saahas.Accessibility.Services

import SensorViewModel
import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.saahas.Models.Room.ContactDao
import com.example.saahas.PermissionManager

class SensorViewModelFactory(
    private val application: Application,
    private val context: Context,
    private val permissionManager: PermissionManager,
    private val contactDao: ContactDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SensorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SensorViewModel(
                application,
                context,
                permissionManager,
                contactDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
