package com.example.saahas.Models

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.painter.Painter

data class OnboardingItem(
    @DrawableRes val imageRes: Int,
    val title: String,
    val description: String,
)
