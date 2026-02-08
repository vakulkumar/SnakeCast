package com.snakecast.shared

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket

/**
 * TCP Socket server for the TV app.
 * Accepts client connections and emits received direction commands.
 */
class SocketServer {
    
    private var serverSocket: ServerSocket? = null
    private var clientSocket: Socket? = null
    private var serverJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)
    
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()
    
    private val _commands = MutableSharedFlow<Direction>(extraBufferCapacity = 64)
    val commands: SharedFlow<Direction> = _commands.asSharedFlow()
    
    /**
     * Start the server on a system-assigned port.
     * @return The port number the server is listening on
     */
    suspend fun start(): Int = withContext(Dispatchers.IO) {
        try {
            serverSocket = ServerSocket(0).also { socket ->
                serverJob = scope.launch {
                    acceptConnections(socket)
                }
            }
            serverSocket!!.localPort
        } catch (e: IOException) {
            _connectionState.value = ConnectionState.Error("Failed to start server: ${e.message}")
            throw e
        }
    }
    
    private suspend fun acceptConnections(serverSocket: ServerSocket) {
        while (scope.isActive) {
            try {
                _connectionState.value = ConnectionState.Disconnected
                
                // Wait for a client connection
                val socket = serverSocket.accept()
                clientSocket = socket
                
                val clientAddress = socket.remoteSocketAddress.toString()
                _connectionState.value = ConnectionState.Connected(clientAddress)
                
                // Handle client communication
                handleClient(socket)
                
            } catch (e: IOException) {
                if (scope.isActive) {
                    _connectionState.value = ConnectionState.Error("Connection error: ${e.message}")
                }
            }
        }
    }
    
    private suspend fun handleClient(socket: Socket) = withContext(Dispatchers.IO) {
        try {
            val inputStream = socket.getInputStream()
            val buffer = ByteArray(1)
            
            while (scope.isActive && socket.isConnected) {
                val bytesRead = inputStream.read(buffer)
                if (bytesRead == -1) {
                    // Client disconnected
                    break
                }
                
                // Parse the direction command
                Direction.fromCode(buffer[0])?.let { direction ->
                    _commands.emit(direction)
                }
            }
        } catch (e: IOException) {
            // Client disconnected or error
        } finally {
            socket.close()
            clientSocket = null
            _connectionState.value = ConnectionState.Disconnected
        }
    }
    
    /**
     * Stop the server and close all connections.
     */
    fun stop() {
        serverJob?.cancel()
        clientSocket?.close()
        serverSocket?.close()
        serverSocket = null
        clientSocket = null
        _connectionState.value = ConnectionState.Disconnected
    }
    
    /**
     * Get the port the server is listening on.
     */
    fun getPort(): Int = serverSocket?.localPort ?: -1
}
