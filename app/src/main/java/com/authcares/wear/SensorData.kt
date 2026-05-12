package com.authcares.wear

/**
 * Modelo que representa el estado actual de todos los sensores.
 * Usa valores por defecto para evitar nulls.
 */
data class SensorData(
    val accelX: Double = 0.0,
    val accelY: Double = 0.0,
    val accelZ: Double = 0.0,
    val gyroX: Double  = 0.0,
    val gyroY: Double  = 0.0,
    val gyroZ: Double  = 0.0,
    val heartRate: Int = 0,
    val heartRateTimestamp: Long = 0L
)

/**
 * Convierte SensorData al mapa exacto que espera Firebase
 * en el nodo: sensor_data/dc16c548a32f6521
 */
fun SensorData.toFirebaseMap(): Map<String, Any> = mapOf(
    "accelerometer" to mapOf(
        "x" to accelX,
        "y" to accelY,
        "z" to accelZ
    ),
    "gyroscope" to mapOf(
        "x" to gyroX,
        "y" to gyroY,
        "z" to gyroZ
    ),
    "heart_rate" to mapOf(
        "value"     to heartRate,
        "timestamp" to heartRateTimestamp
    )
)