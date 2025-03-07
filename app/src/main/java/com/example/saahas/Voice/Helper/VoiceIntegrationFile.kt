package com.example.saahas.Voice.Helper

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.telephony.SmsManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.saahas.R
import com.example.saahas.Voice.Service.BuzzerService
import com.example.saahas.ui.Screens.Location.LocationViewModel
import com.google.api.gax.rpc.ApiStreamObserver
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.speech.v1.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.InputStream

class VoiceIntegrationFile(
    private val context: Context,
    private val buzzerService: BuzzerService,
    private val locationViewModel: LocationViewModel
) {

    private lateinit var speechClient: SpeechClient
    private var selectedLanguage: String = "en-us"
    private var isListening = false
    private var requestObserver: ApiStreamObserver<StreamingRecognizeRequest>? = null
    private val smsHandler = Handler(Looper.getMainLooper())
    private var isSendingSos = false
    private val coroutineScope = CoroutineScope(Dispatchers.IO) // Coroutine scope for IO tasks

    init {
        setupSpeechClient()
    }

    private fun setupSpeechClient() {
        try {
            val inputStream: InputStream = context.resources.openRawResource(R.raw.saahas_key_file)
            val credentials = GoogleCredentials.fromStream(inputStream)
            val speechSettings = SpeechSettings.newBuilder()
                .setCredentialsProvider { credentials }
                .build()
            speechClient = SpeechClient.create(speechSettings)
            Log.d("VoiceIntegration", "SpeechClient initialized successfully")
        } catch (e: Exception) {
            Log.e("VoiceIntegration", "Error initializing SpeechClient: ${e.message}")
            e.printStackTrace()
        }
    }

    fun startListening() {
        if (!::speechClient.isInitialized) {
            Log.e("VoiceIntegration", "SpeechClient not initialized. Aborting listening")
            return
        }
        if (isListening) {
            Log.d("VoiceIntegration", "Already listening, skipping start")
            return
        }
        isListening = true

        try {
            val responseObserver = object : ApiStreamObserver<StreamingRecognizeResponse> {
                override fun onNext(response: StreamingRecognizeResponse) {
                    response.resultsList.forEach { result ->
                        val transcript = result.alternativesList[0].transcript
                        processCommand(transcript)
                        Log.d("VoiceIntegration", "Recognized: $transcript")
                    }
                }

                override fun onError(t: Throwable) {
                    Log.e("VoiceIntegration", "Speech API Error: ${t.message}")
                    t.printStackTrace()
                    restartListening()
                }

                override fun onCompleted() {
                    Log.d("VoiceIntegration", "Voice recognition completed.")
                    restartListening()
                }
            }

            requestObserver = speechClient.streamingRecognizeCallable().bidiStreamingCall(responseObserver)

            val request = StreamingRecognizeRequest.newBuilder()
                .setStreamingConfig(
                    StreamingRecognitionConfig.newBuilder()
                        .setConfig(
                            RecognitionConfig.newBuilder()
                                .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                                .setSampleRateHertz(16000)
                                .setLanguageCode(selectedLanguage)
                                .build()
                        )
                        .setInterimResults(true)
                        .setSingleUtterance(false)
                        .build()
                )
                .build()

            requestObserver?.onNext(request)
            Log.d("VoiceIntegration", "Started listening with language: $selectedLanguage")
        } catch (e: Exception) {
            Log.e("VoiceIntegration", "Error starting voice recognition: ${e.message}")
            e.printStackTrace()
            isListening = false
        }
    }

    fun processCommand(transcript: String) {
        when {
            transcript.contains("start buzzer", ignoreCase = true) -> buzzerService.startBuzzer()
            transcript.contains("Help Help", ignoreCase = true) -> buzzerService.startBuzzer()
            transcript.contains("Please Help", ignoreCase = true) -> buzzerService.startBuzzer()
            transcript.contains("Please Save me!", ignoreCase = true) -> buzzerService.startBuzzer()
            transcript.contains("stop buzzer", ignoreCase = true) -> buzzerService.stopBuzzer()
            transcript.contains("Stop", ignoreCase = true) -> buzzerService.stopBuzzer()
            transcript.contains("change language to Hindi", ignoreCase = true) -> changeLanguage("hi-IN")
            transcript.contains("change language to English", ignoreCase = true) -> changeLanguage("en-US")
            transcript.contains("start sending SOS", ignoreCase = true) -> startSendingSos()
            transcript.contains("stop sending SOS", ignoreCase = true) -> stopSendingSos()
            transcript.contains("start live location", ignoreCase = true) -> {
                locationViewModel.startLocationSharing(context)
                Log.d("VoiceIntegration", "Started live location sharing via voice command")
            }
        }
        restartListening()
    }

    private fun startSendingSos() {
        if (!isSendingSos) {
            isSendingSos = true
            coroutineScope.launch {
                sendSosMessage()
                scheduleNextSos()
            }
        }
    }

    private fun stopSendingSos() {
        isSendingSos = false
        smsHandler.removeCallbacksAndMessages(null)
        Log.d("VoiceIntegration", "Stopped sending SOS messages")
    }

    private suspend fun sendSosMessage() {
        try {
            val smsManager = SmsManager.getDefault()
            val contacts = locationViewModel.contactDao.getAllContacts()
            if (contacts.isNotEmpty()) {
                val locationText = locationViewModel.locationLiveData.value?.let {
                    "https://www.google.com/maps?q=${it.latitude},${it.longitude}"
                } ?: "Location unavailable"
                val message = "Emergency! My location: $locationText"
                contacts.forEach { contact ->
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                        smsManager.sendTextMessage(contact.phoneNumber, null, message, null, null)
                        Log.d("VoiceIntegration", "SOS message sent to ${contact.phoneNumber}: $message")
                    } else {
                        Log.w("VoiceIntegration", "SMS permission not granted")
                    }
                }
            } else {
                Log.w("VoiceIntegration", "No contacts available for SOS")
            }
        } catch (e: Exception) {
            Log.e("VoiceIntegration", "Failed to send SOS message: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun scheduleNextSos() {
        if (isSendingSos) {
            smsHandler.postDelayed({
                coroutineScope.launch {
                    sendSosMessage()
                    scheduleNextSos()
                }
            }, 30000) // 30 seconds interval
        }
    }

    fun changeLanguage(languageCode: String) {
        selectedLanguage = languageCode
        restartListening()
    }

    fun stopListening() {
        if (isListening) {
            isListening = false
            requestObserver?.onCompleted()
            Log.d("VoiceIntegration", "Stopped listening")
        }
    }

    private fun restartListening() {
        if (isListening) {
            stopListening()
            Handler(Looper.getMainLooper()).postDelayed({
                if (!::speechClient.isInitialized) {
                    setupSpeechClient()
                }
                startListening()
            }, 500)
        }
    }

    fun cleanup() {
        stopListening()
        if (::speechClient.isInitialized) {
            speechClient.close()
            Log.d("VoiceIntegration", "SpeechClient closed")
        }
    }
}