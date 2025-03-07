package com.example.saahas.Models

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val token: String?
)

data class SignupRequest(
    val username: String,
    val email: String,
    val password: String
)

data class SignupResponse(
    val token: String
)