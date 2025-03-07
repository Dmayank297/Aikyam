package com.example.saahas.ui.Screens.Location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.SmsManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saahas.Models.LocationData
import com.example.saahas.Models.Room.ContactDao
import com.google.android.gms.maps.model.LatLng
import com.google.maps.GeoApiContext
import com.google.maps.PlacesApi
import com.google.maps.model.PlaceType
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import com.google.maps.model.LatLng as GoogleMapsLatLng

class LocationViewModel(
     val contactDao: ContactDao
) : ViewModel() {

    private val _locationLiveData = MutableLiveData<LocationData?>()
    val locationLiveData: LiveData<LocationData?> = _locationLiveData

    private val _nearbyPlaces = MutableLiveData<List<Pair<LatLng, String>>>(emptyList())
    val nearbyPlaces: LiveData<List<Pair<LatLng, String>>> = _nearbyPlaces

    private val _routes = MutableLiveData<List<List<LatLng>>>(emptyList())
    val routes: LiveData<List<List<LatLng>>> = _routes

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private var smsJob: Job? = null
    private val smsManager = SmsManager.getDefault()

    fun updateLocation(newLocation: LocationData) {
        _locationLiveData.value = newLocation
    }

    fun startLocationSharing(context: Context) {
        smsJob?.cancel()
        smsJob = viewModelScope.launch {
            _locationLiveData.value?.let { location ->
                val contacts = contactDao.getAllContacts()
                if (contacts.isNotEmpty()) {
                    val locationUrl = "https://www.google.com/maps?q=${location.latitude},${location.longitude}"
                    val message = "My live location: $locationUrl"
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                        try {
                            contacts.forEach { contact ->
                                smsManager.sendTextMessage(contact.phoneNumber, null, message, null, null)
                                android.util.Log.d("LocationShare", "SMS sent to ${contact.phoneNumber}: $message")
                            }
                            Toast.makeText(context, "SOS Messages Sent!", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            android.util.Log.e("LocationShare", "Failed to send SMS", e)
                            _errorMessage.postValue("Failed to send SMS: ${e.message}")
                        }
                    } else {
                        _errorMessage.postValue("SMS permission not granted")
                        android.util.Log.d("LocationShare", "SMS permission not granted")
                    }
                } else {
                    android.util.Log.d("LocationShare", "No contacts available")
                    Toast.makeText(context, "No contacts available for SOS!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun stopLocationSharing() {
        smsJob?.cancel()
        smsJob = null
        android.util.Log.d("LocationShare", "Location sharing stopped")
    }

    fun findNearbyEmergencyServices(context: Context, apiKey: String, tag: String) {
        _locationLiveData.value?.let { currentLocation ->
            viewModelScope.launch {
                Toast.makeText(context, "Fetching $tag nearby, please wait...", Toast.LENGTH_SHORT).show()
                val geoContext = GeoApiContext.Builder().apiKey(apiKey).build()
                val origin = GoogleMapsLatLng(currentLocation.latitude, currentLocation.longitude)
                android.util.Log.d("LocationShare", "Finding $tag from: ${origin.lat},${origin.lng}")

                val places = mutableListOf<Pair<LatLng, String>>()
                val type = if (tag == "Hospital") PlaceType.HOSPITAL else PlaceType.POLICE

                try {
                    val request = PlacesApi.nearbySearchQuery(geoContext, origin)
                        .radius(5000) // 5 km radius
                        .type(type)
                        .await()
                    if (request.results.isNotEmpty()) {
                        request.results.forEach { place ->
                            val placeLatLng = LatLng(place.geometry.location.lat, place.geometry.location.lng)
                            places.add(Pair(placeLatLng, place.name ?: tag))
                            android.util.Log.d("LocationShare", "$tag found: ${place.name} at ${placeLatLng.latitude},${placeLatLng.longitude}")
                        }
                        Toast.makeText(context, "$tag found successfully!", Toast.LENGTH_SHORT).show()
                    } else {
                        _errorMessage.postValue("No nearby ${tag.toLowerCase()}s found")
                        android.util.Log.d("LocationShare", "No ${tag.toLowerCase()}s found within 5 km")
                        Toast.makeText(context, "No nearby ${tag.toLowerCase()}s found", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    _errorMessage.postValue("Error finding ${tag.toLowerCase()}s: ${e.message}")
                    android.util.Log.e("LocationShare", "$tag search failed: ${e.message}", e)
                    Toast.makeText(context, "Failed to fetch $tag due to network issue", Toast.LENGTH_SHORT).show()
                }

                _nearbyPlaces.value = places
                _routes.value = emptyList() // No routes anymore
                android.util.Log.d("LocationShare", "Updated places for $tag: ${places.size}")
            }
        } ?: run {
            _errorMessage.postValue("Current location unavailable")
            android.util.Log.d("LocationShare", "No current location available")
            Toast.makeText(context, "Current location unavailable", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopLocationSharing()
    }
}