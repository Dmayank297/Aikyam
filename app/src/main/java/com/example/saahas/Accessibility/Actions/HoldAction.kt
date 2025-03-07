package com.example.saahas.Accessibility.Actions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object HoldAction {

    private const val REQUEST_CALL_PERMISSION = 1

    fun makeCall(context: Context, phoneNumber: String) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            if (context is Activity) {
                ActivityCompat.requestPermissions(
                    context,
                    arrayOf(Manifest.permission.CALL_PHONE),
                    REQUEST_CALL_PERMISSION
                )
            }
        } else {
            initiateCall(context, phoneNumber)
        }
    }

    private fun initiateCall(context: Context, phoneNumber: String) {
        try {
            val callIntent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:$phoneNumber")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            Log.d("HoldAction", "Calling number: $phoneNumber")
            context.startActivity(callIntent)
        } catch (e: SecurityException) {
            Log.e("HoldAction", "SecurityException: ${e.message}")
            Toast.makeText(context, "Call permission not granted.", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("HoldAction", "Exception: ${e.message}")
            Toast.makeText(context, "Unable to make the call.", Toast.LENGTH_SHORT).show()
        }
    }
}