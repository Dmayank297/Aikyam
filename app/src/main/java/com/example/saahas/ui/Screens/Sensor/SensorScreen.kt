package com.example.saahas.ui.Screens.Sensor

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.saahas.Accessibility.Services.SensorViewModelFactory
import com.example.saahas.Models.Room.VoiceRecordingDatabase
import com.example.saahas.PermissionManager

@Composable
fun SensorScreen(
    permissionManager: PermissionManager
) {
    val context = LocalContext.current
    val viewModelFactory = remember {
        SensorViewModelFactory(context.applicationContext as Application, context, permissionManager, VoiceRecordingDatabase.getDatabase(context).ContactDao())
    }
    val sensorViewModel: SensorViewModel = viewModel(factory = viewModelFactory)

    val shakeDetected by sensorViewModel.shakeDetected.collectAsState()
    val rotationData by sensorViewModel.rotationData.collectAsState()

    LaunchedEffect(shakeDetected) {
        if (shakeDetected) {
            sensorViewModel.onSOSButtonClicked()
            Toast.makeText(context, "Emergency level 3 initiated", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (shakeDetected) "Shake Status: Detected!" else "Shake Status: Not Detected",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Gyro Data: X=${rotationData.first}, Y=${rotationData.second}, Z=${rotationData.third}",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            sensorViewModel.startSensors()
            Log.d("SensorUI", "Sensors Started")
        }) {
            Text("Start Sensors")
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(onClick = {
            sensorViewModel.stopSensors()
            Log.d("SensorUI", "Sensors Stopped")
        }) {
            Text("Stop Sensors")
        }
    }
}
