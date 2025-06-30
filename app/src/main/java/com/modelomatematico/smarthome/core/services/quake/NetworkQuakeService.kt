package com.modelomatematico.smarthome.core.services.quake

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import com.modelomatematico.smarthome.core.constants.AppStrings
import com.modelomatematico.smarthome.core.services.bluetooth.NetworkBluetoothService
import com.modelomatematico.smarthome.features.quake.view.ui.QuakeAlertActivity
import kotlinx.coroutines.*

class NetworkQuakeService : Service() {

    private val TAG = "NetworkQuakeService"
    private var isMonitoring = false
    private var alarmHandler: Handler? = null
    private var alarmRunnable: Runnable? = null
    private var isAlarmActive = false

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "🔥 NetworkQuakeService creado")
        alarmHandler = Handler(Looper.getMainLooper())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            val action = intent.action
            action?.let { currentAction ->
                when (currentAction) {
                    AppStrings.ACTION_START_QUEUE_SERVICE -> {
                        Log.d(TAG, "🚀 Iniciando servicio de monitoreo de alarma")
                        startSensorQueue()
                    }
                    AppStrings.ACTION_STOP_QUEUE_SERVICE -> {
                        Log.d(TAG, "🛑 Deteniendo servicio de monitoreo")
                        stopMonitoring()
                        stopSelf()
                    }
                }
            }
        }
        return START_STICKY
    }

    private fun startSensorQueue() {
        if (isMonitoring) {
            Log.d(TAG, "⚠️ El monitoreo ya está activo")
            return
        }

        Log.d(TAG, "🔍 Iniciando monitoreo de alarmas...")
        isMonitoring = true
        Log.d(TAG, "📡 Servicio listo para recibir mensajes de alarma")
    }

    fun handleBluetoothMessage(message: String) {
        Log.d(TAG, "📨 Mensaje recibido en servicio: $message")
        Log.d(TAG, "🔍 Servicio está monitoreando: $isMonitoring")
        Log.d(TAG, "🚨 Alarma actualmente activa: $isAlarmActive")

        when {
            message.contains("GAS_DETECTADO") -> {
                Log.w(TAG, "🔥 ¡ALARMA DE GAS DETECTADA!")
                if (!isAlarmActive) {
                    activateAlarm(message, "GAS_ALARM")
                }
            }
            message.contains("ALARMA_ACTIVADA") -> {
                Log.w(TAG, "🚨 ¡ALARMA ACTIVADA!")
                if (!isAlarmActive) {
                    activateAlarm(message, "SMOKE_ALARM")
                }
            }
            message.contains("HUMO_DETECTADO") -> {
                Log.w(TAG, "💨 ¡ALARMA DE HUMO DETECTADA!")
                if (!isAlarmActive) {
                    activateAlarm(message, "SMOKE_ALARM")
                }
            }
            message.contains("AIRE_LIMPIO") -> {
                Log.i(TAG, "✅ Aire limpio detectado - Desactivando alarma")
                deactivateAlarm()
            }
            message.contains("ALARMA_APAGADA") -> {
                Log.i(TAG, "🔇 Comando de apagar alarma recibido")
                deactivateAlarm()
            }
            message.contains("ALARMA_SILENCIADA_ANDROID") -> {
                Log.i(TAG, "🔇 Arduino confirmó silenciar alarma")
                // No hacer nada más, la alarma ya fue desactivada
            }
        }
    }

    private fun activateAlarm(message: String, alarmType: String) {
        if (isAlarmActive) {
            Log.d(TAG, "⚠️ Alarma ya está activa, ignorando")
            return
        }

        Log.w(TAG, "🚨 ACTIVANDO ALARMA - Tipo: $alarmType - Duración: 10 segundos")
        isAlarmActive = true

        val closeIntent = Intent("com.modelomatematico.smarthome.CLOSE_ALARM")
        sendBroadcast(closeIntent)

        alarmHandler?.postDelayed({
            showAlertActivity(alarmType)
        }, 100)

        alarmRunnable = Runnable {
            Log.i(TAG, "⏰ Tiempo de alarma agotado (10s) - Desactivando alarma")
            deactivateAlarm()
        }

        alarmHandler?.postDelayed(alarmRunnable!!, 10000)
    }

    private fun deactivateAlarm() {
        if (!isAlarmActive) {
            Log.d(TAG, "⚠️ Alarma ya está desactivada")
            return
        }

        Log.i(TAG, "🔇 Desactivando alarma")
        isAlarmActive = false

        alarmRunnable?.let { runnable ->
            alarmHandler?.removeCallbacks(runnable)
        }
        alarmRunnable = null

        val intent = Intent("com.modelomatematico.smarthome.CLOSE_ALARM")
        sendBroadcast(intent)

        sendSilenceCommandToArduino()
    }

    private fun sendSilenceCommandToArduino() {
        val bluetoothService = NetworkBluetoothService.getInstance()

        if (bluetoothService != null && bluetoothService.isConnected) {
            serviceScope.launch {
                try {
                    val command = 'S'
                    val success = bluetoothService.sendBluetoothCommand(command)
                    Log.d(TAG, "📤 Comando silenciar alarma enviado: $command - Éxito: $success")
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error enviando comando silenciar alarma", e)
                }
            }
        } else {
            Log.w(TAG, "⚠️ No se puede enviar comando - Servicio Bluetooth no disponible o desconectado")
        }
    }

    private fun showAlertActivity(alarmType: String) {
        try {
            Log.d(TAG, "🚨 Mostrando QuakeAlertActivity - Tipo: $alarmType")
            val intent = Intent(this, QuakeAlertActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP)
                putExtra("ALARM_TYPE", alarmType)
                putExtra("ALARM_DURATION", 10000L)
            }
            startActivity(intent)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al mostrar QuakeAlertActivity", e)
        }
    }

    private fun stopMonitoring() {
        Log.d(TAG, "🛑 Deteniendo monitoreo...")
        isMonitoring = false

        if (isAlarmActive) {
            deactivateAlarm()
        }

        Log.d(TAG, "✅ Monitoreo detenido")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "🔥 NetworkQuakeService destruido")

        stopMonitoring()
        serviceScope.cancel()
        alarmHandler = null
    }

    companion object {
        @Volatile
        private var instance: NetworkQuakeService? = null

        fun getInstance(): NetworkQuakeService? = instance

        fun handleMessage(message: String) {
            instance?.handleBluetoothMessage(message)
        }
    }

    init {
        instance = this
    }
}