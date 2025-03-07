package com.example.saahas.Service

import android.util.Log
import com.example.saahas.Models.UnsafeLocation
import com.example.saahas.Models.UnsafeLocationRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LocationRepository {
    private val backendUrl = "https://87ee-103-246-194-81.ngrok-free.app" // Replace with active URL

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(backendUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService: LocationApiService = retrofit.create(LocationApiService::class.java)

    suspend fun markUnsafeLocation(lat: Double, lng: Double): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = UnsafeLocationRequest(lat, lng)
            val response = apiService.markUnsafeLocation(request)
            if (response.isSuccessful) {
                true
            } else {
                Log.e("Repository", "Failed to mark unsafe: ${response.message()}")
                false
            }
        } catch (e: Exception) {
            Log.e("Repository", "Error marking unsafe: ${e.message}")
            false
        }
    }

    suspend fun getUnsafeLocations(): List<UnsafeLocation> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getUnsafeLocations()
            Log.d("LocationRepository", "Fetched locations: ${response.body()}")
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                Log.e("Repository", "Failed to load locations: ${response.message()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("Repository", "Error fetching locations: ${e.message}")
            emptyList()
        }
    }
}