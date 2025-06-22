package com.modelomatematico.smarthome.core.services.quake

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.modelomatematico.smarthome.core.constants.AppStrings
import com.modelomatematico.smarthome.features.quake.view.ui.QuakeAlertActivity

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
//        TODO: Implement the logic to start the sensor queue
//        showAlertActivity()
    }

    private fun showAlertActivity() {
        val i = Intent(this, QuakeAlertActivity::class.java)
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(i)
    }

}