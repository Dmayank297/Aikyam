package com.example.saahas.Accessibility.Actions

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.SmsManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices

object PressAction {

    private const val CHANNEL_ID = "sos_channel"
    private const val NOTIFICATION_ID = 1

    fun sendSOSNotification(context: Context, phoneNumber: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "SOS Notifications"
            val descriptionText = "Channel for SOS alerts"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("SOS Alert")
            .setContentText("This is an SOS notification. Help is needed!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            notify(NOTIFICATION_ID, builder.build())
        }

        sendSOSMessage(context, phoneNumber)
    }

    private fun sendSOSMessage(context: Context, phoneNumber: String) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.SEND_SMS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            try {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    fusedLocationClient.lastLocation
                        .addOnSuccessListener { location ->
                            val locationMessage =
                                if (location != null && location.latitude != 0.0 && location.longitude != 0.0) {
                                    "Location: https://www.google.com/maps?q=${location.latitude},${location.longitude}"
                                } else {
                                    "Unable to fetch the accurate location. Please check your GPS settings."
                                }
                            val message = "SOS! I need help! Please respond. $locationMessage"
                            val smsManager = SmsManager.getDefault()
                            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
                            Toast.makeText(context, "SOS Message Sent!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                context,
                                "Failed to retrieve location for SOS message.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                } else {
                    if (context is Activity) {
                        ActivityCompat.requestPermissions(
                            context,
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            2
                        )
                    } else {
                        Toast.makeText(
                            context,
                            "Permission for location is missing.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to send SOS message.", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }
}