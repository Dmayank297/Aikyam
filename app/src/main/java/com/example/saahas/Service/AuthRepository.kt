package com.example.authretro.Repository

import com.example.authretro.Service.RetrofitClient
import com.example.saahas.Models.LoginRequest
import com.example.saahas.Models.LoginResponse
import com.example.saahas.Models.SignupRequest
import retrofit2.Response

class AuthRepository {

    private val apiService = RetrofitClient.apiService

    suspend fun login(request: LoginRequest): Response<LoginResponse> {
        return apiService.login(request)
    }

    suspend fun signup(request: SignupRequest): Response<LoginResponse> {
        return apiService.signup(request)
    }
}
