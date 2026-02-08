package com.snakecast.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.snakecast.tv.game.GameViewModel
import com.snakecast.tv.ui.screens.GameScreen
import com.snakecast.tv.ui.theme.SnakeCastTvTheme
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class MainActivity : ComponentActivity() {
    
    private val viewModel: GameViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val app = application as SnakeCastTvApp
        
        // Start server and observe connections/commands
        app.gameServer.start { port ->
            // Server started on port
        }
        
        // Observe connection state
        app.gameServer.connectionState
            .onEach { state -> viewModel.updateConnectionState(state) }
            .launchIn(lifecycleScope)
        
        // Observe direction commands from controller
        app.gameServer.commands
            .onEach { direction -> viewModel.changeDirection(direction) }
            .launchIn(lifecycleScope)
        
        setContent {
            SnakeCastTvTheme {
                GameScreen(viewModel = viewModel)
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        val app = application as SnakeCastTvApp
        app.gameServer.stop()
    }
}
