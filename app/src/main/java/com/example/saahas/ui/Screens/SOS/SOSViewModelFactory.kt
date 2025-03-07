package com.example.saahas.ui.Screens.SOS

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.saahas.Models.Room.ContactDao
import com.example.saahas.PermissionManager
import com.example.saahas.ui.Authentication.AuthViewModel
import com.example.saahas.ui.Screens.SOSViewModel

class SOSViewModelFactory(
    private val context: Context,
    private val permissionManager: PermissionManager,
    private val authViewModel: AuthViewModel,
    private val contactDao: ContactDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SOSViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SOSViewModel(
                context, permissionManager, authViewModel,
                contactDao = contactDao
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}