package com.example.saahas

import android.content.Intent
import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService

class SOSListenerService : WearableListenerService() {
    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (messageEvent.path == "/sos") {
            Log.d("Wearable", "SOS Message Received!")
            triggerSOS()
        }
    }

    private fun triggerSOS() {
        val intent = Intent(this, SOS_SCREEN::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
    }
}
