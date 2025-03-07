package com.example.saahas.ui.Screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.telephony.SmsManager
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saahas.ADD_FRIENDS
import com.example.saahas.Models.LocationData
import com.example.saahas.Models.Room.ContactDao
import com.example.saahas.PermissionManager
import com.example.saahas.SIGN_IN
import com.example.saahas.SOS_SCREEN
import com.example.saahas.Utils.Location.LocationUtils
import com.example.saahas.ui.Authentication.AuthViewModel
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch

class SOSViewModel(
    private val context: Context,
    private val permissionManager: PermissionManager,
    private val authViewModel: AuthViewModel,
    private val contactDao: ContactDao
) : ViewModel() {

    var locationText by mutableStateOf("Fetching location...")
        private set
    var showPhoneInputDialog by mutableStateOf(false)
        private set

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private val locationUtils = LocationUtils(context)
    private var lastKnownLatLng: Pair<Double, Double>? = null // Store coordinates

    init {
        checkPermissionsAndFetchLocation()
    }

    private fun checkPermissionsAndFetchLocation() {
        viewModelScope.launch {
            permissionManager.checkAndRequestPermission(
                android.Manifest.permission.INTERNET,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.RECORD_AUDIO,
                android.Manifest.permission.SEND_SMS,
                android.Manifest.permission.CALL_PHONE
            )
            if (locationUtils.hasLocationPermission(context)) {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            val locationData = LocationData(location.latitude, location.longitude)
                            locationText = locationUtils.reverseGeocodeLocation(locationData)
                            lastKnownLatLng = Pair(location.latitude, location.longitude) // Save coords
                        } else {
                            locationText = "Unable to fetch location"
                        }
                    }
                    .addOnFailureListener {
                        locationText = "Failed to get location"
                    }
            } else {
                locationText = "Location permission not granted"
            }
        }
    }

    fun onAddContactClicked(openScreen: (String) -> Unit) {
        openScreen(ADD_FRIENDS)
    }

    fun savePhoneNumber(number: String) {
        viewModelScope.launch {
            val contact = com.example.saahas.Models.Contact("Emergency Contact", number)
            contactDao.insertContact(contact)
            showPhoneInputDialog = false
        }
    }

    fun dismissPhoneDialog() {
        showPhoneInputDialog = false
    }

    fun onSOSButtonClicked() {
        if (permissionManager.isPermissionGranted(android.Manifest.permission.SEND_SMS) &&
            permissionManager.isPermissionGranted(android.Manifest.permission.CALL_PHONE)) {
            viewModelScope.launch {
                val contacts = contactDao.getAllContacts()
                if (contacts.isNotEmpty()) {
                    val smsManager = SmsManager.getDefault()
                    val locationUrl = "https://www.google.com/maps?q=${getLatLng()}"
                    val message = "Emergency! My location: $locationUrl"
                    contacts.forEach { contact ->
                        smsManager.sendTextMessage(contact.phoneNumber, null, message, null, null)
                        Log.d("SaahasSOS", "SMS sent to ${contact.phoneNumber}")
                    }
                    val firstContact = contacts.first()
                    val callIntent = Intent(Intent.ACTION_CALL).apply {
                        data = Uri.parse("tel:${firstContact.phoneNumber}")
                    }
                    context.startActivity(callIntent)
                    Log.d("SaahasSOS", "Call initiated to ${firstContact.phoneNumber}")
                } else {
                    showPhoneInputDialog = true
                    Log.d("SaahasSOS", "No contacts available")
                }
            }
        } else {
            permissionManager.checkAndRequestPermission(
                android.Manifest.permission.SEND_SMS,
                android.Manifest.permission.CALL_PHONE
            )
            Log.d("SaahasSOS", "Permissions not granted")
        }
    }

    fun onLocationClicked(openScreen: (String) -> Unit) {
        val uri = Uri.parse("geo:0,0?q=$locationText")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(intent)
    }

    fun onSignOut(openAndPopUp: (String, String) -> Unit) {
        openAndPopUp(SIGN_IN, SOS_SCREEN)
        authViewModel.getToken()?.let {
            context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE).edit().clear().apply()
        }
    }

    private fun getLatLng(): String {
        return lastKnownLatLng?.let { "${it.first},${it.second}" } ?: "unknown"
    }
}