package com.authcares.wear

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log

/**
 * Lee acelerómetro, giroscopio y ritmo cardíaco del Galaxy Watch FE.
 * Llama onSensorUpdate cada vez que llegan datos nuevos.
 */
class SensorReader(
    context: Context,
    private val onSensorUpdate: (SensorData) -> Unit
) : SensorEventListener {

    private val TAG = "AuthCares_Sensors"
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    // Sensores
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val gyroscope     = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    private val heartRate     = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)

    // Estado actual (se actualiza con cada evento)
    private var currentData = SensorData()

    fun start() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
            Log.d(TAG, "Acelerómetro registrado")
        } ?: Log.w(TAG, "Acelerómetro no disponible")

        gyroscope?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
            Log.d(TAG, "Giroscopio registrado")
        } ?: Log.w(TAG, "Giroscopio no disponible")

        heartRate?.let {
            // SENSOR_DELAY_NORMAL es suficiente para BPM
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
            Log.d(TAG, "Ritmo cardíaco registrado")
        } ?: Log.w(TAG, "Sensor de ritmo cardíaco no disponible")
    }

    fun stop() {
        sensorManager.unregisterListener(this)
        Log.d(TAG, "Sensores detenidos")
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {

            Sensor.TYPE_ACCELEROMETER -> {
                currentData = currentData.copy(
                    accelX = event.values[0].toDouble(),
                    accelY = event.values[1].toDouble(),
                    accelZ = event.values[2].toDouble()
                )
            }

            Sensor.TYPE_GYROSCOPE -> {
                currentData = currentData.copy(
                    gyroX = event.values[0].toDouble(),
                    gyroY = event.values[1].toDouble(),
                    gyroZ = event.values[2].toDouble()
                )
            }

            Sensor.TYPE_HEART_RATE -> {
                currentData = currentData.copy(
                    heartRate = event.values[0].toInt(),
                    heartRateTimestamp = System.currentTimeMillis()
                )
            }
        }

        // Notifica con los datos actualizados
        onSensorUpdate(currentData)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.d(TAG, "Precisión cambiada: ${sensor?.name} → $accuracy")
    }
}