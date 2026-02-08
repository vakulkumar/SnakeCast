package com.snakecast.shared

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket

/**
 * TCP Socket client for the Mobile app.
 * Connects to the TV server and sends direction commands.
 */
class SocketClient {
    
    private var socket: Socket? = null
    private var outputStream: OutputStream? = null
    
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()
    
    /**
     * Connect to the TV server.
     * @param host The host address of the TV
     * @param port The port the TV server is listening on
     * @param timeoutMs Connection timeout in milliseconds
     */
    suspend fun connect(host: String, port: Int, timeoutMs: Int = 5000): Boolean = 
        withContext(Dispatchers.IO) {
            try {
                _connectionState.value = ConnectionState.Connecting
                
                socket = Socket().apply {
                    connect(InetSocketAddress(host, port), timeoutMs)
                    tcpNoDelay = true // Disable Nagle's algorithm for low latency
                }
                
                outputStream = socket?.getOutputStream()
                _connectionState.value = ConnectionState.Connected("$host:$port")
                true
                
            } catch (e: IOException) {
                _connectionState.value = ConnectionState.Error("Connection failed: ${e.message}")
                disconnect()
                false
            }
        }
    
    /**
     * Send a direction command to the TV.
     * @param direction The direction to send
     */
    suspend fun sendCommand(direction: Direction): Boolean = withContext(Dispatchers.IO) {
        try {
            outputStream?.let { stream ->
                stream.write(byteArrayOf(direction.code))
                stream.flush()
                true
            } ?: false
        } catch (e: IOException) {
            _connectionState.value = ConnectionState.Error("Send failed: ${e.message}")
            disconnect()
            false
        }
    }
    
    /**
     * Check if the client is connected.
     */
    fun isConnected(): Boolean = socket?.isConnected == true && !socket!!.isClosed
    
    /**
     * Disconnect from the TV server.
     */
    fun disconnect() {
        try {
            outputStream?.close()
            socket?.close()
        } catch (e: IOException) {
            // Ignore close errors
        } finally {
            outputStream = null
            socket = null
            _connectionState.value = ConnectionState.Disconnected
        }
    }
}
