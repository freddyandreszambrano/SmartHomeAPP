package com.modelomatematico.smarthome.core.services.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.modelomatematico.smarthome.core.constants.AppStrings
import com.modelomatematico.smarthome.core.services.quake.NetworkQuakeService
import kotlinx.coroutines.*
import java.io.IOException
import java.io.InputStream
import java.util.UUID

class NetworkBluetoothService : Service() {
    private val TAG = "NetworkBluetoothService"

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var hc06Device: BluetoothDevice? = null
    private var btSocket: BluetoothSocket? = null

    private val hc06Name = "HC-06"
    private val sppUuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    private var isConnecting = false
    var isConnected = false
        private set

    private var listeningJob: Job? = null
    private var inputStream: InputStream? = null

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "🔥 NetworkBluetoothService creado")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            val action = intent.action
            action?.let { currentAction ->
                when (currentAction) {
                    AppStrings.ACTION_START_BLUETOOTH_SERVICE -> {
                        Log.d(TAG, "🚀 Iniciando servicio de Bluetooth")
                        initBluetooth()
                    }
                    AppStrings.ACTION_STOP_BLUETOOTH_SERVICE -> {
                        Log.d(TAG, "🛑 Deteniendo servicio de Bluetooth")
                        disconnectBluetooth()
                        stopSelf()
                    }
                }
            }
        }
        return START_STICKY
    }

    @SuppressLint("MissingPermission")
    private fun initBluetooth() {
        Log.d(TAG, "Inicializando Bluetooth")
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            ?: run {
                Log.e(TAG, "Bluetooth no disponible")
                return
            }

        if (!bluetoothAdapter.isEnabled) {
            Log.w(TAG, "Bluetooth no habilitado")
            return
        }

        hc06Device = bluetoothAdapter.bondedDevices
            .firstOrNull { it.name?.contains(hc06Name, true) == true }

        if (hc06Device == null) {
            Log.e(TAG, "Dispositivo HC-06 no encontrado en dispositivos emparejados")
        } else {
            Log.i(TAG, "HC-06 encontrado: ${hc06Device?.name} - ${hc06Device?.address}")
            if (!isConnected) {
                serviceScope.launch { connectAndWait() }
            }
        }
    }

    @androidx.annotation.RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private suspend fun connectAndWait() {
        if (isConnecting || isConnected || hc06Device == null) {
            Log.d(TAG, "Conexión cancelada - isConnecting: $isConnecting, isConnected: $isConnected, device: ${hc06Device?.name}")
            return
        }

        Log.i(TAG, "Iniciando conexión a HC-06...")

        val ok = connectSocket()
        if (ok) {
            Log.i(TAG, "Conexión exitosa a HC-06")
            isConnected = true
            startListening()
        } else {
            Log.e(TAG, "Fallo en conexión a HC-06")
            isConnected = false
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun connectSocket(): Boolean = withContext(Dispatchers.IO) {
        isConnecting = true
        try {
            Log.d(TAG, "Cerrando socket anterior...")
            btSocket?.close()

            Log.d(TAG, "Creando nuevo socket RFCOMM...")
            btSocket = hc06Device!!.createInsecureRfcommSocketToServiceRecord(sppUuid)
            bluetoothAdapter.cancelDiscovery()

            Log.d(TAG, "Conectando socket...")
            btSocket!!.connect()
            delay(300)

            val connected = btSocket!!.isConnected
            Log.i(TAG, "Estado de conexión del socket: $connected")

            if (connected) {
                inputStream = btSocket!!.inputStream
            }

            return@withContext connected

        } catch (ex: IOException) {
            Log.w(TAG, "Fallo método principal, intentando reflexión...", ex)
            try {
                val m = hc06Device!!::class.java
                    .getMethod("createRfcommSocket", Int::class.javaPrimitiveType)
                btSocket = m.invoke(hc06Device, 1) as BluetoothSocket
                btSocket!!.connect()
                val connected = btSocket!!.isConnected
                Log.i(TAG, "Conexión por reflexión exitosa: $connected")

                if (connected) {
                    inputStream = btSocket!!.inputStream
                }

                return@withContext connected
            } catch (ex2: Exception) {
                Log.e(TAG, "Fallo en método de reflexión", ex2)
            }
            return@withContext false
        } finally {
            isConnecting = false
        }
    }

    @SuppressLint("MissingPermission")
    private fun startListening() {
        Log.d(TAG, "🎧 Iniciando escucha de mensajes Bluetooth...")

        listeningJob = serviceScope.launch(Dispatchers.IO) {
            val buffer = ByteArray(1024)
            var message = StringBuilder()

            while (isConnected && btSocket?.isConnected == true) {
                try {
                    val bytesRead = inputStream?.read(buffer) ?: 0
                    if (bytesRead > 0) {
                        val receivedData = String(buffer, 0, bytesRead)
                        Log.d(TAG, "📡 Datos recibidos: $receivedData")

                        message.append(receivedData)

                        if (receivedData.contains('\n') || receivedData.contains('\r')) {
                            val completeMessage = message.toString().trim()
                            Log.d(TAG, "📨 Mensaje completo recibido: $completeMessage")

                            NetworkQuakeService.handleMessage(completeMessage)

                            message.clear()
                        }
                    }
                } catch (e: IOException) {
                    Log.e(TAG, "❌ Error leyendo datos Bluetooth", e)
                    if (isConnected) {
                        Log.w(TAG, "🔄 Intentando reconectar...")
                        isConnected = false
                        delay(2000)
                        connectAndWait()
                    }
                    break
                }
            }
        }
    }

    private fun stopListening() {
        Log.d(TAG, "🔇 Deteniendo escucha de mensajes...")
        listeningJob?.cancel()
        listeningJob = null
    }

    suspend fun sendBluetoothCommand(cmd: Char): Boolean {
        Log.d(TAG, "Enviando comando '$cmd'")

        if (!isConnected || btSocket?.isConnected != true) {
            Log.w(TAG, "Socket no conectado, intentando reconectar...")
            if (!connectSocket()) {
                Log.e(TAG, "Falló reconexión")
                return false
            }
            isConnected = true
            startListening()
        }

        return sendCommand(cmd)
    }

    private suspend fun sendCommand(cmd: Char): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Escribiendo comando '$cmd' (ASCII: ${cmd.code}) al socket...")
            btSocket!!.outputStream.write(cmd.code)
            btSocket!!.outputStream.flush()
            Log.d(TAG, "Comando enviado y buffer limpiado")
            delay(80)
            true
        } catch (ex: IOException) {
            Log.e(TAG, "Error al enviar comando '$cmd'", ex)
            btSocket?.close()
            isConnected = false
            false
        }
    }

    private fun disconnectBluetooth() {
        Log.d(TAG, "Desconectando Bluetooth")
        stopListening()
        try {
            inputStream?.close()
            btSocket?.close()
            isConnected = false
        } catch (_: IOException) {}
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "🔥 NetworkBluetoothService destruido")
        disconnectBluetooth()
        serviceScope.cancel()
    }

    companion object {
        @Volatile
        private var instance: NetworkBluetoothService? = null

        fun getInstance(): NetworkBluetoothService? = instance
    }

    init {
        instance = this
    }
}