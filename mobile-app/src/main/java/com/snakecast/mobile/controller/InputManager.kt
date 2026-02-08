package com.snakecast.mobile.controller

import com.snakecast.shared.Direction
import com.snakecast.shared.SocketClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Control mode for the mobile controller.
 */
enum class ControlMode {
    JOYSTICK,  // On-screen D-Pad
    MOTION     // Accelerometer-based tilt
}

/**
 * Input manager that handles sending direction commands to the TV.
 * Supports both joystick and motion control modes.
 */
class InputManager(
    private val socketClient: SocketClient
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    private val _controlMode = MutableStateFlow(ControlMode.JOYSTICK)
    val controlMode: StateFlow<ControlMode> = _controlMode.asStateFlow()
    
    private val _lastDirection = MutableStateFlow<Direction?>(null)
    val lastDirection: StateFlow<Direction?> = _lastDirection.asStateFlow()
    
    /**
     * Send a direction command to the TV.
     */
    fun sendDirection(direction: Direction) {
        _lastDirection.value = direction
        
        scope.launch {
            socketClient.sendCommand(direction)
        }
    }
    
    /**
     * Toggle between control modes.
     */
    fun toggleControlMode() {
        _controlMode.value = when (_controlMode.value) {
            ControlMode.JOYSTICK -> ControlMode.MOTION
            ControlMode.MOTION -> ControlMode.JOYSTICK
        }
    }
    
    /**
     * Set the control mode directly.
     */
    fun setControlMode(mode: ControlMode) {
        _controlMode.value = mode
    }
    
    /**
     * Clean up resources.
     */
    fun cleanup() {
        scope.cancel()
    }
}
