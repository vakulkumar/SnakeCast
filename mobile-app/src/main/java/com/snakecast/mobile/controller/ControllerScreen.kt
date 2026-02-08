package com.snakecast.mobile.controller

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snakecast.mobile.ui.components.DPadView
import com.snakecast.mobile.ui.components.SettingsPanel
import com.snakecast.mobile.ui.theme.AccentBlue
import com.snakecast.mobile.ui.theme.BackgroundDark
import com.snakecast.mobile.ui.theme.CardDark
import com.snakecast.mobile.ui.theme.ConnectionGreen
import com.snakecast.mobile.ui.theme.ConnectionRed
import com.snakecast.mobile.ui.theme.SnakeGreen
import com.snakecast.mobile.ui.theme.TextGray
import com.snakecast.mobile.ui.theme.TextWhite
import com.snakecast.shared.ConnectionState
import com.snakecast.shared.SocketClient
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Main controller screen with D-Pad and motion controls.
 */
@Composable
fun ControllerScreen(
    socketClient: SocketClient,
    onDisconnect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val motionSensorManager = remember { MotionSensorManager(context) }
    val inputManager = remember { InputManager(socketClient) }
    
    val connectionState by socketClient.connectionState.collectAsState()
    val controlMode by inputManager.controlMode.collectAsState()
    val lastDirection by inputManager.lastDirection.collectAsState()
    
    // Handle disconnection
    LaunchedEffect(connectionState) {
        if (connectionState is ConnectionState.Disconnected) {
            onDisconnect()
        }
    }
    
    // Motion controls
    LaunchedEffect(controlMode) {
        if (controlMode == ControlMode.MOTION) {
            motionSensorManager.startListening()
                .onEach { direction -> inputManager.sendDirection(direction) }
                .launchIn(this)
        }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            inputManager.cleanup()
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Text(
            text = "SnakeCast",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = SnakeGreen
        )
        
        // Connection status
        ConnectionStatus(connectionState)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Settings panel
        SettingsPanel(
            currentMode = controlMode,
            onModeChanged = { inputManager.setControlMode(it) },
            isMotionAvailable = motionSensorManager.isAvailable()
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Controller area
        when (controlMode) {
            ControlMode.JOYSTICK -> {
                DPadView(
                    onDirectionPressed = { direction ->
                        inputManager.sendDirection(direction)
                    },
                    activeDirection = lastDirection
                )
            }
            ControlMode.MOTION -> {
                MotionControlView(lastDirection = lastDirection)
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Disconnect button
        TextButton(
            onClick = {
                socketClient.disconnect()
            }
        ) {
            Text(
                text = "Disconnect",
                color = ConnectionRed,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun ConnectionStatus(connectionState: ConnectionState) {
    val (color, text) = when (connectionState) {
        is ConnectionState.Connected -> ConnectionGreen to "Connected to TV"
        is ConnectionState.Connecting -> AccentBlue to "Connecting..."
        is ConnectionState.Error -> ConnectionRed to "Connection Error"
        is ConnectionState.Disconnected -> ConnectionRed to "Disconnected"
    }
    
    Text(
        text = text,
        fontSize = 14.sp,
        color = color
    )
}

@Composable
private fun MotionControlView(
    lastDirection: com.snakecast.shared.Direction?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardDark)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "📱",
            fontSize = 64.sp
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Motion Control Active",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextWhite
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Tilt your phone to move the snake",
            fontSize = 14.sp,
            color = TextGray
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Current direction indicator
        Text(
            text = when (lastDirection) {
                com.snakecast.shared.Direction.UP -> "⬆️ UP"
                com.snakecast.shared.Direction.DOWN -> "⬇️ DOWN"
                com.snakecast.shared.Direction.LEFT -> "⬅️ LEFT"
                com.snakecast.shared.Direction.RIGHT -> "➡️ RIGHT"
                null -> "—"
            },
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = AccentBlue
        )
    }
}
