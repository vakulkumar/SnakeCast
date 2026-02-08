package com.snakecast.tv.server

import android.content.Context
import com.snakecast.shared.ConnectionState
import com.snakecast.shared.Direction
import com.snakecast.shared.NsdHelper
import com.snakecast.shared.SocketServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Game server that manages NSD registration and socket connections.
 * Wraps SocketServer and NsdHelper for the TV app.
 */
class GameServer(context: Context) {
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val nsdHelper = NsdHelper(context)
    private val socketServer = SocketServer()
    
    val connectionState: StateFlow<ConnectionState> = socketServer.connectionState
    val commands: SharedFlow<Direction> = socketServer.commands
    
    private var isRunning = false
    
    /**
     * Start the game server.
     * - Starts the TCP socket server
     * - Registers the service via NSD
     */
    fun start(onStarted: (Int) -> Unit = {}) {
        if (isRunning) return
        isRunning = true
        
        scope.launch {
            try {
                // Start socket server
                val port = socketServer.start()
                
                // Register NSD service
                val serviceName = nsdHelper.registerService(port)
                
                onStarted(port)
                
            } catch (e: Exception) {
                e.printStackTrace()
                isRunning = false
            }
        }
    }
    
    /**
     * Stop the game server.
     */
    fun stop() {
        if (!isRunning) return
        isRunning = false
        
        nsdHelper.cleanup()
        socketServer.stop()
    }
    
    /**
     * Clean up all resources.
     */
    fun cleanup() {
        stop()
        scope.cancel()
    }
}
