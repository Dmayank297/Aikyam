package com.example.saahas.Service

import android.content.Context
import android.location.Location
import android.util.Log
import com.example.saahas.Utils.Location.LocationUtils
import com.example.saahas.Voice.Service.BuzzerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.*

class UnsafeAreaDetector(
    private val context: Context,
    private val locationRepository: LocationRepository,
    private val buzzerService: BuzzerService
) {
    private val locationUtils = LocationUtils(context)
    private var detectionJob: Job? = null
    private val detectorRadius = 10 //
    private val markRadius = 5 //
    private val triggerDistance = detectorRadius + markRadius

    fun startMonitoring() {
        if (detectionJob?.isActive == true) return

        detectionJob = CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                try {
                    val unsafeLocations = locationRepository.getUnsafeLocations()
                    val currentLocation = getCurrentLocation()

                    if (currentLocation != null && unsafeLocations.isNotEmpty()) {
                        val isNearUnsafe = unsafeLocations.any { unsafe ->
                            val distance = calculateDistance(
                                currentLocation.latitude,
                                currentLocation.longitude,
                                unsafe.lat,
                                unsafe.lng
                            )
                            Log.d("UnsafeAreaDetector", "Distance to ${unsafe.lat}, ${unsafe.lng}: $distance meters")
                            distance <= triggerDistance
                        }
                        if (isNearUnsafe) {
                            Log.d("UnsafeAreaDetector", "User in unsafe area, buzzing at ${currentLocation.latitude}, ${currentLocation.longitude}")
                            buzzerService.startBuzzer()
                        } else {
                            Log.d("UnsafeAreaDetector", "User out of unsafe area, stopping buzz at ${currentLocation.latitude}, ${currentLocation.longitude}")
                            buzzerService.stopBuzzer()
                        }
                    } else {
                        Log.d("UnsafeAreaDetector", "No location or unsafe areas yet: loc=$currentLocation, unsafeCount=${unsafeLocations.size}")
                        buzzerService.stopBuzzer()
                    }
                } catch (e: Exception) {
                    Log.e("UnsafeAreaDetector", "Error in monitoring: ${e.message}")
                    buzzerService.stopBuzzer()
                }
                delay(1000) // Check every 5 seconds
            }
        }
    }

    fun stopMonitoring() {
        detectionJob?.cancel()
        detectionJob = null
        buzzerService.stopBuzzer()
        Log.d("UnsafeAreaDetector", "Monitoring stopped")
    }

    private suspend fun getCurrentLocation(): Location? = withContext(Dispatchers.IO) {
        val loc = locationUtils.getLastKnownLocation()
        Log.d("UnsafeAreaDetector", "Current location: ${loc?.latitude}, ${loc?.longitude}")
        loc
    }

    // Haversine formula to calculate distance between two points in meters
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371000.0 // Earth's radius in meters
        val lat1Rad = Math.toRadians(lat1)
        val lon1Rad = Math.toRadians(lon1)
        val lat2Rad = Math.toRadians(lat2)
        val lon2Rad = Math.toRadians(lon2)

        val deltaLat = lat2Rad - lat1Rad
        val deltaLon = lon2Rad - lon1Rad

        val a = sin(deltaLat / 2).pow(2) + cos(lat1Rad) * cos(lat2Rad) * sin(deltaLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return r * c // Distance in meters
    }
}