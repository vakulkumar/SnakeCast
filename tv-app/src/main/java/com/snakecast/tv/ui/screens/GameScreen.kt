package com.snakecast.tv.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import com.snakecast.shared.ConnectionState
import com.snakecast.shared.Direction
import com.snakecast.tv.game.GameState
import com.snakecast.tv.game.GameStatus
import com.snakecast.tv.game.GameViewModel
import com.snakecast.tv.game.Position
import com.snakecast.tv.ui.theme.AccentBlue
import com.snakecast.tv.ui.theme.BackgroundDark
import com.snakecast.tv.ui.theme.ConnectionGreen
import com.snakecast.tv.ui.theme.ConnectionRed
import com.snakecast.tv.ui.theme.FoodRed
import com.snakecast.tv.ui.theme.GridDark
import com.snakecast.tv.ui.theme.GridLight
import com.snakecast.tv.ui.theme.SnakeGreen
import com.snakecast.tv.ui.theme.SnakeHeadGreen
import com.snakecast.tv.ui.theme.TextWhite
import kotlin.math.sin

/**
 * Main game screen for the TV app with premium visuals.
 */
@Composable
fun GameScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val gameState by viewModel.gameState.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    
    // Animation for food pulse
    val infiniteTransition = rememberInfiniteTransition(label = "food_pulse")
    val foodPulse by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "food_scale"
    )
    
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
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Game Board
            GameBoard(
                gameState = gameState,
                foodPulse = foodPulse,
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
            )
            
            Spacer(modifier = Modifier.width(48.dp))
            
            // Side Panel
            SidePanel(
                gameState = gameState,
                connectionState = connectionState,
                modifier = Modifier.width(320.dp)
            )
        }
        
        // Overlay for non-running states
        if (gameState.status != GameStatus.RUNNING) {
            GameOverlay(
                gameState = gameState,
                connectionState = connectionState
            )
        }
    }
}

@Composable
private fun GameBoard(
    gameState: GameState,
    foodPulse: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .shadow(16.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.radialGradient(
                    listOf(
                        Color(0xFF1E1E3F),
                        Color(0xFF151528)
                    )
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            val cellWidth = size.width / gameState.gridWidth
            val cellHeight = size.height / gameState.gridHeight
            
            // Draw subtle grid lines
            drawGridLines(gameState, cellWidth, cellHeight)
            
            // Draw food with pulse animation and glow
            drawFood(gameState, cellWidth, cellHeight, foodPulse)
            
            // Draw snake with gradient segments
            drawSnake(gameState, cellWidth, cellHeight)
        }
    }
}

private fun DrawScope.drawGridLines(
    gameState: GameState,
    cellWidth: Float,
    cellHeight: Float
) {
    val gridColor = Color(0xFF2A2A4A)
    
    // Vertical lines
    for (x in 0..gameState.gridWidth) {
        drawLine(
            color = gridColor,
            start = Offset(x * cellWidth, 0f),
            end = Offset(x * cellWidth, size.height),
            strokeWidth = 1f
        )
    }
    
    // Horizontal lines
    for (y in 0..gameState.gridHeight) {
        drawLine(
            color = gridColor,
            start = Offset(0f, y * cellHeight),
            end = Offset(size.width, y * cellHeight),
            strokeWidth = 1f
        )
    }
}

private fun DrawScope.drawFood(
    gameState: GameState,
    cellWidth: Float,
    cellHeight: Float,
    pulse: Float
) {
    val food = gameState.food
    val centerX = food.x * cellWidth + cellWidth / 2
    val centerY = food.y * cellHeight + cellHeight / 2
    val baseRadius = (cellWidth.coerceAtMost(cellHeight) / 2) * 0.7f
    val radius = baseRadius * pulse
    
    // Outer glow
    drawCircle(
        color = FoodRed.copy(alpha = 0.3f),
        radius = radius * 1.5f,
        center = Offset(centerX, centerY)
    )
    
    // Food body
    drawCircle(
        brush = Brush.radialGradient(
            listOf(
                Color(0xFFFF6B6B),
                FoodRed,
                Color(0xFFCC4444)
            ),
            center = Offset(centerX - radius * 0.2f, centerY - radius * 0.2f)
        ),
        radius = radius,
        center = Offset(centerX, centerY)
    )
    
    // Highlight
    drawCircle(
        color = Color.White.copy(alpha = 0.4f),
        radius = radius * 0.3f,
        center = Offset(centerX - radius * 0.3f, centerY - radius * 0.3f)
    )
}

private fun DrawScope.drawSnake(
    gameState: GameState,
    cellWidth: Float,
    cellHeight: Float
) {
    val snake = gameState.snake
    val padding = cellWidth * 0.1f
    
    snake.forEachIndexed { index, position ->
        val isHead = index == 0
        val progress = 1f - (index.toFloat() / snake.size.coerceAtLeast(1))
        
        // Segment colors with gradient effect
        val segmentColor = if (isHead) {
            SnakeHeadGreen
        } else {
            Color(
                red = (SnakeGreen.red * progress + 0.1f * (1 - progress)),
                green = (SnakeGreen.green * progress + 0.3f * (1 - progress)),
                blue = (SnakeGreen.blue * progress + 0.1f * (1 - progress)),
                alpha = 1f
            )
        }
        
        val x = position.x * cellWidth + padding
        val y = position.y * cellHeight + padding
        val segmentWidth = cellWidth - padding * 2
        val segmentHeight = cellHeight - padding * 2
        val cornerRadius = if (isHead) segmentWidth * 0.4f else segmentWidth * 0.25f
        
        // Shadow for head
        if (isHead) {
            drawRoundRect(
                color = Color.Black.copy(alpha = 0.3f),
                topLeft = Offset(x + 3f, y + 3f),
                size = Size(segmentWidth, segmentHeight),
                cornerRadius = CornerRadius(cornerRadius)
            )
        }
        
        // Main segment
        drawRoundRect(
            brush = Brush.linearGradient(
                listOf(segmentColor, segmentColor.copy(alpha = 0.8f))
            ),
            topLeft = Offset(x, y),
            size = Size(segmentWidth, segmentHeight),
            cornerRadius = CornerRadius(cornerRadius)
        )
        
        // Eyes for head
        if (isHead) {
            drawSnakeEyes(gameState.direction, x, y, segmentWidth, segmentHeight)
        }
    }
}

private fun DrawScope.drawSnakeEyes(
    direction: Direction,
    x: Float,
    y: Float,
    width: Float,
    height: Float
) {
    val eyeRadius = width * 0.12f
    val pupilRadius = eyeRadius * 0.5f
    
    val (eye1Offset, eye2Offset, pupilOffset) = when (direction) {
        Direction.UP -> Triple(
            Offset(x + width * 0.3f, y + height * 0.35f),
            Offset(x + width * 0.7f, y + height * 0.35f),
            Offset(0f, -eyeRadius * 0.3f)
        )
        Direction.DOWN -> Triple(
            Offset(x + width * 0.3f, y + height * 0.65f),
            Offset(x + width * 0.7f, y + height * 0.65f),
            Offset(0f, eyeRadius * 0.3f)
        )
        Direction.LEFT -> Triple(
            Offset(x + width * 0.35f, y + height * 0.3f),
            Offset(x + width * 0.35f, y + height * 0.7f),
            Offset(-eyeRadius * 0.3f, 0f)
        )
        Direction.RIGHT -> Triple(
            Offset(x + width * 0.65f, y + height * 0.3f),
            Offset(x + width * 0.65f, y + height * 0.7f),
            Offset(eyeRadius * 0.3f, 0f)
        )
    }
    
    // Draw eyes
    listOf(eye1Offset, eye2Offset).forEach { eyeCenter ->
        // Eye white
        drawCircle(
            color = Color.White,
            radius = eyeRadius,
            center = eyeCenter
        )
        // Pupil
        drawCircle(
            color = Color(0xFF1A1A2E),
            radius = pupilRadius,
            center = Offset(eyeCenter.x + pupilOffset.x, eyeCenter.y + pupilOffset.y)
        )
    }
}

@Composable
private fun SidePanel(
    gameState: GameState,
    connectionState: ConnectionState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .shadow(8.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF1E1E3F).copy(alpha = 0.9f),
                        Color(0xFF151528).copy(alpha = 0.9f)
                    )
                )
            )
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo
        Text(
            text = "🐍",
            fontSize = 48.sp
        )
        
        Text(
            text = "SNAKECAST",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = SnakeGreen,
            letterSpacing = 4.sp
        )
        
        Spacer(modifier = Modifier.height(40.dp))
        
        // Score Card
        ScoreCard(
            label = "SCORE",
            value = "${gameState.score}",
            color = AccentBlue
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Length Card
        ScoreCard(
            label = "LENGTH",
            value = "${gameState.snake.size}",
            color = SnakeGreen
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Connection status
        ConnectionIndicator(connectionState)
    }
}

@Composable
private fun ScoreCard(
    label: String,
    value: String,
    color: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.3f))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = TextWhite.copy(alpha = 0.6f),
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 42.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun ConnectionIndicator(connectionState: ConnectionState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.3f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        val (color, text) = when (connectionState) {
            is ConnectionState.Connected -> ConnectionGreen to "Controller Connected"
            is ConnectionState.Connecting -> AccentBlue to "Connecting..."
            is ConnectionState.Disconnected -> ConnectionRed to "Waiting for Controller"
            is ConnectionState.Error -> ConnectionRed to "Connection Error"
        }
        
        // Pulsing indicator
        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.4f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(800),
                repeatMode = RepeatMode.Reverse
            ),
            label = "indicator_alpha"
        )
        
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = alpha))
        )
        
        Spacer(modifier = Modifier.width(10.dp))
        
        Text(
            text = text,
            fontSize = 13.sp,
            color = color
        )
    }
}

@Composable
private fun GameOverlay(
    gameState: GameState,
    connectionState: ConnectionState
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            when (gameState.status) {
                GameStatus.WAITING -> {
                    // Animated controller icon
                    val infiniteTransition = rememberInfiniteTransition(label = "bounce")
                    val offsetY by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 15f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(600),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "bounce_y"
                    )
                    
                    Box(modifier = Modifier.padding(bottom = offsetY.dp)) {
                        Text(text = "🎮", fontSize = 80.sp)
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = "Waiting for Controller",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "Open SnakeCast on your phone\nand connect to start playing",
                        fontSize = 20.sp,
                        color = TextWhite.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        lineHeight = 28.sp
                    )
                }
                
                GameStatus.PAUSED -> {
                    Text(text = "⏸️", fontSize = 80.sp)
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = "Game Paused",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    
                    if (connectionState is ConnectionState.Disconnected) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Controller disconnected",
                            fontSize = 20.sp,
                            color = ConnectionRed
                        )
                    }
                }
                
                GameStatus.GAME_OVER -> {
                    Text(text = "💀", fontSize = 80.sp)
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = "Game Over",
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Bold,
                        color = FoodRed
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Text(
                        text = "Final Score",
                        fontSize = 18.sp,
                        color = TextWhite.copy(alpha = 0.6f)
                    )
                    
                    Text(
                        text = "${gameState.score}",
                        fontSize = 64.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentBlue
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Pulsing restart text
                    val infiniteTransition = rememberInfiniteTransition(label = "pulse_text")
                    val alpha by infiniteTransition.animateFloat(
                        initialValue = 0.5f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(700),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "restart_alpha"
                    )
                    
                    Text(
                        text = "Tap any direction to restart",
                        fontSize = 20.sp,
                        color = SnakeGreen.copy(alpha = alpha)
                    )
                }
                
                else -> {}
            }
        }
    }
}
