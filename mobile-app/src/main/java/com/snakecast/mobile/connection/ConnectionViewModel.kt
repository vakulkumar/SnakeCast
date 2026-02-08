package com.snakecast.mobile.connection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snakecast.shared.ConnectionState
import com.snakecast.shared.DiscoveredService
import com.snakecast.shared.NsdHelper
import com.snakecast.shared.SocketClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * Discovery state for the connection screen.
 */
data class DiscoveryState(
    val isScanning: Boolean = false,
    val discoveredServices: List<DiscoveredService> = emptyList(),
    val error: String? = null
)

/**
 * ViewModel for managing NSD discovery and connection.
 */
class ConnectionViewModel(
    private val nsdHelper: NsdHelper,
    private val socketClient: SocketClient
) : ViewModel() {
    
    private val _discoveryState = MutableStateFlow(DiscoveryState())
    val discoveryState: StateFlow<DiscoveryState> = _discoveryState.asStateFlow()
    
    val connectionState: StateFlow<ConnectionState> = socketClient.connectionState
    
    /**
     * Start scanning for available TV devices.
     */
    fun startScanning() {
        if (_discoveryState.value.isScanning) return
        
        _discoveryState.value = _discoveryState.value.copy(
            isScanning = true,
            discoveredServices = emptyList(),
            error = null
        )
        
        viewModelScope.launch {
            nsdHelper.discoverServices()
                .catch { e ->
                    _discoveryState.value = _discoveryState.value.copy(
                        isScanning = false,
                        error = e.message
                    )
                }
                .collect { service ->
                    val currentServices = _discoveryState.value.discoveredServices
                    // Avoid duplicates
                    if (currentServices.none { it.host == service.host && it.port == service.port }) {
                        _discoveryState.value = _discoveryState.value.copy(
                            discoveredServices = currentServices + service
                        )
                    }
                }
        }
    }
    
    /**
     * Stop scanning for devices.
     */
    fun stopScanning() {
        nsdHelper.stopDiscovery()
        _discoveryState.value = _discoveryState.value.copy(isScanning = false)
    }
    
    /**
     * Connect to a discovered TV.
     */
    fun connectToService(service: DiscoveredService) {
        viewModelScope.launch {
            stopScanning()
            socketClient.connect(service.host, service.port)
        }
    }
    
    /**
     * Disconnect from the current TV.
     */
    fun disconnect() {
        socketClient.disconnect()
    }
    
    override fun onCleared() {
        super.onCleared()
        nsdHelper.cleanup()
        socketClient.disconnect()
    }
}
