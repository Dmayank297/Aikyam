package com.example.saahas.Accessibility.Services

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.telephony.SmsManager
import android.widget.Toast
import androidx.core.app.ActivityCompat

class SendSMSService : Service() {

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val phoneNumber = intent.getStringExtra("phoneNumber")
        val message = intent.getStringExtra("message")

        if (phoneNumber != null && message != null) {
            // Check if SEND_SMS permission is granted
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.SEND_SMS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // Send the SMS
                try {
                    val smsManager = SmsManager.getDefault()
                    smsManager.sendTextMessage(phoneNumber, null, message, null, null)
                    showToast("SOS Message Sent!")
                } catch (e: Exception) {
                    showToast("Failed to send SOS message.")
                    e.printStackTrace()
                }
            } else {
                showToast("SEND_SMS permission not granted.")
            }
        } else {
            showToast("Invalid phone number or message.")
        }

        return START_NOT_STICKY
    }

    private fun showToast(message: String) {
        // Use application context to show a Toast safely
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
