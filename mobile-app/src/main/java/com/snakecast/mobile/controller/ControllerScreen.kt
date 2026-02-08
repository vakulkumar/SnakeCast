package com.snakecast.mobile.controller

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snakecast.mobile.ui.components.DPadView
import com.snakecast.mobile.ui.components.SettingsPanel
import com.snakecast.mobile.ui.theme.AccentBlue
import com.snakecast.mobile.ui.theme.CardDark
import com.snakecast.mobile.ui.theme.ConnectionGreen
import com.snakecast.mobile.ui.theme.ConnectionRed
import com.snakecast.mobile.ui.theme.SnakeGreen
import com.snakecast.mobile.ui.theme.TextGray
import com.snakecast.mobile.ui.theme.TextWhite
import com.snakecast.shared.ConnectionState
import com.snakecast.shared.Direction
import com.snakecast.shared.SocketClient
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Premium controller screen with glassmorphic UI.
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
    
    // Motion controls - only start if sensor is available
    LaunchedEffect(controlMode) {
        if (controlMode == ControlMode.MOTION && motionSensorManager.isAvailable()) {
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
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0D0D1A),
                        Color(0xFF1A1A2E),
                        Color(0xFF0D0D1A)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with connection status
            HeaderSection(connectionState)
            
            Spacer(modifier = Modifier.height(20.dp))
            
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
                    EnhancedMotionControlView(
                        lastDirection = lastDirection,
                        isAvailable = motionSensorManager.isAvailable()
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Disconnect button
            TextButton(
                onClick = { socketClient.disconnect() },
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(ConnectionRed.copy(alpha = 0.1f))
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "⏏ Disconnect",
                    color = ConnectionRed,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun HeaderSection(connectionState: ConnectionState) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🐍",
            fontSize = 40.sp
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = "SNAKECAST",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = SnakeGreen,
            letterSpacing = 4.sp
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Connection status pill
        ConnectionStatusPill(connectionState)
    }
}

@Composable
private fun ConnectionStatusPill(connectionState: ConnectionState) {
    val (color, text) = when (connectionState) {
        is ConnectionState.Connected -> ConnectionGreen to "Connected"
        is ConnectionState.Connecting -> AccentBlue to "Connecting..."
        is ConnectionState.Error -> ConnectionRed to "Error"
        is ConnectionState.Disconnected -> ConnectionRed to "Disconnected"
    }
    
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_alpha"
    )
    
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = dotAlpha))
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}

@Composable
private fun EnhancedMotionControlView(
    lastDirection: Direction?,
    isAvailable: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "phone_tilt")
    val tiltAngle by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tilt"
    )
    
    Box(
        modifier = modifier
            .size(280.dp)
            .shadow(16.dp, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.radialGradient(
                    listOf(
                        Color(0xFF2A2A4A),
                        Color(0xFF1A1A2E)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (!isAvailable) {
                Text(
                    text = "⚠️",
                    fontSize = 48.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Motion Sensor Unavailable",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = ConnectionRed
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Switch to D-Pad mode",
                    fontSize = 14.sp,
                    color = TextGray
                )
            } else {
                // Phone icon with tilt animation
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    SnakeGreen.copy(alpha = 0.3f),
                                    AccentBlue.copy(alpha = 0.3f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "📱",
                        fontSize = 48.sp,
                        modifier = Modifier.tiltAnimation(rotationZ = tiltAngle)
                    )
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Text(
                    text = "Motion Active",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = SnakeGreen
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Tilt to control",
                    fontSize = 14.sp,
                    color = TextGray
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Direction indicator
                DirectionIndicator(lastDirection)
            }
        }
    }
}

@Composable
private fun DirectionIndicator(direction: Direction?) {
    val (text, color) = when (direction) {
        Direction.UP -> "⬆️ UP" to SnakeGreen
        Direction.DOWN -> "⬇️ DOWN" to SnakeGreen
        Direction.LEFT -> "⬅️ LEFT" to SnakeGreen
        Direction.RIGHT -> "➡️ RIGHT" to SnakeGreen
        null -> "—" to TextGray
    }
    
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.2f))
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Text(
            text = text,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun Modifier.tiltAnimation(rotationZ: Float): Modifier {
    return this.rotate(rotationZ)
}
