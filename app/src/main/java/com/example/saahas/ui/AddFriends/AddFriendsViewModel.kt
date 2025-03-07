package com.example.saahas.ui.Screens

import android.content.Context
import android.content.Intent
import android.provider.ContactsContract
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saahas.Models.Contact
import com.example.saahas.PermissionManager
import kotlinx.coroutines.launch
import com.example.saahas.Models.Room.ContactDao

class AddFriendsViewModel(
    private val context: Context,
    private val permissionManager: PermissionManager,
    private val contactDao: ContactDao
) : ViewModel() {

    val contactList = mutableStateListOf<Contact>()
    private var onContactPicked: ((Intent) -> Unit)? = null

    init {
        loadContactsFromDatabase()
    }

    private fun loadContactsFromDatabase() {
        viewModelScope.launch {
            contactList.clear()
            contactList.addAll(contactDao.getAllContacts())
        }
    }

    fun onAddContactClicked(onPickContact: (Intent) -> Unit) {
        if (permissionManager.isPermissionGranted(android.Manifest.permission.READ_CONTACTS)) {
            onContactPicked = onPickContact
            onPickContact(Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI))
        } else {
            permissionManager.checkAndRequestPermission(android.Manifest.permission.READ_CONTACTS)
        }
    }

    fun handleContactPicked(data: Intent?) {
        data?.data?.let { uri ->
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    val phoneIndex = it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    val name = it.getString(nameIndex)
                    val phone = it.getString(phoneIndex).replace("\\s".toRegex(), "")
                    val contact = Contact(name, phone)
                    saveContact(contact)
                }
            }
        }
    }

    private fun saveContact(contact: Contact) {
        viewModelScope.launch {
            contactDao.insertContact(contact)
            contactList.add(contact)
        }
    }

    fun deleteContact(contact: Contact) {
        viewModelScope.launch {
            contactDao.deleteContact(contact)
            contactList.remove(contact)
        }
    }

    fun updateContact(oldContact: Contact, newName: String) {
        viewModelScope.launch {
            val updatedContact = oldContact.copy(name = newName)
            contactDao.updateContact(updatedContact)
            contactList.remove(oldContact)
            contactList.add(updatedContact)
        }
    }

    fun onBackClicked(popUpScreen: () -> Unit) {
        popUpScreen()
    }
}

