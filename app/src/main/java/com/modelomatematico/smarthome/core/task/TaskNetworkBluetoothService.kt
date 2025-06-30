package com.modelomatematico.smarthome.core.task

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.modelomatematico.smarthome.core.constants.AppStrings
import com.modelomatematico.smarthome.core.services.bluetooth.NetworkBluetoothService

class TaskNetworkBluetoothService {

    private fun isServiceRunning(context: Context): Boolean {
        val activityManager =
            context.getSystemService(AppCompatActivity.ACTIVITY_SERVICE) as ActivityManager
        for (service in activityManager.getRunningServices(Int.MAX_VALUE)) {
            if (NetworkBluetoothService::class.java.name == service.service.className) {
                return true
            }
        }
        return false
    }

    fun startService(context: Context) {
        if (!isServiceRunning(context)) {
            val intent = Intent(context, NetworkBluetoothService::class.java).apply {
                action = AppStrings.ACTION_START_BLUETOOTH_SERVICE
            }
            context.startService(intent)
        }
    }

    fun stopService(context: Context) {
        if (isServiceRunning(context)) {
            val intent = Intent(context, NetworkBluetoothService::class.java).apply {
                action = AppStrings.ACTION_STOP_BLUETOOTH_SERVICE
            }
            context.stopService(intent)
        }
    }
}
