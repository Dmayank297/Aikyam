package com.example.authretro.Service

import com.example.saahas.Models.LoginRequest
import com.example.saahas.Models.LoginResponse
import com.example.saahas.Models.SignupRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("/api/auth/login")  // Adjust the endpoint as per your friend's API
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("/api/auth/signup")
    suspend fun signup(@Body request: SignupRequest): Response<LoginResponse>
}
