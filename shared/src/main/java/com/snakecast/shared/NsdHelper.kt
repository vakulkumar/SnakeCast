package com.snakecast.shared

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Helper class for Network Service Discovery (NSD).
 * Used by TV to advertise and Mobile to discover services.
 */
class NsdHelper(context: Context) {
    
    private val nsdManager: NsdManager = 
        context.getSystemService(Context.NSD_SERVICE) as NsdManager
    
    private var registrationListener: NsdManager.RegistrationListener? = null
    private var discoveryListener: NsdManager.DiscoveryListener? = null
    
    /**
     * Register a service for discovery (used by TV app).
     * @param port The port the server is listening on
     * @return The actual service name registered (may be modified to ensure uniqueness)
     */
    suspend fun registerService(port: Int): String = suspendCancellableCoroutine { cont ->
        val serviceInfo = NsdServiceInfo().apply {
            serviceName = Protocol.SERVICE_NAME
            serviceType = Protocol.SERVICE_TYPE
            setPort(port)
        }
        
        registrationListener = object : NsdManager.RegistrationListener {
            override fun onServiceRegistered(info: NsdServiceInfo) {
                if (cont.isActive) {
                    cont.resume(info.serviceName)
                }
            }
            
            override fun onRegistrationFailed(info: NsdServiceInfo, errorCode: Int) {
                if (cont.isActive) {
                    cont.resumeWithException(
                        NsdException("Registration failed with error code: $errorCode")
                    )
                }
            }
            
            override fun onServiceUnregistered(info: NsdServiceInfo) {
                // Service unregistered
            }
            
            override fun onUnregistrationFailed(info: NsdServiceInfo, errorCode: Int) {
                // Unregistration failed
            }
        }
        
        nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener)
        
        cont.invokeOnCancellation {
            unregisterService()
        }
    }
    
    /**
     * Unregister the service.
     */
    fun unregisterService() {
        registrationListener?.let {
            try {
                nsdManager.unregisterService(it)
            } catch (e: Exception) {
                // Already unregistered
            }
            registrationListener = null
        }
    }
    
    /**
     * Discover available services (used by Mobile app).
     * Emits discovered services as a Flow.
     */
    fun discoverServices(): Flow<DiscoveredService> = callbackFlow {
        val resolveListener = object : NsdManager.ResolveListener {
            override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                // Resolution failed, continue discovery
            }
            
            override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                val discovered = DiscoveredService(
                    serviceName = serviceInfo.serviceName,
                    host = serviceInfo.host.hostAddress ?: "",
                    port = serviceInfo.port
                )
                trySend(discovered)
            }
        }
        
        discoveryListener = object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(serviceType: String) {
                // Discovery started
            }
            
            override fun onServiceFound(serviceInfo: NsdServiceInfo) {
                if (serviceInfo.serviceType == Protocol.SERVICE_TYPE) {
                    nsdManager.resolveService(serviceInfo, resolveListener)
                }
            }
            
            override fun onServiceLost(serviceInfo: NsdServiceInfo) {
                // Service lost
            }
            
            override fun onDiscoveryStopped(serviceType: String) {
                // Discovery stopped
            }
            
            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                close(NsdException("Discovery start failed with error code: $errorCode"))
            }
            
            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                // Stop discovery failed
            }
        }
        
        nsdManager.discoverServices(
            Protocol.SERVICE_TYPE, 
            NsdManager.PROTOCOL_DNS_SD, 
            discoveryListener
        )
        
        awaitClose {
            stopDiscovery()
        }
    }
    
    /**
     * Stop service discovery.
     */
    fun stopDiscovery() {
        discoveryListener?.let {
            try {
                nsdManager.stopServiceDiscovery(it)
            } catch (e: Exception) {
                // Already stopped
            }
            discoveryListener = null
        }
    }
    
    /**
     * Clean up all NSD resources.
     */
    fun cleanup() {
        unregisterService()
        stopDiscovery()
    }
}

class NsdException(message: String) : Exception(message)
