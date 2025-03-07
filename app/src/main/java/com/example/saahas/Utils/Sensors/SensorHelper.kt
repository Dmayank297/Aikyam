package com.example.saahas.Utils.Sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

class SensorHelper(context: Context, private val callback: SensorCallback) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private var gyroscope: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    private var lastAcceleration = 0f
    private var shakeThreshold = 12f  // Adjust based on testing

    fun startSensors() {
        accelerometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
        gyroscope?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
    }

    fun stopSensors() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> detectShake(event)
            Sensor.TYPE_GYROSCOPE -> detectRotation(event)
        }
    }

    private fun detectShake(event: SensorEvent) {
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        val acceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
        if (acceleration - lastAcceleration > shakeThreshold) {
            callback.onShakeDetected()
        }
        lastAcceleration = acceleration
    }

    private fun detectRotation(event: SensorEvent) {
        val rotationX = event.values[0]
        val rotationY = event.values[1]
        val rotationZ = event.values[2]

        if (rotationX > 2 || rotationY > 2 || rotationZ > 2) {  // Adjust threshold as needed
            callback.onRotationDetected(rotationX, rotationY, rotationZ)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    interface SensorCallback {
        fun onShakeDetected()
        fun onRotationDetected(x: Float, y: Float, z: Float)
    }
}
