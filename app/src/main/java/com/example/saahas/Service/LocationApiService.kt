package com.example.saahas.Service

import com.example.saahas.Models.UnsafeLocation
import com.example.saahas.Models.UnsafeLocationRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface LocationApiService {
    @POST("/api/unsafe")
    suspend fun markUnsafeLocation(@Body request: UnsafeLocationRequest): Response<Unit>

    @GET("/api/unsafe")
    suspend fun getUnsafeLocations(): Response<List<UnsafeLocation>>
}