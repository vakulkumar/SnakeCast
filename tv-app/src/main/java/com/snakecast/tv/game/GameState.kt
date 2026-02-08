package com.snakecast.tv.game

import com.snakecast.shared.Direction

/**
 * Position on the game grid with wrap-around support.
 */
data class Position(val x: Int, val y: Int) {
    fun move(direction: Direction, gridWidth: Int, gridHeight: Int): Position {
        val newX = when (direction) {
            Direction.LEFT -> if (x - 1 < 0) gridWidth - 1 else x - 1
            Direction.RIGHT -> if (x + 1 >= gridWidth) 0 else x + 1
            else -> x
        }
        val newY = when (direction) {
            Direction.UP -> if (y - 1 < 0) gridHeight - 1 else y - 1
            Direction.DOWN -> if (y + 1 >= gridHeight) 0 else y + 1
            else -> y
        }
        return Position(newX, newY)
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
    
    /**
     * Check for self collision only (no wall collision with wrap-around).
     */
    fun isCollision(): Boolean {
        val newHead = head.move(nextDirection, gridWidth, gridHeight)
        // Only self collision (check against body, not including tail which will move)
        return snake.dropLast(1).contains(newHead)
    }
    
    fun isEatingFood(): Boolean = head.move(nextDirection, gridWidth, gridHeight) == food
}

/**
 * Game constants for configuration.
 */
object GameConstants {
    const val GRID_WIDTH = 20
    const val GRID_HEIGHT = 20
    const val INITIAL_TICK_MS = 250L    // Slower start (was 150)
    const val MIN_TICK_MS = 120L        // Slower minimum (was 80)
    const val SPEED_INCREASE_INTERVAL = 5 // Every N foods, speed increases
}
