package com.example.saahas.ui.AddFriends

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.saahas.Models.Room.ContactDao
import com.example.saahas.PermissionManager
import com.example.saahas.ui.Screens.AddFriendsViewModel


class AddFriendsViewModelFactory(
    private val context: Context,
    private val permissionManager: PermissionManager,
    private val contactDao: ContactDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddFriendsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddFriendsViewModel(context, permissionManager, contactDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}