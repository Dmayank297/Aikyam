package com.example.saahas.ui.Screens.SpeechRecognation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.saahas.Voice.Service.BuzzerService
import com.example.saahas.Voice.Service.VoiceBackgroundService
import com.example.saahas.Voice.Service.VoiceCommandService

@Composable
fun VoiceControlScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var isListening by remember { mutableStateOf(false) }
    var commandStatus by remember { mutableStateOf("Say 'start buzzer' or 'stop buzzer'") }
    var isBackgroundServiceRunning by remember { mutableStateOf(false) }
    val buzzerService = remember { BuzzerService() }
    val voiceService = remember {
        VoiceCommandService(context) { command ->
            when (command) {
                "start buzzer", "begin buzz" -> {
                    buzzerService.startBuzzer()
                    commandStatus = "Buzzer started"
                }
                "stop buzzer", "end buzz" -> {
                    buzzerService.stopBuzzer()
                    commandStatus = "Buzzer stopped"
                }
                else -> commandStatus = "Unrecognized: $command"
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            commandStatus = "Permission granted"
        } else {
            commandStatus = "Microphone permission denied"
        }
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = commandStatus, fontSize = 20.sp, modifier = Modifier.padding(bottom = 16.dp))
        Button(onClick = {
            if (isListening) {
                isListening = false
                voiceService.stopListening()
                commandStatus = "Stopped listening"
            } else {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                    isListening = true
                    voiceService.startListening()
                    commandStatus = "Listening..."
                } else {
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            }
        }) {
            Text(text = if (isListening) "Stop Listening" else "Start Listening")
        }
        Button(onClick = {
            if (isBackgroundServiceRunning) {
                context.stopService(Intent(context, VoiceBackgroundService::class.java))
                isBackgroundServiceRunning = false
                commandStatus = "Background service stopped"
            } else {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                    val serviceIntent = Intent(context, VoiceBackgroundService::class.java)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(serviceIntent)
                    } else {
                        context.startService(serviceIntent)
                    }
                    isBackgroundServiceRunning = true
                    commandStatus = "Background service started - Say 'Porcupine' or 'Alexa'"
                } else {
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    commandStatus = "Please grant microphone permission first"
                }
            }
        }) {
            Text(text = if (isBackgroundServiceRunning) "Stop Background Service" else "Start Background Service")
        }
        Button(onClick = {
            buzzerService.stopBuzzer()
            commandStatus = "Buzzer stopped manually"
        }) {
            Text(text = "Stop Buzzer Manually")
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            voiceService.destroy()
            buzzerService.stopBuzzer()
        }
    }
}