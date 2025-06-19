package com.modelomatematico.smarthome.core.services.quake

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.modelomatematico.smarthome.core.constants.AppStrings

class NetworkQuakeService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            val action = intent.action
            action?.let { currentAction ->
                when (currentAction) {
                    AppStrings.ACTION_START_QUEUE_SERVICE -> startSensorQueue()
                    AppStrings.ACTION_STOP_QUEUE_SERVICE -> stopSelf()
                }
            }
        }
        return START_STICKY
    }

    private fun startSensorQueue() {
        TODO("LOGIC TO START SENSOR QUEUE")
    }
}