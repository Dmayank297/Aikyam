package com.example.saahas.ui.Screens.Location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.saahas.Models.Room.VoiceRecordingDatabase
import com.example.saahas.Models.UnsafeLocation
import com.example.saahas.PermissionManager
import com.example.saahas.R
import com.example.saahas.Service.LocationRepository
import com.example.saahas.Service.UnsafeAreaDetector
import com.example.saahas.Utils.Location.LocationUtils
import com.example.saahas.Voice.Service.BuzzerService
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("InlinedApi")
@Composable
fun LocationMapScreen(
    permission: PermissionManager,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val location = LocationUtils(context)
    val viewModel: LocationViewModel = viewModel(
        factory = LocationViewModelFactory(VoiceRecordingDatabase.getDatabase(context).ContactDao())
    )
    var selectedFriends by remember { mutableStateOf(listOf<String>()) }
    var duration by remember { mutableStateOf("Always") }
    val locationData by viewModel.locationLiveData.observeAsState()
    var isSharing by remember { mutableStateOf(false) }
    val nearbyPlaces by viewModel.nearbyPlaces.observeAsState(emptyList())
    val routes by viewModel.routes.observeAsState(emptyList())
    val apiKey = ""

    val errorMessage by viewModel.errorMessage.observeAsState()
    val scope = rememberCoroutineScope()

    val locationRepository = remember { LocationRepository() }
    val buzzerService = remember { BuzzerService() }
    val unsafeLocations = remember { mutableStateOf<List<UnsafeLocation>>(emptyList()) }
    val unsafeAreaDetector = remember { UnsafeAreaDetector(context, locationRepository, buzzerService) }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        if (location.hasLocationPermission(context)) {
            location.requestLocationUpdates(viewModel)
            unsafeAreaDetector.startMonitoring() // Start detector immediately
            // Periodically refresh unsafe locations for all users
            while (true) {
                unsafeLocations.value = locationRepository.getUnsafeLocations()
                delay(5000) // Refresh every 5 seconds to match detector
            }
        } else {
            permission.checkAndRequestPermission(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.SEND_SMS
            )
        }
    }

    LaunchedEffect(locationData) {
        scope.launch {
            while (isSharing) {
                delay(5_000) // 5 seconds
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Text(
            text = "Share Live Location",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        var expanded by remember { mutableStateOf(false) }
        Box {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = "Live Location Duration: $duration",
                    color = MaterialTheme.colorScheme.primary
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .background(Color.White)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                DropdownMenuItem(
                    text = { Text("Always") },
                    onClick = { duration = "Always"; expanded = false }
                )
                DropdownMenuItem(
                    text = { Text("1 Hour") },
                    onClick = { duration = "1 Hour"; expanded = false }
                )
                DropdownMenuItem(
                    text = { Text("8 Hours") },
                    onClick = { duration = "8 Hours"; expanded = false }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                if (location.hasLocationPermission(context)) {
                    if (!isSharing) {
                        location.requestLocationUpdates(viewModel)
                        viewModel.startLocationSharing(context)
                        isSharing = true
                    } else {
                        location.stopLocationUpdates()
                        viewModel.stopLocationSharing()
                        isSharing = false
                    }
                } else {
                    permission.checkAndRequestPermission(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.SEND_SMS
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(
                text = if (isSharing) "Stop Live Location" else "Start Live Location",
                color = Color.White,
                fontSize = 16.sp
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = {
                    if (location.hasLocationPermission(context)) {
                        viewModel.findNearbyEmergencyServices(context, apiKey, "Hospital")
                    } else {
                        permission.checkAndRequestPermission(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.FOREGROUND_SERVICE
                        )
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .padding(end = 4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("Find Hospital", color = Color.White, fontSize = 16.sp)
            }
            Button(
                onClick = {
                    if (location.hasLocationPermission(context)) {
                        viewModel.findNearbyEmergencyServices(context, apiKey, "Police Station")
                    } else {
                        permission.checkAndRequestPermission(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.FOREGROUND_SERVICE
                        )
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .padding(start = 4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("Find Police", color = Color.White, fontSize = 16.sp)
            }
        }

        Button(
            onClick = {
                if (location.hasLocationPermission(context)) {
                    locationData?.let { data ->
                        scope.launch {
                            val success = locationRepository.markUnsafeLocation(data.latitude, data.longitude)
                            Toast.makeText(
                                context,
                                if (success) "Location marked as unsafe" else "Failed to mark unsafe",
                                Toast.LENGTH_SHORT
                            ).show()
                            unsafeLocations.value = locationRepository.getUnsafeLocations()
                        }
                    } ?: run {
                        Toast.makeText(context, "Location not available yet", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    permission.checkAndRequestPermission(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
        ) {
            Text(
                text = "Mark Unsafe",
                color = Color.White,
                fontSize = 16.sp
            )
        }

        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp),
            factory = { context ->
                MapView(context).apply {
                    onCreate(null)
                    onResume()
                    getMapAsync { googleMap ->
                        if (location.hasLocationPermission(context)) {
                            googleMap.isMyLocationEnabled = true
                        }
                        locationData?.let { data ->
                            val latLng = LatLng(data.latitude, data.longitude)
                            googleMap.clear()
                            googleMap.addMarker(MarkerOptions().position(latLng).title("You"))
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                        }
                        nearbyPlaces.forEach { (latLng, title) ->
                            val markerOptions = MarkerOptions().position(latLng).title(title)
                            when (title) {
                                "Hospital" -> markerOptions.icon(
                                    bitmapDescriptorFromVector(context, R.drawable.ic_hospital_red)
                                )
                                "Police Station" -> markerOptions.icon(
                                    bitmapDescriptorFromVector(context, R.drawable.ic_police_blue)
                                )
                            }
                            googleMap.addMarker(markerOptions)
                        }
                        routes.forEach { route ->
                            val color = if (nearbyPlaces.firstOrNull()?.second == "Hospital") {
                                android.graphics.Color.RED
                            } else {
                                android.graphics.Color.BLUE
                            }
                            googleMap.addPolyline(PolylineOptions().addAll(route).color(color))
                        }
                        unsafeLocations.value.forEach { unsafe ->
                            val latLng = LatLng(unsafe.lat, unsafe.lng)
                            googleMap.addMarker(
                                MarkerOptions()
                                    .position(latLng)
                                    .title("Unsafe")
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                            )
                        }
                    }
                }
            },
            update = { mapView ->
                mapView.getMapAsync { googleMap ->
                    locationData?.let { data ->
                        val latLng = LatLng(data.latitude, data.longitude)
                        googleMap.clear()
                        googleMap.addMarker(MarkerOptions().position(latLng).title("You"))
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                    }
                    nearbyPlaces.forEach { (latLng, title) ->
                        val markerOptions = MarkerOptions().position(latLng).title(title)
                        when (title) {
                            "Hospital" -> markerOptions.icon(
                                bitmapDescriptorFromVector(context, R.drawable.ic_hospital_red)
                            )
                            "Police Station" -> markerOptions.icon(
                                bitmapDescriptorFromVector(context, R.drawable.ic_police_blue)
                            )
                        }
                        googleMap.addMarker(markerOptions)
                    }
                    routes.forEach { route ->
                        val color = if (nearbyPlaces.firstOrNull()?.second == "Hospital") {
                            android.graphics.Color.RED
                        } else {
                            android.graphics.Color.BLUE
                        }
                        googleMap.addPolyline(PolylineOptions().addAll(route).color(color))
                    }
                    unsafeLocations.value.forEach { unsafe ->
                        val latLng = LatLng(unsafe.lat, unsafe.lng)
                        googleMap.addMarker(
                            MarkerOptions()
                                .position(latLng)
                                .title("Unsafe")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                        )
                    }
                }
            }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            if (isSharing) {
                location.stopLocationUpdates()
                viewModel.stopLocationSharing()
            }
            unsafeAreaDetector.stopMonitoring()
        }
    }
}


private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor {
    val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)
    vectorDrawable?.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)
    val bitmap = Bitmap.createBitmap(
        vectorDrawable?.intrinsicWidth ?: 48,
        vectorDrawable?.intrinsicHeight ?: 48,
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    vectorDrawable?.draw(canvas)
    return BitmapDescriptorFactory.fromBitmap(bitmap)
}