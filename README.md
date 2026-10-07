# AuthCares — Wear OS Sensor Monitor

App para Samsung Galaxy Watch FE (Wear OS) que captura datos biométricos en tiempo real y los transmite a Firebase Realtime Database para su visualización en el dashboard web.

**Dashboard web:** configura la visualización con tu propio proyecto Firebase.

---

## Estado actual del proyecto

| Funcionalidad | Estado |
|---|---|
| Conexión con Firebase Realtime Database | ✅ Implementado |
| Lectura de acelerómetro | ✅ Implementado |
| Lectura de giroscopio | ✅ Implementado |
| Lectura de ritmo cardíaco | ✅ Implementado |
| Envío en tiempo real a Firebase | ✅ Implementado |
| Historial con `push()` para ML | 🔲 Pendiente (Paso 3) |
| Autenticación multi-usuario | 🔲 Pendiente (Paso 4) |
| UX circular para Wear OS | 🔲 Pendiente (Paso 5) |
| Exportación de historial para ML | 🔲 Pendiente (Paso 6) |

---

## Requisitos previos

- Android Studio Hedgehog (2023.1.1) o superior
- SDK de Wear OS (API 30 mínimo)
- Dispositivo físico: Samsung Galaxy Watch FE con Wear OS 3.0+
  - ⚠️ El emulador no tiene sensores reales; usar el reloj físico para pruebas completas
- Cuenta Firebase con acceso al proyecto `TU_PROYECTO_FIREBASE`

---

## Estructura del proyecto

```
app/
├── src/main/
│   ├── java/com/authcares/wear/
│   │   ├── MainActivity.kt       # Activity principal, botón de inicio/parada
│   │   ├── SensorReader.kt       # Lee acelerómetro, giroscopio y BPM
│   │   └── SensorData.kt         # Modelo de datos + conversión a mapa Firebase
│   ├── res/layout/
│   │   └── activity_main.xml     # UI básica con estado y valores de sensores
│   └── AndroidManifest.xml       # Permisos INTERNET, BODY_SENSORS
└── google-services.json          # ← NO incluido en el repo (ver configuración)
```

---

## Configuración inicial (setup desde cero)

### 1. Clonar el repositorio

```bash
git clone https://github.com/BorixQ/AuthCaresLogger.git
cd AuthCaresLogger
```

### 2. Agregar google-services.json

Este archivo **no está en el repositorio** por seguridad. Debes descargarlo manualmente:

1. Ve a [Firebase Console](https://console.firebase.google.com) → proyecto `TU_PROYECTO_FIREBASE`
2. **Project Settings → General → Your apps**
3. Descarga `google-services.json`
4. Colócalo en `app/google-services.json`

```
AuthCaresLogger/
└── app/
    └── google-services.json   ← va aquí
```

### 3. Sincronizar dependencias

En Android Studio: **File → Sync Project with Gradle Files**

O desde terminal:
```bash
./gradlew dependencies
```

### 4. Compilar y desplegar en el reloj

Conecta el Galaxy Watch FE por ADB (Wi-Fi o USB) y ejecuta:
```bash
./gradlew installDebug
```

---

## Dependencias principales

Definidas en `app/build.gradle.kts`:

```kotlin
// Firebase
implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
implementation("com.google.firebase:firebase-database-ktx")

// Wear OS
implementation("androidx.wear:wear:1.3.0")

// Coroutines
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.0")
```

Plugin de Google Services en `build.gradle.kts` (raíz):
```kotlin
id("com.google.gms.google-services") version "4.4.2" apply false
```

---

## Permisos requeridos

Declarados en `AndroidManifest.xml`:

| Permiso | Motivo |
|---|---|
| `INTERNET` | Comunicación con Firebase |
| `ACCESS_NETWORK_STATE` | Verificar conectividad |
| `BODY_SENSORS` | Leer ritmo cardíaco (solicita aprobación al usuario) |

---

## Estructura de datos en Firebase

Los datos se escriben en tiempo real en:

```
sensor_data/
  DEVICE_ID/
    accelerometer:
      x: 0.123
      y: 9.81
      z: 0.456
    gyroscope:
      x: 0.001
      y: 0.002
      z: 0.003
    heart_rate:
      value: 72
      timestamp: 1764805106116
```

**Nodo leído por el dashboard web:** `sensor_data/DEVICE_ID`

> ⚠️ Actualmente el ID de dispositivo (`DEVICE_ID`) está hardcodeado. La arquitectura multi-usuario se implementará en el Paso 4.

---

## Uso de la app

1. Abre la app en el Galaxy Watch FE
2. La app solicita permiso **BODY_SENSORS** en el primer uso — acéptalo
3. Presiona **▶ Iniciar** para comenzar la captura
4. Los valores de acelerómetro, giroscopio y BPM se actualizan en pantalla
5. Cada segundo se envía un snapshot a Firebase
6. Presiona **⏹ Detener** para pausar el envío

---

## Throttle de envío

Para no saturar Firebase ni la cuota gratuita, los datos se envían con un intervalo mínimo de **1 segundo** entre escrituras, independientemente de la frecuencia de los sensores.

Configurable en `MainActivity.kt`:
```kotlin
private val SEND_INTERVAL_MS = 1000L  // milisegundos entre envíos
```

---

## Roadmap

```
Paso 3 — Historial con push() (datos para ML)
         ↓
Paso 4 — Firebase Auth (multi-usuario)
         ↓
Paso 5 — UX circular para Wear OS
         ↓
Paso 6 — Exportación de historial a CSV/JSON para entrenamiento
```

---

## Proyectos relacionados

- **Queñaris** — pipeline geoespacial para selección de sitios de reforestación con Polylepis
  [github.com/BorixQ/QuenarisDA](https://github.com/BorixQ/QuenarisDA)
- **AuthCares — ESP32** — firmware de sensores con comunicación MQTT.
  [github.com/BorixQ/AuthCares-ESP32](https://github.com/BorixQ/AuthCares-ESP32)

---

## Autor

**Borix** — Físico, Data Scientist & Backend Developer
Arequipa, Perú
