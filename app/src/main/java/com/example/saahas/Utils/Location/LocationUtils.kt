package com.example.saahas.Utils.Location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Looper
import androidx.core.content.ContextCompat
import com.example.saahas.Models.LocationData
import com.example.saahas.ui.Screens.Location.LocationViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class LocationUtils(val context: Context) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    private var locationCallback: LocationCallback? = null

    @Suppress("MissingPermission")
    fun requestLocationUpdates(viewModel: LocationViewModel) {
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let {
                    val location = LocationData(it.latitude, it.longitude)
                    viewModel.updateLocation(location)
                }
            }
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 5000 // Update every 5 seconds
        ).build()

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
        this.locationCallback = locationCallback
    }

    fun hasLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    fun stopLocationUpdates() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
            locationCallback = null
        }
    }

    fun reverseGeocodeLocation(location: LocationData): String {
        val geocoder = Geocoder(context, Locale.getDefault())
        val coordinates = LatLng(location.latitude, location.longitude)
        val addresses: MutableList<Address>? =
            geocoder.getFromLocation(coordinates.latitude, coordinates.longitude, 1)

        return if (addresses?.isNotEmpty() == true) {
            addresses[0].getAddressLine(0)
        } else {
            "Address Not Found"
        }
    }

    @Suppress("MissingPermission")
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun getLastKnownLocation(): Location? = suspendCancellableCoroutine { continuation ->
        if (hasLocationPermission(context)) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    continuation.resume(location)
                }
                .addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
        } else {
            continuation.resume(null)
        }
    }
}