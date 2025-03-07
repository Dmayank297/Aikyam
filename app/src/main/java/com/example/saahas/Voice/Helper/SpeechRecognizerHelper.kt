package com.example.saahas.Voice.Helper

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import kotlin.apply
import kotlin.collections.firstOrNull
import kotlin.text.orEmpty

class SpeechRecognizerHelper(
    private val context: Context,
    private val onResult: (String) -> Unit
) {
    private var speechRecognizer: SpeechRecognizer? = null
    var isListening = false
    private var errorCount = 0
    private val maxErrorRetries = 3

    fun startListening() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Microphone permission required!", Toast.LENGTH_SHORT).show()
            return
        }

        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        Toast.makeText(context, "Listening...", Toast.LENGTH_SHORT).show()
                        isListening = true
                        Log.d("SpeechRecognizer", "Ready for Speech")
                    }

                    override fun onBeginningOfSpeech() {
                        Log.d("SpeechRecognizer", "User started speaking")
                    }

                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        Toast.makeText(context, "Stopped Listening.", Toast.LENGTH_SHORT).show()
                        Log.d("SpeechRecognizer", "End of Speech detected")
                    }

                    override fun onError(error: Int) {
                        Toast.makeText(context, "Error: $error", Toast.LENGTH_SHORT).show()
                        Log.e("SpeechRecognizer", "Error: $error")
                        restartListening()
                    }

                    override fun onResults(results: Bundle?) {
                        val data = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val recognizedText = data?.firstOrNull().orEmpty()
                        onResult(recognizedText)
                        Log.d("SpeechRecognizer", "Recognized Text: $recognizedText")
                        errorCount = 0 // ✅ Reset error count on success
                        restartListening()
                    }

                    override fun onPartialResults(partialResults: Bundle?) {}
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak Now...")
        }

        Log.d("SpeechRecognizer", "Starting Speech Recognition")
        speechRecognizer?.startListening(intent)
    }

    fun stopListening() {
        if (isListening) {
            speechRecognizer?.cancel()
            isListening = false
            Toast.makeText(context, "Stopped Listening", Toast.LENGTH_SHORT).show()
            Log.d("SpeechRecognizer", "Speech Recognizer Stopped")
            destroy()
        }
    }

    private fun restartListening() {
        if (errorCount >= maxErrorRetries) {
            Log.e("SpeechRecognizer", "Too many errors, stopping restart")
            return
        }
        speechRecognizer?.cancel()
        errorCount++
        Handler(Looper.getMainLooper()).postDelayed({ startListening() }, 1000) // Longer delay
    }

    fun destroy() {
        speechRecognizer?.destroy()
        speechRecognizer = null
        Log.d("SpeechRecognizer", "Speech Recognizer Destroyed")
    }
}

