package com.example.saahas.ui.Screens.Button.AudioRecord

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.media.MediaPlayer
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
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
import com.example.saahas.Models.VoiceRecordingHistory
import com.example.saahas.PermissionManager
import com.example.saahas.R

@SuppressLint("NewApi")
@Composable
fun VoiceRecordingScreen(
    permission: PermissionManager,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: AudioRecordViewModel = viewModel(
        factory = AudioRecordViewModelFactory(context.applicationContext as Application)
    )
    val isRecording by viewModel.isRecording.observeAsState(false)
    val allRecordings by viewModel.allRecordings.observeAsState(emptyList())
    val errorMessage by viewModel.errorMessage.observeAsState()

    var selectedTab by remember { mutableStateOf("Record") }


    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Anonymous Record",
            fontSize = 16.sp,
            modifier = Modifier.padding(top = 24.dp),
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
            "Record" -> {
                Recording(permission, viewModel, isRecording)
            }
            "History" -> {
                allRecordings.forEach { recording ->
                    RecordingHistory(recording = recording)
                }
            }
        }
    }
}

@Composable
fun Recording(
    permission: PermissionManager,
    viewModel: AudioRecordViewModel,
    isRecording: Boolean
) {
    val context = LocalContext.current
    var startTime by remember { mutableStateOf(0L) }
    val hasPermission = permission.isPermissionGranted(Manifest.permission.RECORD_AUDIO)

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
                    if (hasPermission) {
                        if (!isRecording) {
                            viewModel.startRecording()
                            startTime = System.currentTimeMillis()
                            Toast.makeText(context, "Voice recording started", Toast.LENGTH_SHORT).show()
                        } else {
                            val duration = System.currentTimeMillis() - startTime
                            viewModel.stopRecording()
                            Toast.makeText(context, "Voice recording stopped", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "Microphone Permission Denied!", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    painter = painterResource(if (!isRecording) R.drawable.recordprogress else R.drawable.recordprogressstart),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    tint = Color.Unspecified
                )
            }
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
                        if (isSelected(text)) MaterialTheme.colorScheme.primary else Color.Transparent,
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = text,
                    fontSize = 16.sp,
                    color = if (isSelected(text)) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun RecordingHistory(
    recording: VoiceRecordingHistory
) {
    var showMenu by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val mediaPlayerState by rememberUpdatedState(MediaPlayer())

    DisposableEffect(mediaPlayerState) {
        val mediaPlayer = mediaPlayerState
        onDispose {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
            }
            mediaPlayer.release()
        }
    }

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
            IconButton(
                onClick = {
                    val mediaPlayer = mediaPlayerState
                    if (isPlaying) {
                        mediaPlayer.pause()
                        isPlaying = false
                    } else {
                        try {
                            mediaPlayer.reset()
                            mediaPlayer.setDataSource(recording.filePath)
                            mediaPlayer.prepare()
                            mediaPlayer.start()
                            isPlaying = true
                            mediaPlayer.setOnCompletionListener {
                                isPlaying = false
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error playing audio: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            ) {
                Icon(
                    painter = painterResource(if (isPlaying) R.drawable.pause else R.drawable.play),
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color.Unspecified
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
                    onClick = { showMenu = false }
                )
            }
        }
    }
}