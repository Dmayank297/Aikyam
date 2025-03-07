package com.example.saahas.ui.Screens

import android.Manifest
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.saahas.Models.Room.VoiceRecordingDatabase
import com.example.saahas.Models.VoiceRecordingHistory
import com.example.saahas.PermissionManager
import com.example.saahas.R
import com.example.saahas.Voice.Helper.VoiceIntegrationFile
import com.example.saahas.Voice.Service.BuzzerService
import com.example.saahas.ui.Screens.Location.LocationViewModel
import com.example.saahas.ui.Screens.Location.LocationViewModelFactory
import com.google.android.gms.location.LocationServices

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun VoiceScreen(permission: PermissionManager, modifier: Modifier = Modifier) {
    var selectedTab by remember { mutableStateOf("Record") }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Anonymous Record",
            fontSize = 16.sp,
            modifier = Modifier.padding(top = 16.dp),
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp)
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp)),
            horizontalArrangement = Arrangement.Center
        ) {
            TabButton(
                isSelected = { it == selectedTab },
                onClick = { selectedTab = it }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        when (selectedTab) {
            "Record" -> Recording(permission)
            "History" -> {
                // Placeholder for history tab - no history functionality yet
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun Recording(permission: PermissionManager, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var response by remember { mutableStateOf("") }
    val locationViewModel = viewModel<LocationViewModel>(
        factory = LocationViewModelFactory(VoiceRecordingDatabase.getDatabase(context).ContactDao())
    )
    val buzzerService = remember { BuzzerService() }
    val voiceIntegration = remember { VoiceIntegrationFile(context, buzzerService, locationViewModel) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var expanded by remember { mutableStateOf(false) }
    val languages = listOf("English (en-US)", "Hindi (hi-IN)", "French (fr-FR)", "Spanish (es-ES)")
    var selectedLanguage by remember { mutableStateOf("English (en-US)") }

    LaunchedEffect(Unit) {
        permission.checkAndRequestPermission(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.SEND_SMS
        )
        if (permission.isPermissionGranted(Manifest.permission.RECORD_AUDIO)) {
            voiceIntegration.startListening()
        }
        if (permission.isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    locationViewModel.updateLocation(
                        com.example.saahas.Models.LocationData(
                            latitude = location?.latitude ?: 0.0,
                            longitude = location?.longitude ?: 0.0
                        )
                    )
                }
                .addOnFailureListener {
                    Log.e("VoiceScreen", "Failed to get location: ${it.message}")
                }
        }
    }

    LaunchedEffect(response) {
        voiceIntegration.processCommand(response)
    }

    DisposableEffect(Unit) {
        onDispose {
            voiceIntegration.cleanup()
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            Box {
                Button(onClick = { expanded = true }) {
                    Text("Language: $selectedLanguage")
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    languages.forEach { lang ->
                        DropdownMenuItem(
                            text = { Text(lang) },
                            onClick = {
                                selectedLanguage = lang
                                expanded = false
                                val langCode = when (lang) {
                                    "English (en-US)" -> "en-US"
                                    "Hindi (hi-IN)" -> "hi-IN"
                                    "French (fr-FR)" -> "fr-FR"
                                    "Spanish (es-ES)" -> "es-ES"
                                    else -> "en-US"
                                }
                                voiceIntegration.changeLanguage(langCode)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            var isVoiceRecordStart by remember { mutableStateOf(false) }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp, horizontal = 16.dp)
                    .size(280.dp),
                colors = CardDefaults.cardColors(Color.Transparent)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = {
                            if (permission.isPermissionGranted(Manifest.permission.RECORD_AUDIO)) {
                                if (!isVoiceRecordStart) {
                                    voiceIntegration.startListening()
                                    isVoiceRecordStart = true
                                    Toast.makeText(context, "Voice recording started", Toast.LENGTH_SHORT).show()
                                } else {
                                    voiceIntegration.stopListening()
                                    isVoiceRecordStart = false
                                    Toast.makeText(context, "Voice recording stopped", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(context, "Microphone Permission Denied!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            painter = painterResource(if (!isVoiceRecordStart) R.drawable.recordprogress else R.drawable.recordprogressstart),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            tint = Color.Unspecified
                        )
                    }
                }
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isVoiceRecordStart) {
                    IconButton(onClick = {}) {
                        Icon(
                            painter = painterResource(R.drawable.play),
                            contentDescription = null,
                            tint = Color.Unspecified
                        )
                    }
                    IconButton(onClick = {}) {
                        Icon(
                            painter = painterResource(R.drawable.pause),
                            contentDescription = null,
                            tint = Color.Unspecified
                        )
                    }
                }
                Column {
                    Button(onClick = { buzzerService.startBuzzer() }) {
                        Text("Start Buzzer")
                    }
                    Button(onClick = { buzzerService.stopBuzzer() }) {
                        Text("Stop Buzzer")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(response, fontSize = 20.sp)
        }
    }
}

@Composable
fun TabButton(
    isSelected: (String) -> Boolean,
    onClick: (String) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        listOf("Record", "History").forEach { text ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 8.dp)
                    .clickable { onClick(text) }
                    .background(
                        if (isSelected(text)) Color.Red else Color.Transparent,
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = text,
                    fontSize = 16.sp,
                    color = if (isSelected(text)) Color.White else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun RecordingHistory(
    recording: VoiceRecordingHistory,
    recordingsList: MutableList<VoiceRecordingHistory>
) {
    var showMenu by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .shadow(4.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { isPlaying = !isPlaying }) {
                Icon(
                    painter = painterResource(if (isPlaying) R.drawable.pause else R.drawable.play),
                    contentDescription = if (isPlaying) "Pause" else "Play"
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Recording ${recording.id}", fontWeight = FontWeight.Bold)
                Text(text = "Duration: ${recording.duration / 1000} sec", style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = { showMenu = true }) {
                Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More Options")
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Delete") },
                    onClick = {
                        showMenu = false
                        recordingsList.remove(recording)
                    }
                )
            }
        }
    }
}