package com.example.saahas.Models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Voice_Recordings")
data class VoiceRecordingHistory(
    @PrimaryKey(autoGenerate = true)  val id: Long = 0,               // Unique ID for each recording
    val filePath: String,
    val duration: Long,
    val timestamp: Long,
    val isUploaded: Boolean = false
)

