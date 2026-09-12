package com.afin19.wirefinder.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.afin19.wirefinder.audio.WireFinderAudioBeeper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Raw point recorded from magnetometer.
 */
data class MagneticSample(
    val timestampMs: Long,
    val magnitude: Float // |B| = sqrt(x^2 + y^2 + z^2) in microTesla (µT)
)

/**
 * Historical point for UI waveform/graph.
 */
data class LevelPoint(
    val timestampMs: Long,
    val level: Float // 0..100 %
)

/**
 * Point for dedicated delta(t) chart.
 * delta: B(t) - baseline(t) in µT
 * envelope: smoothed peak-to-peak amplitude in µT
 */
data class DeltaPoint(
    val timestampMs: Long,
    val delta: Float,
    val envelope: Float
)

/**
 * Current status of sensor and wire finder calculations.
 */
data class WireFinderState(
    val isSensorAvailable: Boolean = true,
    val isScanning: Boolean = false,
    val isCalibrating: Boolean = false,
    val calibrationProgress: Float = 0f, // 0..1
    val isSimulationMode: Boolean = false,
    val hapticEnabled: Boolean = true,
    val soundEnabled: Boolean = true,

    // Real-time metrics
    val rawMagnitude: Float = 0f,       // Current |B| in µT
    val baseline: Float = 0f,           // 1.0s moving average of |B|
    val delta: Float = 0f,              // |B| - baseline
    val signalAmplitude: Float = 0f,    // Peak-to-peak in 0.35s window (µT)
    val displaySignal: Float = 0f,      // Smoothed signal amplitude (µT)
    val noiseFloor: Float = 0.8f,       // Calibrated noise baseline (µT)
    val level: Float = 0f,              // 0..100 %
    val peakLevel: Float = 0f,          // Highest level observed
    val samplingRateHz: Int = 0,        // Estimated sensor update rate

    // Sensitivity multiplier (0.5x to 3.0x)
    val sensitivity: Float = 1.0f,

    // Recent history for level gauge graph (last ~8-10 seconds)
    val history: List<LevelPoint> = emptyList(),

    // Ring buffer of delta(t) for dedicated scrolling chart (last ~4.5 seconds)
    val deltaHistory: List<DeltaPoint> = emptyList()
)

class MagneticSensorProcessor(
    private val context: Context,
    private val scope: CoroutineScope
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val magnetometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private val audioBeeper = WireFinderAudioBeeper(scope)

    private val _state = MutableStateFlow(
        WireFinderState(isSensorAvailable = magnetometer != null)
    )
    val state: StateFlow<WireFinderState> = _state.asStateFlow()

    // Sliding buffers (thread-safe synchronization via private lock or UI loop)
    private val lock = Any()
    private val rawSamples = ArrayDeque<MagneticSample>() // For 1.0s baseline
    private val deltaSamples = ArrayDeque<Pair<Long, Float>>() // For 0.35s window (timestamp, delta)
    private val displaySamples = ArrayDeque<Pair<Long, Float>>() // For 0.18s UI smoothing

    // Calibration buffer
    private val calibrationSamples = mutableListOf<Float>()
    private var calibrationStartTime = 0L
    private val calibrationDurationMs = 2500L

    // Sampling rate calculation
    private var sampleCountInSecond = 0
    private var lastRateCalculationTime = 0L
    private var currentSamplingHz = 0

    // History of level for the live graph
    private val historyPoints = ArrayDeque<LevelPoint>()
    private var lastHistoryRecordTime = 0L

    // Dedicated ring buffer of delta(t) for the scrolling chart (last ~4.5 seconds)
    private val deltaHistoryPoints = ArrayDeque<DeltaPoint>()
    private var lastDeltaSnapshotTime = 0L
    private var cachedDeltaSnapshot: List<DeltaPoint> = emptyList()

    // Peak tracking
    private var maxPeakLevel = 0f

    // Simulation job for devices without magnetometer
    private var simulationJob: Job? = null

    // Haptics pacing
    private var lastVibrationTime = 0L

    fun start() {
        if (_state.value.isScanning) return

        val hasSensor = magnetometer != null
        _state.value = _state.value.copy(
            isScanning = true,
            isSensorAvailable = hasSensor,
            isSimulationMode = !hasSensor
        )

        if (hasSensor) {
            registerMagnetometerListener()
        } else {
            startSimulation()
        }
        audioBeeper.isEnabled = _state.value.soundEnabled
        audioBeeper.start()
    }

    private fun registerMagnetometerListener() {
        if (magnetometer == null) return
        try {
            sensorManager.registerListener(
                this,
                magnetometer,
                SensorManager.SENSOR_DELAY_FASTEST
            )
        } catch (_: SecurityException) {
            try {
                sensorManager.registerListener(
                    this,
                    magnetometer,
                    SensorManager.SENSOR_DELAY_GAME
                )
            } catch (_: Exception) {
                startSimulation()
            }
        }
    }

    fun stop() {
        audioBeeper.stop()
        sensorManager.unregisterListener(this)
        simulationJob?.cancel()
        simulationJob = null
        _state.value = _state.value.copy(isScanning = false)
    }

    fun toggleSimulation() {
        val newSimMode = !_state.value.isSimulationMode
        if (newSimMode) {
            sensorManager.unregisterListener(this)
            startSimulation()
        } else {
            simulationJob?.cancel()
            simulationJob = null
            if (magnetometer != null) {
                registerMagnetometerListener()
            }
        }
        _state.value = _state.value.copy(isSimulationMode = newSimMode)
    }

    fun setSensitivity(value: Float) {
        val clamped = value.coerceIn(0.5f, 3.0f)
        _state.value = _state.value.copy(sensitivity = clamped)
    }

    fun setHapticEnabled(enabled: Boolean) {
        _state.value = _state.value.copy(hapticEnabled = enabled)
    }

    fun setSoundEnabled(enabled: Boolean) {
        audioBeeper.isEnabled = enabled
        _state.value = _state.value.copy(soundEnabled = enabled)
    }

    fun resetPeak() {
        synchronized(lock) {
            maxPeakLevel = 0f
            _state.value = _state.value.copy(peakLevel = 0f)
        }
    }

    fun startCalibration() {
        synchronized(lock) {
            calibrationSamples.clear()
            calibrationStartTime = SystemClock.elapsedRealtime()
            _state.value = _state.value.copy(
                isCalibrating = true,
                calibrationProgress = 0f
            )
        }
    }

    fun dismissCalibration() {
        synchronized(lock) {
            _state.value = _state.value.copy(
                isCalibrating = false,
                calibrationProgress = 0f
            )
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_MAGNETIC_FIELD) return
        val now = SystemClock.elapsedRealtime()

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        val magnitude = sqrt(x * x + y * y + z * z)

        processSample(magnitude, now)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }

    private fun processSample(magnitude: Float, now: Long) {
        var newLevel = 0f
        var newPeak = 0f
        var currentSignal = 0f
        var currentDisplay = 0f
        var currentBaseline = 0f
        var currentDelta = 0f
        var historySnapshot: List<LevelPoint> = emptyList()
        var calibProgress = 0f
        var isCalibDone = false
        var calculatedNoiseFloor = _state.value.noiseFloor

        synchronized(lock) {
            // Rate tracking
            sampleCountInSecond++
            if (now - lastRateCalculationTime >= 1000L) {
                currentSamplingHz = sampleCountInSecond
                sampleCountInSecond = 0
                lastRateCalculationTime = now
            }

            // 1. СГЛАЖИВАНИЕ ТРЕНДА (baseline) за последние ~1.0 сек (1000 ms)
            rawSamples.addLast(MagneticSample(now, magnitude))
            while (rawSamples.isNotEmpty() && now - rawSamples.first().timestampMs > 1000L) {
                rawSamples.removeFirst()
            }

            var sumB = 0.0
            for (sample in rawSamples) {
                sumB += sample.magnitude
            }
            currentBaseline = if (rawSamples.isNotEmpty()) (sumB / rawSamples.size).toFloat() else magnitude

            // 2. ВЫДЕЛЕНИЕ ПЕРЕМЕННОЙ СОСТАВЛЯЮЩЕЙ: delta(t) = B(t) - baseline(t)
            currentDelta = magnitude - currentBaseline

            // 3. ОЦЕНКА АМПЛИТУДЫ КОЛЕБАНИЙ в коротком скользящем окне W (0.35 сек)
            deltaSamples.addLast(Pair(now, currentDelta))
            while (deltaSamples.isNotEmpty() && now - deltaSamples.first().first > 350L) {
                deltaSamples.removeFirst()
            }

            var minDelta = Float.MAX_VALUE
            var maxDelta = -Float.MAX_VALUE
            for (pair in deltaSamples) {
                if (pair.second < minDelta) minDelta = pair.second
                if (pair.second > maxDelta) maxDelta = pair.second
            }
            currentSignal = if (deltaSamples.size >= 2) (maxDelta - minDelta) else 0f

            // 4. СГЛАЖИВАНИЕ ИНДИКАТОРА ДЛЯ UI (скользящее среднее signal за ~0.18 сек)
            displaySamples.addLast(Pair(now, currentSignal))
            while (displaySamples.isNotEmpty() && now - displaySamples.first().first > 180L) {
                displaySamples.removeFirst()
            }

            var sumDisplay = 0f
            for (pair in displaySamples) {
                sumDisplay += pair.second
            }
            currentDisplay = if (displaySamples.isNotEmpty()) (sumDisplay / displaySamples.size) else currentSignal

            // Calibration processing
            if (_state.value.isCalibrating) {
                val elapsed = now - calibrationStartTime
                calibProgress = (elapsed.toFloat() / calibrationDurationMs).coerceIn(0f, 1f)
                calibrationSamples.add(currentSignal)

                if (elapsed >= calibrationDurationMs) {
                    isCalibDone = true
                    val avgNoise = if (calibrationSamples.isNotEmpty()) {
                        calibrationSamples.average().toFloat()
                    } else {
                        0.8f
                    }
                    calculatedNoiseFloor = avgNoise.coerceAtLeast(0.3f)
                }
            }

            // 5. НОРМАЛИЗАЦИЯ ОТНОСИТЕЛЬНО ШУМА
            // level(t) = (display(t) - noise_floor) / scale
            val effectiveNoise = if (isCalibDone) calculatedNoiseFloor else _state.value.noiseFloor
            val excessSignal = (currentDisplay - effectiveNoise).coerceAtLeast(0f)

            // Dynamic scale factoring in user sensitivity setting
            // Standard full range span is ~6.0 µT above noise for typical wires behind drywall
            val baseSpan = 6.0f / _state.value.sensitivity
            newLevel = ((excessSignal / baseSpan) * 100f).coerceIn(0f, 100f)

            // Peak tracking
            if (newLevel > maxPeakLevel) {
                maxPeakLevel = newLevel
            }
            newPeak = maxPeakLevel

            // Update waveform history (~every 60ms)
            if (now - lastHistoryRecordTime >= 60L) {
                lastHistoryRecordTime = now
                historyPoints.addLast(LevelPoint(now, newLevel))
                while (historyPoints.isNotEmpty() && now - historyPoints.first().timestampMs > 8000L) {
                    historyPoints.removeFirst()
                }
            }
            historySnapshot = historyPoints.toList()

            // Update dedicated delta(t) ring buffer (window ~4.5 seconds)
            deltaHistoryPoints.addLast(DeltaPoint(now, currentDelta, currentDisplay))
            while (deltaHistoryPoints.isNotEmpty() && now - deltaHistoryPoints.first().timestampMs > 4500L) {
                deltaHistoryPoints.removeFirst()
            }

            // Snapshot delta history at ~30 Hz (every 33ms) for efficient UI recomposition
            if (now - lastDeltaSnapshotTime >= 33L || cachedDeltaSnapshot.isEmpty()) {
                lastDeltaSnapshotTime = now
                cachedDeltaSnapshot = deltaHistoryPoints.toList()
            }
        }

        // Haptic feedback when level is elevated
        if (_state.value.hapticEnabled && newLevel >= 30f) {
            triggerHapticFeedback(newLevel, now)
        }

        // Acoustic audio feedback
        audioBeeper.updateLevel(newLevel)

        _state.value = _state.value.copy(
            rawMagnitude = magnitude,
            baseline = currentBaseline,
            delta = currentDelta,
            signalAmplitude = currentSignal,
            displaySignal = currentDisplay,
            level = newLevel,
            peakLevel = newPeak,
            samplingRateHz = currentSamplingHz,
            noiseFloor = calculatedNoiseFloor,
            isCalibrating = if (isCalibDone) false else _state.value.isCalibrating,
            calibrationProgress = calibProgress,
            history = historySnapshot,
            deltaHistory = cachedDeltaSnapshot
        )
    }

    private fun triggerHapticFeedback(level: Float, now: Long) {
        // Geiger-counter-like pacing: pulse interval 600ms at 30% down to 80ms at 100%
        val intervalMs = (600f - ((level - 30f) / 70f * 520f)).coerceIn(80f, 600f).toLong()
        if (now - lastVibrationTime >= intervalMs) {
            lastVibrationTime = now
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val duration = if (level > 75f) 40L else 20L
                    val amplitude = if (level > 75f) 255 else 160
                    vibrator?.vibrate(VibrationEffect.createOneShot(duration, amplitude))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(25L)
                }
            } catch (_: Exception) {
                // Ignore vibration errors if permission or hardware fails
            }
        }
    }

    /**
     * Physics simulation for emulators or testing without physical live wires.
     * Simulates 50Hz AC current aliasing (~3.5 Hz slow beating envelope) on Earth magnetic field.
     */
    private fun startSimulation() {
        simulationJob?.cancel()
        simulationJob = scope.launch(Dispatchers.Default) {
            var simTime = 0.0
            val earthField = 48.5f // µT typical Earth magnetic field
            var simulatedProximity = 0.0f // 0 to 1

            while (isActive) {
                simTime += 0.015 // ~66 Hz sample interval
                val now = SystemClock.elapsedRealtime()

                // Oscillate proximity slowly or simulate user sweeping across a wire
                val sweepPhase = (simTime * 0.4) % (2 * Math.PI)
                simulatedProximity = (sin(sweepPhase).toFloat().coerceAtLeast(0f)).let { it * it }

                // Aliasing envelope (beating at ~3.2 Hz as described in physical principle)
                val aliasingBeating = sin(simTime * 2.0 * Math.PI * 3.2).toFloat()
                val wireFieldFluctuation = simulatedProximity * (4.5f * aliasingBeating)

                // Add small random noise floor
                val sensorNoise = ((Math.random() - 0.5) * 0.4).toFloat()

                val simulatedMagnitude = earthField + wireFieldFluctuation + sensorNoise
                processSample(simulatedMagnitude, now)

                delay(15) // ~66 Hz
            }
        }
    }
}
