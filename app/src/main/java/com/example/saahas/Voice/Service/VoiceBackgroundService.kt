package com.example.saahas.Voice.Service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import ai.picovoice.porcupine.Porcupine
import ai.picovoice.porcupine.PorcupineException
import ai.picovoice.porcupine.PorcupineManager
import com.example.saahas.Accessibility.Model.ScreenStateReceiver
import com.example.saahas.MainActivity
import com.example.saahas.R

class VoiceBackgroundService : Service() {
    private var porcupineManager: PorcupineManager? = null
    private lateinit var voiceCommandService: VoiceCommandService
    private val screenReceiver = ScreenStateReceiver()
    private var isServiceActive = false
    private val buzzerService = BuzzerService() // Initialize here

    override fun onCreate() {
        super.onCreate()
        Log.d("VoiceBackgroundService", "onCreate started")
        createNotificationChannel()
        try {
            val notification = createNotification()
            Log.d("VoiceBackgroundService", "Notification created successfully")
            startForeground(1, notification)
            Log.d("VoiceBackgroundService", "startForeground called")
            registerReceiver(screenReceiver, IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_ON)
                addAction(Intent.ACTION_SCREEN_OFF)
                addAction(Intent.ACTION_USER_PRESENT)
            })
            setupPorcupine()
            voiceCommandService = VoiceCommandService(this) { command ->
                Log.d("VoiceBackgroundService", "Command processed: $command")
                when (command) {
                    "start buzzer", "begin buzz", "help", "help help" -> {
                        buzzerService.startBuzzer()
                        Log.d("VoiceBackgroundService", "Buzzer started")
                    }
                    "stop buzzer", "end buzz", "stop", "stop stop" -> {
                        buzzerService.stopBuzzer()
                        Log.d("VoiceBackgroundService", "Buzzer stopped")
                    }
                    else -> Log.d("VoiceBackgroundService", "Unrecognized command: $command")
                }
            }
            isServiceActive = true
            Log.d("VoiceBackgroundService", "onCreate completed")
        } catch (e: Exception) {
            Log.e("VoiceBackgroundService", "Service creation failed: ${e.message}", e)
            stopSelf()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("VoiceBackgroundService", "onStartCommand called")
        return START_STICKY
    }

    private fun setupPorcupine() {
        Log.d("VoiceBackgroundService", "Setting up Porcupine")
        try {
            Log.d("VoiceBackgroundService", "Using access key: ${"YOUR_ACCESS_KEY".take(5)}...")
            porcupineManager = PorcupineManager.Builder()
                .setAccessKey("")
                .setKeywords(arrayOf(Porcupine.BuiltInKeyword.PORCUPINE, Porcupine.BuiltInKeyword.ALEXA))
                .setSensitivities(floatArrayOf(0.7f, 0.7f))
                .build(this) { wakeWordDetected(it) }
            Log.d("VoiceBackgroundService", "Porcupine initialized successfully")
            if (ScreenStateReceiver.isScreenOn) {
                porcupineManager?.start()
                Log.d("VoiceBackgroundService", "Porcupine started with keywords PORCUPINE and ALEXA")
            } else {
                Log.d("VoiceBackgroundService", "Screen off, waiting to start Porcupine")
            }
        } catch (e: PorcupineException) {
            Log.e("VoiceBackgroundService", "Porcupine setup failed: ${e.message}", e)
            stopSelf()
        }
    }

    private fun wakeWordDetected(keywordIndex: Int) {
        val keyword = when (keywordIndex) {
            0 -> "PORCUPINE"
            1 -> "ALEXA"
            else -> "Unknown"
        }
        Log.d("VoiceBackgroundService", "Wake word detected: $keyword (index: $keywordIndex)")
        porcupineManager?.stop()
        voiceCommandService.startListening()
        android.os.Handler(mainLooper).postDelayed({
            if (ScreenStateReceiver.isScreenOn || isServiceActive) {
                porcupineManager?.start()
                Log.d("VoiceBackgroundService", "Porcupine restarted")
            }
        }, 5000) // Increased to 10 seconds for longer speech window
    }

    private fun createNotification(): Notification {
        Log.d("VoiceBackgroundService", "Creating notification")
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(this, "voice_channel")
            .setContentTitle("Voice Control Active")
            .setContentText("Listening for 'Porcupine' or 'Alexa'")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
        Log.d("VoiceBackgroundService", "Notification built")
        return notification
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "voice_channel",
                "Voice Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Channel for voice control service"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
            Log.d("VoiceBackgroundService", "Notification channel created in service")
        }
    }

    override fun onDestroy() {
        Log.d("VoiceBackgroundService", "onDestroy called")
        isServiceActive = false
        unregisterReceiver(screenReceiver)
        porcupineManager?.let {
            it.stop()
            it.delete()
            Log.d("VoiceBackgroundService", "Porcupine stopped and deleted")
        }
        voiceCommandService.destroy()
        buzzerService.stopBuzzer() // Ensure buzzer stops on destroy
        super.onDestroy()
        Log.d("VoiceBackgroundService", "Service destroyed")
    }

    override fun onBind(intent: Intent?): IBinder? = null
}