package com.example.saahas.ui.Screens.Button.AudioRecord

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.saahas.Models.Room.VoiceRecordingDatabase
import com.example.saahas.Models.Room.VoiceRecordingRepository
import com.example.saahas.Models.VoiceRecordingHistory
import com.example.saahas.Utils.Audio.AudioRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AudioRecordViewModel(application: Application) : AndroidViewModel(application) {
    private val audioRecorder = AudioRecorder(application.applicationContext)
    private val repository = VoiceRecordingRepository(
         VoiceRecordingDatabase.getDatabase(application).voiceRecordingDao(),
    )

    private val _isRecording = MutableLiveData(false)
    val isRecording: LiveData<Boolean> = _isRecording

    private val _recordedFilePath = MutableLiveData<String?>()
    val recordedFilePath: LiveData<String?> = _recordedFilePath

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    val allRecordings: LiveData<List<VoiceRecordingHistory>> = repository.allRecordings

    fun startRecording() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val filePath = audioRecorder.startRecording()
                if (filePath != null) {
                    _isRecording.postValue(true)
                    _recordedFilePath.postValue(filePath)
                    _errorMessage.postValue(null)
                } else {
                    _errorMessage.postValue("Failed to start recording")
                }
            } catch (e: Exception) {
                _errorMessage.postValue(e.message)
            }
        }
    }

    fun stopRecording() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = audioRecorder.stopRecording()
                _isRecording.postValue(false)
                if (result != null) {
                    val (filePath, duration) = result
                    _recordedFilePath.postValue(filePath)
                    _errorMessage.postValue(null)

                    val recording = VoiceRecordingHistory(
                        filePath = filePath!!,
                        duration = duration,
                        timestamp = System.currentTimeMillis()
                    )
                    repository.insertRecording(recording)
                } else {
                    _errorMessage.postValue("Failed to stop recording")
                }
            } catch (e: Exception) {
                _errorMessage.postValue(e.message)
            }
        }
    }

    fun deleteRecording(recordingId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.deleteRecording(recordingId)
                if (_recordedFilePath.value != null) {
                    audioRecorder.deleteRecording(_recordedFilePath.value!!)
                    _recordedFilePath.postValue(null)
                }
            } catch (e: Exception) {
                _errorMessage.postValue("Failed to delete recording: ${e.message}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (audioRecorder.isRecording()) {
            audioRecorder.stopRecording()
        }
    }
}