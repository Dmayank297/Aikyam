package com.example.saahas.Utils.Audio

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File
import java.io.IOException

class AudioRecorder(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var startTime: Long = 0L

    fun startRecording(): String? {
        try {
            outputFile = File(context.cacheDir, "recording_${System.currentTimeMillis()}.mp3")

            recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(outputFile?.absolutePath)
                prepare()
                start()
            }
            startTime = System.currentTimeMillis()
            return outputFile?.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }

    fun stopRecording(): Pair<String?, Long>? {
        try {
            recorder?.apply {
                stop()
                release()
            }
            val duration = System.currentTimeMillis() - startTime
            recorder = null
            return Pair(outputFile?.absolutePath, duration)
        } catch (e: IllegalStateException) {
            e.printStackTrace()
            return null
        }
    }

    fun isRecording(): Boolean = recorder != null

    fun deleteRecording(filePath: String) {
        val file = File(filePath)
        if (file.exists()) {
            file.delete()
        }
    }
}