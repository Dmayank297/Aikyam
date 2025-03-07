package com.example.saahas.ui.Authentication

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.authretro.Repository.AuthRepository
import com.example.saahas.Models.LoginRequest
import com.example.saahas.Models.SignupRequest
import com.example.saahas.SOS_SCREEN
import com.example.saahas.SIGN_IN
import com.example.saahas.SIGN_UP
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {


    private val authRepository = AuthRepository()
    private val _authResult = MutableLiveData<String?>()
    val authResult: LiveData<String?> get() = _authResult

    private val sharedPreferences = application.getSharedPreferences("app_prefs", Application.MODE_PRIVATE)

    private fun saveToken(token: String) {
        sharedPreferences.edit().putString("token", token).apply()
    }

    fun getToken(): String? = sharedPreferences.getString("token", null)

    fun logout() {
        sharedPreferences.edit().remove("token").apply()
    }
    // LOGIN FUNCTION
    fun login(email: String, password: String, openAndPopUp: (String, String) -> Unit) {
        viewModelScope.launch {
            Log.d("SaahasAuth", "Login started: email=$email")
            _authResult.value = "loading"

            try {
                val response = authRepository.login(LoginRequest(email, password))
                Log.d("SaahasAuth", "Login response code: ${response.code()}")
                if (response.isSuccessful) {
                    val token = response.body()?.token
                    if (token != null) {
                        saveToken(token)
                        _authResult.value = "success"
                        Log.d("SaahasAuth", "Login successful, token: $token")
                        openAndPopUp(SOS_SCREEN, SIGN_IN)
                        Log.d("SaahasAuth", "Navigation call completed")
                    } else {
                        _authResult.value = "error: No token in response"
                        Log.d("SaahasAuth", "No token found in response body")
                    }
                } else {
                    _authResult.value = "error: ${response.message()}"
                    Log.d("SaahasAuth", "Login failed: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("SaahasAuth", "Login error: ${e.message}", e)
                _authResult.value = "error: ${e.message}"
            }
        }
    }

    // SIGNUP FUNCTION
    fun signup(email: String, password: String, name: String, openAndPopUp: (String, String) -> Unit) {
        viewModelScope.launch {
            _authResult.value = "loading"

            try {
                val response = authRepository.signup(SignupRequest(name, email, password))
                if (response.isSuccessful) {
                    response.body()?.token?.let {
                        saveToken(it)
                        _authResult.value = "success"
                        openAndPopUp(SIGN_IN, SIGN_UP)
                        Log.d("AuthViewModel", "Navigating to SIGN in screen")
                        delay(100)
                    } ?: run { _authResult.value = "error: Invalid response" }
                } else {
                    _authResult.value = "error: ${response.message()}"
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Signup error: ${e.message}")
                _authResult.value = "error: ${e.message}"
            }
        }
    }
}