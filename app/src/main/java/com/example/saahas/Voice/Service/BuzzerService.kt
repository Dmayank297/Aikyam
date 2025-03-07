package com.example.saahas.Voice.Service

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.util.Log
import kotlin.math.sin

class BuzzerService {
    private val sampleRate1 = 44100 // High-quality sample rate
    private val frequency1 = 2500.0

    private var audioTrack: AudioTrack? = null
    private val sampleRate = 44100 // High-quality sample rate
    private val frequency = 1000.0 // Audible frequency (1 kHz)
    private var isBuzzing = false
    private var buzzerThread: Thread? = null

    fun startBuzzer() {
        stopBuzzer()
        Log.d("BuzzerService", "Starting buzzer")

        val bufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        audioTrack = AudioTrack(
            AudioManager.STREAM_MUSIC,
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize,
            AudioTrack.MODE_STREAM
        )

        isBuzzing = true
        buzzerThread = Thread {
            audioTrack?.play()
            val shortDuration = 100 // 100 ms for short beep
            val longDuration = 300 // 300 ms for long beep
            val pauseDuration = 100 // 100 ms pause
            val shortSamples = (sampleRate * shortDuration / 1000).toInt()
            val longSamples = (sampleRate * longDuration / 1000).toInt()
            val pauseSamples = (sampleRate * pauseDuration / 1000).toInt()
            val shortBuffer = ShortArray(shortSamples)
            val longBuffer = ShortArray(longSamples)
            val pauseBuffer = ShortArray(pauseSamples) { 0 } // Silent buffer
            var phase = 0.0
            val increment = 2 * Math.PI * frequency / sampleRate

            // Generate short beep
            for (i in shortBuffer.indices) {
                shortBuffer[i] = (Short.MAX_VALUE * sin(phase)).toInt().toShort()
                phase += increment
            }
            // Generate long beep
            for (i in longBuffer.indices) {
                longBuffer[i] = (Short.MAX_VALUE * sin(phase)).toInt().toShort()
                phase += increment
            }

            while (isBuzzing) {
                // Pattern: .._..
                audioTrack?.write(shortBuffer, 0, shortBuffer.size) // .
                audioTrack?.write(pauseBuffer, 0, pauseBuffer.size) // pause
                audioTrack?.write(shortBuffer, 0, shortBuffer.size) // .
                audioTrack?.write(pauseBuffer, 0, pauseBuffer.size) // pause
                audioTrack?.write(longBuffer, 0, longBuffer.size)   // _
                audioTrack?.write(pauseBuffer, 0, pauseBuffer.size) // pause
                audioTrack?.write(shortBuffer, 0, shortBuffer.size) // .
                audioTrack?.write(pauseBuffer, 0, pauseBuffer.size) // pause
                audioTrack?.write(shortBuffer, 0, shortBuffer.size) // .
                audioTrack?.write(pauseBuffer, 0, pauseBuffer.size) // pause
            }
            Log.d("BuzzerService", "Buzzer loop stopped")
        }
        buzzerThread?.start()
    }

    fun stopBuzzer() {
        isBuzzing = false
        buzzerThread?.join() // Wait for thread to finish
        audioTrack?.stop()
        audioTrack?.release()
        audioTrack = null
        buzzerThread = null
        Log.d("BuzzerService", "Buzzer stopped")
    }
}