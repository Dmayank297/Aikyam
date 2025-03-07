package com.example.saahas.Voice.Service

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.core.content.ContextCompat

class VoiceCommandService(
    private val context: Context,
    private val onCommandDetected: (String) -> Unit
) {
    private val speechRecognizer: SpeechRecognizer? = try {
        val sr = SpeechRecognizer.createSpeechRecognizer(context)
        Log.d("VoiceCommandService", "SpeechRecognizer created: ${sr != null}")
        sr
    } catch (e: Exception) {
        Log.e("VoiceCommandService", "Failed to create SpeechRecognizer: ${e.message}", e)
        null
    }
    private var isListening = false

    init {
        if (speechRecognizer == null) {
            Log.e("VoiceCommandService", "SpeechRecognizer initialization failed")
        }
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.d("VoiceCommandService", "Ready for speech")
            }
            override fun onBeginningOfSpeech() {
                Log.d("VoiceCommandService", "Beginning of speech detected")
            }
            override fun onRmsChanged(rmsdB: Float) {
                // Log.d("VoiceCommandService", "RMS changed: $rmsdB")
            }
            override fun onBufferReceived(buffer: ByteArray?) {
                Log.d("VoiceCommandService", "Audio buffer received")
            }
            override fun onEndOfSpeech() {
                Log.d("VoiceCommandService", "End of speech detected")
            }
            override fun onError(error: Int) {
                val errorMsg = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                    SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                    SpeechRecognizer.ERROR_NETWORK -> "Network error"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                    SpeechRecognizer.ERROR_NO_MATCH -> "No match found"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer busy"
                    SpeechRecognizer.ERROR_SERVER -> "Server error"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout"
                    else -> "Unknown error"
                }
                Log.e("VoiceCommandService", "Speech error: $errorMsg (code: $error)")
                stopListening()
            }
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                matches?.firstOrNull()?.let { command ->
                    Log.d("VoiceCommandService", "Recognized: $command")
                    onCommandDetected(command.lowercase())
                } ?: Log.d("VoiceCommandService", "No results received")
                stopListening()
            }
            override fun onPartialResults(partialResults: Bundle?) {
                Log.d("VoiceCommandService", "Partial results: ${partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)}")
            }
            override fun onEvent(eventType: Int, params: Bundle?) {
                Log.d("VoiceCommandService", "Event: $eventType")
            }
        })
    }

    fun startListening() {
        if (isListening) {
            Log.d("VoiceCommandService", "Already listening, ignoring request")
            return
        }
        if (speechRecognizer == null) {
            Log.e("VoiceCommandService", "SpeechRecognizer is null, cannot start listening")
            return
        }
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            Log.e("VoiceCommandService", "Speech recognition not available on this device")
            return
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Log.e("VoiceCommandService", "Microphone permission not granted, cannot listen")
            // Note: Can't request permission from a service, must be handled in UI
            return
        }
        isListening = true
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, false) // Ensure online recognition
        }
        try {
            speechRecognizer.startListening(intent)
            Log.d("VoiceCommandService", "Started listening to speech")
        } catch (e: Exception) {
            Log.e("VoiceCommandService", "Failed to start listening: ${e.message}", e)
            stopListening()
        }
    }

    fun stopListening() {
        if (isListening) {
            isListening = false
            speechRecognizer?.stopListening()
            Log.d("VoiceCommandService", "Stopped listening")
        }
    }

    fun destroy() {
        stopListening()
        speechRecognizer?.destroy()
        Log.d("VoiceCommandService", "Destroyed")
    }
}