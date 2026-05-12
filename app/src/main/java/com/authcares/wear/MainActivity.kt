package com.authcares.wear

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.database.FirebaseDatabase

class MainActivity : ComponentActivity() {

    private val TAG = "AuthCares_Main"
    private val PERMISSION_REQUEST_CODE = 100

    // Firebase — nodo correcto
    private val sensorRef = FirebaseDatabase
        .getInstance("https://authcares-12c89-default-rtdb.firebaseio.com/")
        .getReference("sensor_data/dc16c548a32f6521")

    // Lector de sensores
    private lateinit var sensorReader: SensorReader

    // Control de envío
    private var isSending = false

    // Throttle: enviar a Firebase máximo cada 1 segundo
    private var lastSendTime = 0L
    private val SEND_INTERVAL_MS = 1000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val statusText  = findViewById<TextView>(R.id.statusText)
        val sensorText  = findViewById<TextView>(R.id.sensorText)
        val btnToggle   = findViewById<Button>(R.id.btnToggle)

        // Inicializa el lector de sensores
        sensorReader = SensorReader(this) { data ->
            // Este callback llega en el hilo del sensor

            // Actualiza UI en hilo principal
            runOnUiThread {
                sensorText.text = """
                    🏃 Acelerómetro
                    X: ${"%.3f".format(data.accelX)}
                    Y: ${"%.3f".format(data.accelY)}
                    Z: ${"%.3f".format(data.accelZ)}
                    
                    🔄 Giroscopio
                    X: ${"%.4f".format(data.gyroX)}
                    Y: ${"%.4f".format(data.gyroY)}
                    Z: ${"%.4f".format(data.gyroZ)}
                    
                    ❤️ Ritmo cardíaco: ${data.heartRate} BPM
                """.trimIndent()
            }

            // Throttle: no enviar más rápido de 1 vez/segundo
            val now = System.currentTimeMillis()
            if (isSending && (now - lastSendTime) >= SEND_INTERVAL_MS) {
                lastSendTime = now
                sendToFirebase(data, statusText)
            }
        }

        // Botón para iniciar/detener
        btnToggle.setOnClickListener {
            if (!isSending) {
                requestPermissionsAndStart(statusText, btnToggle)
            } else {
                stopSending(statusText, btnToggle)
            }
        }
    }

    private fun requestPermissionsAndStart(statusText: TextView, btnToggle: Button) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS)
            != PackageManager.PERMISSION_GRANTED) {

            // Solicita permiso al usuario
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.BODY_SENSORS),
                PERMISSION_REQUEST_CODE
            )
        } else {
            startSending(statusText, btnToggle)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val statusText = findViewById<TextView>(R.id.statusText)
        val btnToggle  = findViewById<Button>(R.id.btnToggle)

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startSending(statusText, btnToggle)
            } else {
                statusText.text = "⚠️ Permiso BODY_SENSORS denegado.\nNo se puede leer ritmo cardíaco."
                // Inicia de todos modos (sin BPM)
                startSending(statusText, btnToggle)
            }
        }
    }

    private fun startSending(statusText: TextView, btnToggle: Button) {
        isSending = true
        btnToggle.text = "⏹ Detener"
        statusText.text = "📡 Enviando a Firebase..."
        sensorReader.start()
        Log.d(TAG, "Captura iniciada")
    }

    private fun stopSending(statusText: TextView, btnToggle: Button) {
        isSending = false
        btnToggle.text = "▶ Iniciar"
        statusText.text = "⏸ Detenido"
        sensorReader.stop()
        Log.d(TAG, "Captura detenida")
    }

    private fun sendToFirebase(data: SensorData, statusText: TextView) {
        sensorRef.setValue(data.toFirebaseMap())
            .addOnSuccessListener {
                Log.d(TAG, "✅ Datos enviados: BPM=${data.heartRate}")
                runOnUiThread {
                    statusText.text = "✅ Enviando... BPM=${data.heartRate}"
                }
            }
            .addOnFailureListener { error ->
                Log.e(TAG, "❌ Error Firebase: ${error.message}")
                runOnUiThread {
                    statusText.text = "❌ Error: ${error.message}"
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorReader.stop()
    }
}