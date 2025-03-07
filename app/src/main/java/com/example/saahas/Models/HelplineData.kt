package com.example.saahas.Models

import androidx.annotation.DrawableRes

data class HelplineData(
    val number: String,
    val name: String,
    @DrawableRes val imageRes: Int
)
