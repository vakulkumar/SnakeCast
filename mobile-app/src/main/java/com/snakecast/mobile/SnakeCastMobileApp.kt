package com.snakecast.mobile

import android.app.Application
import com.snakecast.shared.NsdHelper
import com.snakecast.shared.SocketClient

class SnakeCastMobileApp : Application() {
    
    lateinit var nsdHelper: NsdHelper
        private set
    
    lateinit var socketClient: SocketClient
        private set
    
    override fun onCreate() {
        super.onCreate()
        nsdHelper = NsdHelper(this)
        socketClient = SocketClient()
    }
    
    override fun onTerminate() {
        super.onTerminate()
        nsdHelper.cleanup()
        socketClient.disconnect()
    }
}
