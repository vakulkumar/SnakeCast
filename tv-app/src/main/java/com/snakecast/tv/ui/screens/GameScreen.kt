package com.snakecast.tv.ui.screens

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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Text
import com.snakecast.shared.ConnectionState
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

/**
 * Main game screen for the TV app.
 */
@Composable
fun GameScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val gameState by viewModel.gameState.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
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
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
            )
            
            Spacer(modifier = Modifier.width(48.dp))
            
            // Side Panel
            SidePanel(
                gameState = gameState,
                connectionState = connectionState,
                modifier = Modifier.width(300.dp)
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
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(GridDark)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cellWidth = size.width / gameState.gridWidth
            val cellHeight = size.height / gameState.gridHeight
            
            // Draw checkerboard grid
            for (x in 0 until gameState.gridWidth) {
                for (y in 0 until gameState.gridHeight) {
                    val color = if ((x + y) % 2 == 0) GridDark else GridLight
                    drawRect(
                        color = color,
                        topLeft = Offset(x * cellWidth, y * cellHeight),
                        size = Size(cellWidth, cellHeight)
                    )
                }
            }
            
            // Draw food
            val padding = cellWidth * 0.15f
            drawOval(
                color = FoodRed,
                topLeft = Offset(
                    gameState.food.x * cellWidth + padding,
                    gameState.food.y * cellHeight + padding
                ),
                size = Size(cellWidth - padding * 2, cellHeight - padding * 2)
            )
            
            // Draw snake
            gameState.snake.forEachIndexed { index, position ->
                val color = if (index == 0) SnakeHeadGreen else SnakeGreen
                val cornerRadius = if (index == 0) cellWidth * 0.3f else cellWidth * 0.15f
                
                drawRoundRect(
                    color = color,
                    topLeft = Offset(
                        position.x * cellWidth + padding / 2,
                        position.y * cellHeight + padding / 2
                    ),
                    size = Size(cellWidth - padding, cellHeight - padding),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius)
                )
            }
        }
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
            .clip(RoundedCornerShape(16.dp))
            .background(GridDark.copy(alpha = 0.8f))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "SNAKECAST",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = SnakeGreen
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Score
        Text(
            text = "SCORE",
            fontSize = 16.sp,
            color = TextWhite.copy(alpha = 0.7f)
        )
        Text(
            text = "${gameState.score}",
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = AccentBlue
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Snake length
        Text(
            text = "LENGTH",
            fontSize = 16.sp,
            color = TextWhite.copy(alpha = 0.7f)
        )
        Text(
            text = "${gameState.snake.size}",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = SnakeGreen
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Connection status
        ConnectionIndicator(connectionState)
    }
}

@Composable
private fun ConnectionIndicator(connectionState: ConnectionState) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        val (color, text) = when (connectionState) {
            is ConnectionState.Connected -> ConnectionGreen to "Controller Connected"
            is ConnectionState.Connecting -> AccentBlue to "Connecting..."
            is ConnectionState.Disconnected -> ConnectionRed to "Waiting for Controller"
            is ConnectionState.Error -> ConnectionRed to "Connection Error"
        }
        
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = text,
            fontSize = 14.sp,
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
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (gameState.status) {
                GameStatus.WAITING -> {
                    Text(
                        text = "🎮",
                        fontSize = 64.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Waiting for Controller",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Open SnakeCast on your phone\nand connect to start playing",
                        fontSize = 18.sp,
                        color = TextWhite.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
                
                GameStatus.PAUSED -> {
                    Text(
                        text = "⏸️",
                        fontSize = 64.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Game Paused",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    if (connectionState is ConnectionState.Disconnected) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Controller disconnected",
                            fontSize = 18.sp,
                            color = ConnectionRed
                        )
                    }
                }
                
                GameStatus.GAME_OVER -> {
                    Text(
                        text = "💀",
                        fontSize = 64.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Game Over",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = FoodRed
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Final Score: ${gameState.score}",
                        fontSize = 32.sp,
                        color = AccentBlue
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Tap any direction to restart",
                        fontSize = 18.sp,
                        color = TextWhite.copy(alpha = 0.7f)
                    )
                }
                
                else -> {}
            }
        }
    }
}
