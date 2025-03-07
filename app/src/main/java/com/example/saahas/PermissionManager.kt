package com.example.saahas

import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

class PermissionManager(
    private val activity: MainActivity,
    private val onPermissionResult: (Boolean) -> Unit = {}) {

    private val permissionLauncher = activity
        .registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
            val allGranted = results.values.all { it }
            onPermissionResult(allGranted)

            results.entries.forEach { (permission, granted) ->
                if (!granted) {
                    Toast.makeText(activity, "Permission denied: $permission", Toast.LENGTH_SHORT).show()
                }
            }
        }

    fun checkAndRequestPermission(vararg permissions: String) {
        val requiredPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED
        }

        if(requiredPermissions.isNotEmpty()) {
            permissionLauncher.launch(requiredPermissions.toTypedArray())
        }
    }

    fun isPermissionGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED
    }
}
