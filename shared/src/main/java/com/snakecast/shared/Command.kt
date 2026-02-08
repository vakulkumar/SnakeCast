package com.snakecast.shared

/**
 * Direction commands for Snake game control.
 * Single-byte protocol for minimal latency.
 */
enum class Direction(val code: Byte) {
    UP(0x01),
    DOWN(0x02),
    LEFT(0x03),
    RIGHT(0x04);

    companion object {
        fun fromCode(code: Byte): Direction? = entries.find { it.code == code }
    }
}

/**
 * Connection state for both server and client.
 */
sealed class ConnectionState {
    data object Disconnected : ConnectionState()
    data object Connecting : ConnectionState()
    data class Connected(val clientInfo: String = "") : ConnectionState()
    data class Error(val message: String) : ConnectionState()
}

/**
 * Service discovery result for NSD.
 */
data class DiscoveredService(
    val serviceName: String,
    val host: String,
    val port: Int
)

/**
 * Protocol constants for SnakeCast networking.
 */
object Protocol {
    const val SERVICE_TYPE = "_snakecast._tcp."
    const val SERVICE_NAME = "SnakeCast-TV"
    const val DEFAULT_PORT = 0 // Use system-assigned port
}
