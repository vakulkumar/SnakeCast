package com.snakecast.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.snakecast.mobile.ui.theme.CardDark
import com.snakecast.mobile.ui.theme.SnakeGreen
import com.snakecast.mobile.ui.theme.SnakeGreenDark
import com.snakecast.shared.Direction

/**
 * D-Pad controller component with haptic feedback.
 */
@Composable
fun DPadView(
    onDirectionPressed: (Direction) -> Unit,
    activeDirection: Direction? = null,
    modifier: Modifier = Modifier,
    size: Dp = 280.dp
) {
    val haptic = LocalHapticFeedback.current
    val buttonSize = size * 0.35f
    val centerOffset = size * 0.32f
    
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Background circle
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(CardDark.copy(alpha = 0.5f))
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
        
        // Center indicator
        Box(
            modifier = Modifier
                .size(buttonSize * 0.6f)
                .clip(CircleShape)
                .background(CardDark)
        )
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
        isActive || isPressed -> SnakeGreen
        else -> SnakeGreenDark.copy(alpha = 0.7f)
    }
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
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
            modifier = Modifier
                .size(32.dp)
                .background(Color.Transparent)
        ) {
            androidx.compose.foundation.Canvas(modifier = Modifier.matchParentSize()) {
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(size.width / 2, size.height * 0.2f)
                    lineTo(size.width * 0.8f, size.height * 0.7f)
                    lineTo(size.width * 0.2f, size.height * 0.7f)
                    close()
                }
                
                rotate(degrees = rotation) {
                    drawPath(
                        path = path,
                        color = Color.White
                    )
                }
            }
        }
    }
}
