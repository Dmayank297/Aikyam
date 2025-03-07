package com.example.saahas.Models.Room

import androidx.lifecycle.LiveData
import com.example.saahas.Models.VoiceRecordingHistory

class VoiceRecordingRepository(private val dao: VoiceRecordingDao) {
    val allRecordings: LiveData<List<VoiceRecordingHistory>> = dao.getAllRecordings()

    suspend fun insertRecording(recording: VoiceRecordingHistory) {
        dao.insertRecording(recording)
    }

    suspend fun deleteRecording(recordingId: Long) {
        dao.deleteRecording(recordingId)
    }
}
