package com.example.saahas.ui.Screens.OnBoarding

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.saahas.Models.OnboardingItem
import com.example.saahas.R

class OnboardingViewModel : ViewModel() {
    // List of onboarding items (images, titles, descriptions)
    val onboardingItems = listOf(
        OnboardingItem(
            imageRes = R.drawable.recordingonboarding,
            title = "Voice System",
            description = "Stay safe with voice-activated SOS call for help instantly, even when you can’t reach your phone, ensuring protection anytime anywhere."
        ),
        OnboardingItem(
            imageRes = R.drawable.sosonboarding,
            title = "SOS",
            description = "Instant SOS for emergencies and real-time alerts travel fearlessly with quick help at your fingertips, ensuring your safety every step of the way."
        ),
        OnboardingItem(
            imageRes = R.drawable.assistanceonboarding,
            title = "Instant Assistance",
            description = "Stay secure with instant assistance travel confidently knowing help is always within reach, ensuring your safety every step of the way."
        ),
        OnboardingItem(
            imageRes = R.drawable.communityonboarding,
            title = "Community",
            description = "Manage Emergency Contacts Add or remove trusted contacts to receive instant SOS alerts, ensuring help is always just a tap away\nwith the help of Saahas."
        )
    )

    // Current page state as MutableLiveData to trigger recomposition
    private val _currentPage = MutableLiveData(0)
    val currentPage: LiveData<Int> = _currentPage

    // Move to the next page
    fun nextPage(): Boolean {
        val current = _currentPage.value ?: 0
        return if (current < onboardingItems.size - 1) {
            _currentPage.value = current + 1
            true
        } else {
            false
        }
    }

    // Reset to first page (optional, if needed)
    fun resetPage() {
        _currentPage.value = 0
    }
}