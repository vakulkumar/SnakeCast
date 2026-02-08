package com.snakecast.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.snakecast.mobile.connection.ConnectionScreen
import com.snakecast.mobile.connection.ConnectionViewModel
import com.snakecast.mobile.controller.ControllerScreen
import com.snakecast.mobile.ui.theme.SnakeCastMobileTheme

class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val app = application as SnakeCastMobileApp
        
        setContent {
            SnakeCastMobileTheme {
                SnakeCastMobileApp(
                    nsdHelper = app.nsdHelper,
                    socketClient = app.socketClient
                )
            }
        }
    }
}

@Composable
private fun SnakeCastMobileApp(
    nsdHelper: com.snakecast.shared.NsdHelper,
    socketClient: com.snakecast.shared.SocketClient
) {
    var isConnected by remember { mutableStateOf(false) }
    
    val connectionViewModel = remember {
        ConnectionViewModel(nsdHelper, socketClient)
    }
    
    if (isConnected) {
        ControllerScreen(
            socketClient = socketClient,
            onDisconnect = { isConnected = false }
        )
    } else {
        ConnectionScreen(
            viewModel = connectionViewModel,
            onConnected = { isConnected = true }
        )
    }
}
