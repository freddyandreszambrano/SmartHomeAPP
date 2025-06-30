package com.modelomatematico.smarthome.features.quake.view.ui

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.modelomatematico.smarthome.features.quake.view.components.QuakeScreen

class QuakeAlertActivity : ComponentActivity() {

    private val TAG = "QuakeAlertActivity"
    private var alarmType: String = "SMOKE_ALARM"
    private var alarmDuration: Long = 10000L

    private var vibrator: Vibrator? = null

    private val closeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, "🔇 Recibido comando para cerrar alarma")
            stopVibration()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "🚨 QuakeAlertActivity creada")

        initializeVibrator()

        alarmType = intent.getStringExtra("ALARM_TYPE") ?: "SMOKE_ALARM"
        alarmDuration = intent.getLongExtra("ALARM_DURATION", 10000L)

        Log.d(TAG, "📋 Tipo de alarma: $alarmType, Duración: ${alarmDuration}ms")

        setupWindow()

        registerCloseReceiver()

        setContent {
            QuakeScreen(
                alarmType = alarmType,
                onDismiss = {
                    Log.d(TAG, "👆 Usuario tocó para cerrar alarma")
                    stopVibration()
                    finish()
                }
            )
        }
    }

    private fun initializeVibrator() {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    private fun stopVibration() {
        try {
            vibrator?.cancel()
            Log.d(TAG, "🔇 Vibración detenida manualmente")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al detener vibración: ${e.message}")
        }
    }

    private fun setupWindow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private fun registerCloseReceiver() {
        val filter = IntentFilter("com.modelomatematico.smarthome.CLOSE_ALARM")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(closeReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(closeReceiver, filter)
        }
        Log.d(TAG, "📡 BroadcastReceiver registrado para cerrar alarma")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "🔥 QuakeAlertActivity destruida")

        stopVibration()

        try {
            unregisterReceiver(closeReceiver)
            Log.d(TAG, "📡 BroadcastReceiver desregistrado")
        } catch (e: Exception) {
            Log.w(TAG, "⚠️ Error al desregistrar receiver: ${e.message}")
        }
    }

    override fun onPause() {
        super.onPause()
        stopVibration()
        Log.d(TAG, "⏸️ Activity pausada - Vibración detenida")
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d(TAG, "🔄 onNewIntent llamado - Actualizando intent")
        setIntent(intent)

        intent?.let {
            alarmType = it.getStringExtra("ALARM_TYPE") ?: alarmType
            alarmDuration = it.getLongExtra("ALARM_DURATION", alarmDuration)
            Log.d(TAG, "📋 Datos actualizados - Tipo: $alarmType, Duración: ${alarmDuration}ms")
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        Log.d(TAG, "⚠️ Back button presionado - Deteniendo vibración")
        stopVibration()
    }
}