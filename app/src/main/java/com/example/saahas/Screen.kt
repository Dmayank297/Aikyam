package com.example.saahas


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val showBottomBar: Boolean,
    val icon: ImageVector? = null,
    val image: Int? = null
) {
    object SOS : Screen(
        route = SOS_SCREEN,
        title = "Smart SOS",
        showBottomBar = true,
        icon = Icons.Default.Home
    )
    object LocationTracker : Screen(
        route = LOCATION_MAP_SCREEN,
        title = "Location Tracker",
        showBottomBar = true,
        icon = Icons.Default.LocationOn
    )
    object Record : Screen(
        route = VOICE_RECORDING_SCREEN,
        title = "Record",
        showBottomBar = true,
        image = R.drawable.baseline_mic_24
    )
    object VoiceControl : Screen(
        route = VOICE_CONTROL_SCREEN,
        title = "Smart SOS Voice",
        showBottomBar = false
    )
    object ShakeTrigger : Screen(
        route = SENSOR_SCREEN,
        title = "Shake Trigger",
        showBottomBar = false
    )
    object AddFriends : Screen(
        route = ADD_FRIENDS,
        title = "Add Friends",
        showBottomBar = false
    )
}
