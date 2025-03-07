package com.example.saahas.ui.Screens.Location


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.saahas.Models.Room.ContactDao

class LocationViewModelFactory(
    private val contactDao: ContactDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LocationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LocationViewModel(contactDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}