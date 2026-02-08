package com.snakecast.tv.game

import com.snakecast.shared.Direction

/**
 * Position on the game grid.
 */
data class Position(val x: Int, val y: Int) {
    fun move(direction: Direction): Position = when (direction) {
        Direction.UP -> copy(y = y - 1)
        Direction.DOWN -> copy(y = y + 1)
        Direction.LEFT -> copy(x = x - 1)
        Direction.RIGHT -> copy(x = x + 1)
    }
}

/**
 * Game status states.
 */
enum class GameStatus {
    WAITING,    // Waiting for controller connection
    RUNNING,    // Game is actively running
    PAUSED,     // Game is paused
    GAME_OVER   // Game ended
}

/**
 * Complete game state for the Snake game.
 */
data class GameState(
    val snake: List<Position> = listOf(Position(10, 10)),
    val food: Position = Position(15, 10),
    val direction: Direction = Direction.RIGHT,
    val nextDirection: Direction = Direction.RIGHT,
    val score: Int = 0,
    val status: GameStatus = GameStatus.WAITING,
    val gridWidth: Int = GameConstants.GRID_WIDTH,
    val gridHeight: Int = GameConstants.GRID_HEIGHT,
    val isControllerConnected: Boolean = false
) {
    val head: Position get() = snake.first()
    
    fun isCollision(): Boolean {
        val newHead = head.move(nextDirection)
        // Wall collision
        if (newHead.x < 0 || newHead.x >= gridWidth || 
            newHead.y < 0 || newHead.y >= gridHeight) {
            return true
        }
        // Self collision (check against body, not including tail which will move)
        return snake.dropLast(1).contains(newHead)
    }
    
    fun isEatingFood(): Boolean = head.move(nextDirection) == food
}

/**
 * Game constants for configuration.
 */
object GameConstants {
    const val GRID_WIDTH = 20
    const val GRID_HEIGHT = 20
    const val INITIAL_TICK_MS = 150L
    const val MIN_TICK_MS = 80L
    const val SPEED_INCREASE_INTERVAL = 5 // Every N foods, speed increases
}
