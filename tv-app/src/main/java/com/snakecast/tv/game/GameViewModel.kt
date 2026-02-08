package com.snakecast.tv.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snakecast.shared.ConnectionState
import com.snakecast.shared.Direction
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * ViewModel for the Snake game logic.
 * Manages game state, game loop, and direction input.
 */
class GameViewModel : ViewModel() {
    
    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()
    
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()
    
    private var gameLoopJob: Job? = null
    private var currentTickMs = GameConstants.INITIAL_TICK_MS
    
    /**
     * Update connection state from the server.
     */
    fun updateConnectionState(state: ConnectionState) {
        _connectionState.value = state
        
        when (state) {
            is ConnectionState.Connected -> {
                _gameState.update { it.copy(isControllerConnected = true) }
                if (_gameState.value.status == GameStatus.WAITING) {
                    start()
                }
            }
            is ConnectionState.Disconnected -> {
                _gameState.update { it.copy(isControllerConnected = false) }
                if (_gameState.value.status == GameStatus.RUNNING) {
                    pause()
                }
            }
            else -> {}
        }
    }
    
    /**
     * Change the snake's direction.
     * Prevents 180-degree turns.
     * If game is over, restarts the game.
     */
    fun changeDirection(direction: Direction) {
        // If game over, restart the game
        if (_gameState.value.status == GameStatus.GAME_OVER) {
            reset()
            start()
            return
        }
        
        // If paused and controller connected, resume
        if (_gameState.value.status == GameStatus.PAUSED && _gameState.value.isControllerConnected) {
            start()
        }
        
        _gameState.update { state ->
            // Prevent reversing direction
            val isOpposite = when (direction) {
                Direction.UP -> state.direction == Direction.DOWN
                Direction.DOWN -> state.direction == Direction.UP
                Direction.LEFT -> state.direction == Direction.RIGHT
                Direction.RIGHT -> state.direction == Direction.LEFT
            }
            
            if (!isOpposite) {
                state.copy(nextDirection = direction)
            } else {
                state
            }
        }
    }
    
    /**
     * Start or resume the game.
     */
    fun start() {
        if (_gameState.value.status == GameStatus.GAME_OVER) {
            reset()
        }
        
        _gameState.update { it.copy(status = GameStatus.RUNNING) }
        startGameLoop()
    }
    
    /**
     * Pause the game.
     */
    fun pause() {
        gameLoopJob?.cancel()
        _gameState.update { it.copy(status = GameStatus.PAUSED) }
    }
    
    /**
     * Reset the game to initial state.
     */
    fun reset() {
        gameLoopJob?.cancel()
        currentTickMs = GameConstants.INITIAL_TICK_MS
        
        val initialSnake = listOf(
            Position(GameConstants.GRID_WIDTH / 2, GameConstants.GRID_HEIGHT / 2)
        )
        
        _gameState.value = GameState(
            snake = initialSnake,
            food = generateFood(initialSnake),
            direction = Direction.RIGHT,
            nextDirection = Direction.RIGHT,
            score = 0,
            status = if (_gameState.value.isControllerConnected) GameStatus.RUNNING else GameStatus.WAITING,
            isControllerConnected = _gameState.value.isControllerConnected
        )
        
        if (_gameState.value.status == GameStatus.RUNNING) {
            startGameLoop()
        }
    }
    
    private fun startGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch {
            while (isActive && _gameState.value.status == GameStatus.RUNNING) {
                tick()
                delay(currentTickMs)
            }
        }
    }
    
    private fun tick() {
        _gameState.update { state ->
            if (state.status != GameStatus.RUNNING) return@update state
            
            // Check for self-collision before moving
            if (state.isCollision()) {
                return@update state.copy(status = GameStatus.GAME_OVER)
            }
            
            val newHead = state.head.move(state.nextDirection, state.gridWidth, state.gridHeight)
            val eating = state.isEatingFood()
            
            val newSnake = if (eating) {
                listOf(newHead) + state.snake
            } else {
                listOf(newHead) + state.snake.dropLast(1)
            }
            
            val newScore = if (eating) state.score + 10 else state.score
            val newFood = if (eating) generateFood(newSnake) else state.food
            
            // Increase speed every N foods
            if (eating && newScore % (GameConstants.SPEED_INCREASE_INTERVAL * 10) == 0) {
                currentTickMs = maxOf(GameConstants.MIN_TICK_MS, currentTickMs - 15)
            }
            
            state.copy(
                snake = newSnake,
                food = newFood,
                direction = state.nextDirection,
                score = newScore
            )
        }
    }
    
    private fun generateFood(snake: List<Position>): Position {
        val state = _gameState.value
        var food: Position
        do {
            food = Position(
                Random.nextInt(state.gridWidth),
                Random.nextInt(state.gridHeight)
            )
        } while (snake.contains(food))
        return food
    }
    
    override fun onCleared() {
        super.onCleared()
        gameLoopJob?.cancel()
    }
}
