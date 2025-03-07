package com.example.saahas.ui.Screens.Location

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saahas.Models.UnsafeLocation
import com.example.saahas.Service.LocationRepository
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch

class MapViewModel : ViewModel() {
    private val repository = LocationRepository()

    private val _unsafeLocations = MutableLiveData<List<UnsafeLocation>>()
    val unsafeLocations: LiveData<List<UnsafeLocation>> get() = _unsafeLocations

    private val _markSuccess = MutableLiveData<LatLng?>()
    val markSuccess: LiveData<LatLng?> get() = _markSuccess

    fun loadUnsafeLocations() {
        viewModelScope.launch {
            val locations = repository.getUnsafeLocations()
            _unsafeLocations.value = locations
        }
    }

    fun markUnsafeLocation(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            val success = repository.markUnsafeLocation(latitude, longitude)
            if (success) {
                _markSuccess.value = LatLng(latitude, longitude)
                loadUnsafeLocations() // Refresh the list
            }
        }
    }

    fun clearMarkSuccess() {
        _markSuccess.value = null
    }
}