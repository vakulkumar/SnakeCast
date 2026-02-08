package com.snakecast.mobile.connection

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snakecast.mobile.ui.theme.AccentBlue
import com.snakecast.mobile.ui.theme.BackgroundDark
import com.snakecast.mobile.ui.theme.CardDark
import com.snakecast.mobile.ui.theme.ConnectionGreen
import com.snakecast.mobile.ui.theme.ConnectionYellow
import com.snakecast.mobile.ui.theme.SnakeGreen
import com.snakecast.mobile.ui.theme.TextGray
import com.snakecast.mobile.ui.theme.TextWhite
import com.snakecast.shared.ConnectionState
import com.snakecast.shared.DiscoveredService

/**
 * Connection screen for discovering and connecting to TV.
 */
@Composable
fun ConnectionScreen(
    viewModel: ConnectionViewModel,
    onConnected: () -> Unit,
    modifier: Modifier = Modifier
) {
    val discoveryState by viewModel.discoveryState.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    
    // Navigate when connected
    LaunchedEffect(connectionState) {
        if (connectionState is ConnectionState.Connected) {
            onConnected()
        }
    }
    
    // Start scanning on launch
    LaunchedEffect(Unit) {
        viewModel.startScanning()
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        
        // App title
        Text(
            text = "🐍",
            fontSize = 64.sp
        )
        Text(
            text = "SnakeCast",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = SnakeGreen
        )
        Text(
            text = "Controller",
            fontSize = 18.sp,
            color = TextGray
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Scanning indicator
        if (discoveryState.isScanning) {
            ScanningIndicator()
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Connection state message
        when (connectionState) {
            is ConnectionState.Connecting -> {
                CircularProgressIndicator(
                    color = AccentBlue,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Connecting...",
                    fontSize = 16.sp,
                    color = ConnectionYellow
                )
            }
            is ConnectionState.Error -> {
                Text(
                    text = (connectionState as ConnectionState.Error).message,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }
            else -> {}
        }
        
        // Device list
        if (discoveryState.discoveredServices.isEmpty() && discoveryState.isScanning) {
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Looking for SnakeCast TV...",
                fontSize = 16.sp,
                color = TextGray,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Make sure your TV app is running\nand both devices are on the same Wi-Fi",
                fontSize = 14.sp,
                color = TextGray.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(discoveryState.discoveredServices) { service ->
                    DeviceCard(
                        service = service,
                        onClick = { viewModel.connectToService(service) },
                        isConnecting = connectionState is ConnectionState.Connecting
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Scan button
        Button(
            onClick = {
                if (discoveryState.isScanning) {
                    viewModel.stopScanning()
                } else {
                    viewModel.startScanning()
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (discoveryState.isScanning) CardDark else SnakeGreen
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (discoveryState.isScanning) "Stop Scanning" else "Scan for TV",
                fontSize = 16.sp,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
private fun ScanningIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "scan")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    Box(
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(AccentBlue.copy(alpha = 0.2f))
        )
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(AccentBlue.copy(alpha = 0.4f))
        )
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(AccentBlue)
        )
    }
}

@Composable
private fun DeviceCard(
    service: DiscoveredService,
    onClick: () -> Unit,
    isConnecting: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardDark)
            .clickable(enabled = !isConnecting, onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // TV icon
        Text(
            text = "📺",
            fontSize = 32.sp
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = service.serviceName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )
            Text(
                text = "${service.host}:${service.port}",
                fontSize = 12.sp,
                color = TextGray
            )
        }
        
        // Status indicator
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(ConnectionGreen)
        )
    }
}
