package com.snakecast.tv

import android.app.Application
import com.snakecast.tv.server.GameServer

class SnakeCastTvApp : Application() {
    
    lateinit var gameServer: GameServer
        private set
    
    override fun onCreate() {
        super.onCreate()
        gameServer = GameServer(this)
    }
    
    override fun onTerminate() {
        super.onTerminate()
        gameServer.cleanup()
    }
}
