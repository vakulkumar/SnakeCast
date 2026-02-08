package com.snakecast.mobile.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.snakecast.mobile.ui.theme.CardDark
import com.snakecast.mobile.ui.theme.SnakeGreen
import com.snakecast.mobile.ui.theme.SnakeGreenDark
import com.snakecast.shared.Direction

/**
 * Premium D-Pad controller with glassmorphic design and haptic feedback.
 */
@Composable
fun DPadView(
    onDirectionPressed: (Direction) -> Unit,
    activeDirection: Direction? = null,
    modifier: Modifier = Modifier,
    size: Dp = 300.dp
) {
    val haptic = LocalHapticFeedback.current
    val buttonSize = size * 0.32f
    val centerOffset = size * 0.34f
    
    // Subtle glow animation
    val infiniteTransition = rememberInfiniteTransition(label = "dpad_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )
    
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Outer glow ring
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            SnakeGreen.copy(alpha = glowAlpha),
                            Color.Transparent
                        )
                    )
                )
        )
        
        // Background circle with gradient
        Box(
            modifier = Modifier
                .size(size * 0.95f)
                .shadow(12.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            Color(0xFF2A2A4A),
                            Color(0xFF1A1A2E)
                        )
                    )
                )
        )
        
        // Up button
        DirectionButton(
            direction = Direction.UP,
            isActive = activeDirection == Direction.UP,
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onDirectionPressed(Direction.UP)
            },
            modifier = Modifier
                .offset(y = -centerOffset)
                .size(buttonSize),
            rotation = 0f
        )
        
        // Down button
        DirectionButton(
            direction = Direction.DOWN,
            isActive = activeDirection == Direction.DOWN,
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onDirectionPressed(Direction.DOWN)
            },
            modifier = Modifier
                .offset(y = centerOffset)
                .size(buttonSize),
            rotation = 180f
        )
        
        // Left button
        DirectionButton(
            direction = Direction.LEFT,
            isActive = activeDirection == Direction.LEFT,
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onDirectionPressed(Direction.LEFT)
            },
            modifier = Modifier
                .offset(x = -centerOffset)
                .size(buttonSize),
            rotation = 270f
        )
        
        // Right button
        DirectionButton(
            direction = Direction.RIGHT,
            isActive = activeDirection == Direction.RIGHT,
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onDirectionPressed(Direction.RIGHT)
            },
            modifier = Modifier
                .offset(x = centerOffset)
                .size(buttonSize),
            rotation = 90f
        )
        
        // Center snake logo
        Box(
            modifier = Modifier
                .size(buttonSize * 0.7f)
                .shadow(4.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            Color(0xFF3A3A5A),
                            Color(0xFF252540)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.material3.Text(
                text = "🐍",
                fontSize = with(androidx.compose.ui.platform.LocalDensity.current) {
                    (buttonSize * 0.4f).toSp()
                }
            )
        }
    }
}

@Composable
private fun DirectionButton(
    direction: Direction,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    rotation: Float = 0f
) {
    var isPressed by remember { mutableStateOf(false) }
    
    val backgroundColor = when {
        isActive || isPressed -> Brush.linearGradient(
            listOf(
                SnakeGreen,
                Color(0xFF2ECC71)
            )
        )
        else -> Brush.linearGradient(
            listOf(
                SnakeGreenDark.copy(alpha = 0.8f),
                SnakeGreenDark.copy(alpha = 0.6f)
            )
        )
    }
    
    Box(
        modifier = modifier
            .shadow(if (isActive || isPressed) 8.dp else 4.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        onClick()
                        tryAwaitRelease()
                        isPressed = false
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Arrow indicator using Canvas
        Box(
            modifier = Modifier.size(36.dp)
        ) {
            androidx.compose.foundation.Canvas(modifier = Modifier.matchParentSize()) {
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(size.width / 2, size.height * 0.15f)
                    lineTo(size.width * 0.85f, size.height * 0.75f)
                    lineTo(size.width * 0.15f, size.height * 0.75f)
                    close()
                }
                
                rotate(degrees = rotation) {
                    drawPath(
                        path = path,
                        color = if (isActive || isPressed) Color.White else Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}
