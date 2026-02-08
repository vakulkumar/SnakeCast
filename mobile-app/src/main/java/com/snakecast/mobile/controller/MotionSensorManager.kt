package com.snakecast.mobile.controller

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.snakecast.shared.Direction
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlin.math.abs

/**
 * Motion sensor manager for tilt-based controls.
 * Uses accelerometer to detect device tilt and translate to directions.
 */
class MotionSensorManager(context: Context) {
    
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    
    companion object {
        // Tilt thresholds in m/s² (gravity is ~9.8)
        private const val TILT_THRESHOLD = 2.5f  // ~15° tilt
        private const val DEAD_ZONE = 1.0f       // ~6° dead zone
        private const val DEBOUNCE_MS = 100L     // Minimum time between direction changes
    }
    
    /**
     * Start listening for tilt gestures.
     * Emits Direction when tilt exceeds threshold.
     * Returns empty flow if sensor is not available.
     */
    fun startListening(): Flow<Direction> {
        // Return empty flow if sensor manager or accelerometer is null
        val manager = sensorManager
        val sensor = accelerometer

        if (manager == null || sensor == null) {
            return emptyFlow()
        }
        
        return callbackFlow {
            var lastDirection: Direction? = null
            var lastEmitTime = 0L
            
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    // Accelerometer values:
                    // x: +right, -left
                    // y: +up, -down (when device is vertical)
                    // z: perpendicular to screen
                    
                    if (event.values.size < 2) return

                    val x = event.values[0]  // Tilt left/right
                    val y = event.values[1]  // Tilt forward/backward
                    
                    val currentTime = System.currentTimeMillis()
                    
                    // Determine dominant direction based on tilt
                    val direction = when {
                        // Phone tilted left (x positive when tilted right, negative when tilted left)
                        x > TILT_THRESHOLD && abs(x) > abs(y) -> Direction.LEFT
                        x < -TILT_THRESHOLD && abs(x) > abs(y) -> Direction.RIGHT
                        // Phone tilted forward (y negative when tilted away)
                        y < -TILT_THRESHOLD && abs(y) > abs(x) -> Direction.UP
                        // Phone tilted backward (y positive when tilted toward)
                        y > TILT_THRESHOLD && abs(y) > abs(x) -> Direction.DOWN
                        else -> null
                    }
                    
                    // Only emit if direction changed and debounce period passed
                    direction?.let {
                        if (it != lastDirection && currentTime - lastEmitTime >= DEBOUNCE_MS) {
                            lastDirection = it
                            lastEmitTime = currentTime
                            trySend(it)
                        }
                    }
                }
                
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                    // Not used
                }
            }
            
            manager.registerListener(
                listener,
                sensor,
                SensorManager.SENSOR_DELAY_GAME
            )
            
            awaitClose {
                manager.unregisterListener(listener)
            }
        }.distinctUntilChanged()
    }
    
    /**
     * Check if accelerometer is available.
     */
    fun isAvailable(): Boolean = sensorManager != null && accelerometer != null
}
