package com.modelomatematico.smarthome.features.home.view.ui

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.modelomatematico.smarthome.R
import com.modelomatematico.smarthome.core.task.TaskNetworkQuakeService
import com.modelomatematico.smarthome.core.view.decoration.GridSpacingItemDecoration
import com.modelomatematico.smarthome.databinding.ActivityControlButtonsBinding
import com.modelomatematico.smarthome.features.home.view.ui.adapter.HomeCardAdapter
import com.modelomatematico.smarthome.features.lights.view.ui.LightsActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.UUID

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityControlButtonsBinding
    private lateinit var cardTitles: List<String>
    private lateinit var cardActions: Array<String>

    // Bluetooth variables
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var hc06Device: BluetoothDevice? = null
    private var btSocket: BluetoothSocket? = null

    private val hc06Name = "HC-06"
    private val sppUuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private val TAG = "HomeActivity"

    private var isConnecting = false
    var isConnected = false
        private set

    private val btPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { perms ->
            if (perms.values.all { it }) {
                Log.d(TAG, "Permisos Bluetooth concedidos")
                initBluetooth()
            } else {
                Log.e(TAG, "Permisos Bluetooth denegados")
                showToast("Se necesitan permisos Bluetooth")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityControlButtonsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cardTitles = resources.getStringArray(R.array.home_card_titles).toList()
        cardActions = resources.getStringArray(R.array.home_card_actions)

        initRecyclerView()
        startNetworkQuakeService()
        requestBtPermissions()
    }

    @androidx.annotation.RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart - isConnected: $isConnected, hc06Device: ${hc06Device?.name}")
        if (hc06Device != null && !isConnected) {
            lifecycleScope.launch { connectAndWait() }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy - Cerrando conexión")
        try { btSocket?.close() } catch (_: IOException) {}
        isConnected = false
    }

    private fun requestBtPermissions() {
        Log.d(TAG, "Solicitando permisos Bluetooth")
        val perms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
        btPermissionLauncher.launch(perms)
    }

    @SuppressLint("MissingPermission")
    private fun initBluetooth() {
        Log.d(TAG, "Inicializando Bluetooth")
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            ?: run {
                Log.e(TAG, "Bluetooth no disponible")
                showToast("Bluetooth no disponible")
                return
            }

        if (!bluetoothAdapter.isEnabled) {
            Log.w(TAG, "Bluetooth no habilitado, solicitando activación")
            startActivity(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
            return
        }

        hc06Device = bluetoothAdapter.bondedDevices
            .firstOrNull { it.name?.contains(hc06Name, true) == true }

        if (hc06Device == null) {
            Log.e(TAG, "Dispositivo HC-06 no encontrado en dispositivos emparejados")
            showToast("⚠️ Empareja el $hc06Name primero")
        } else {
            Log.i(TAG, "HC-06 encontrado: ${hc06Device?.name} - ${hc06Device?.address}")
            if (!isConnected) {
                lifecycleScope.launch { connectAndWait() }
            }
        }
    }

    @androidx.annotation.RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private suspend fun connectAndWait() {
        if (isConnecting || isConnected || hc06Device == null) {
            Log.d(TAG, "Conexión cancelada - isConnecting: $isConnecting, isConnected: $isConnected, device: ${hc06Device?.name}")
            return
        }

        withContext(Dispatchers.Main) { showToast("🔄 Conectando a HC-06…") }
        Log.i(TAG, "Iniciando conexión a HC-06...")

        val ok = connectSocket()
        withContext(Dispatchers.Main) {
            if (ok) {
                Log.i(TAG, "Conexión exitosa a HC-06")
                showToast("✅ HC-06 conectado")
                isConnected = true
            } else {
                Log.e(TAG, "Fallo en conexión a HC-06")
                showToast("❌ No se pudo conectar al HC-06")
                isConnected = false
            }
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
                return@withContext connected
            } catch (ex2: Exception) {
                Log.e(TAG, "Fallo en método de reflexión", ex2)
            }
            return@withContext false
        } finally {
            isConnecting = false
        }
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
            false
        }
    }

    private fun initRecyclerView() {
        binding.rvHomeCards.layoutManager = GridLayoutManager(this, 2)
        binding.rvHomeCards.adapter = HomeCardAdapter(cardTitles) { cardTitle ->
            handleCardClick(cardTitle)
        }
        val spacingInPixels = resources.getDimensionPixelSize(R.dimen.grid_spacing)
        binding.rvHomeCards.addItemDecoration(
            GridSpacingItemDecoration(2, spacingInPixels, true)
        )
    }

    private fun handleCardClick(cardTitle: String) {
        val index = cardTitles.indexOf(cardTitle)
        if (index != -1 && index < cardActions.size) {
            val action = cardActions[index]

            when (action) {
                "lights" -> {
                    goToLightsActivity()
                }
                "bathroom" -> {
                    Toast.makeText(this, "Control de baño", Toast.LENGTH_SHORT).show()
                }
                "doors" -> {
                    Toast.makeText(this, "Control de puertas", Toast.LENGTH_SHORT).show()
                }
                "dining" -> {
                    Toast.makeText(this, "Control de comedor", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun goToLightsActivity() {
        val intent = Intent(this, LightsActivity::class.java)
        startActivity(intent)
    }

    private fun startNetworkQuakeService() {
        TaskNetworkQuakeService().startService(this)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        var instance: HomeActivity? = null
    }

    init {
        instance = this
    }
}