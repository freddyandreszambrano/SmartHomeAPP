package com.modelomatematico.smarthome.features.home.view.ui

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.modelomatematico.smarthome.R
import com.modelomatematico.smarthome.core.constants.AppStrings
import com.modelomatematico.smarthome.core.services.bluetooth.NetworkBluetoothService
import com.modelomatematico.smarthome.core.services.quake.NetworkQuakeService
import com.modelomatematico.smarthome.core.task.TaskNetworkQuakeService
import com.modelomatematico.smarthome.core.view.decoration.GridSpacingItemDecoration
import com.modelomatematico.smarthome.databinding.ActivityControlButtonsBinding
import com.modelomatematico.smarthome.features.home.view.ui.adapter.HomeCardAdapter
import com.modelomatematico.smarthome.features.lights.view.ui.LightsActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityControlButtonsBinding
    private lateinit var cardTitles: List<String>
    private lateinit var cardActions: Array<String>

    private val TAG = "HomeActivity"

    private var isAppClosing = false

    private var fanState = false

    private val btPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { perms ->
            if (perms.values.all { it }) {
                Log.d(TAG, "Permisos Bluetooth concedidos")
                startBluetoothService()
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

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy - Verificando si debe detener servicios")

        if (isAppClosing) {
            Log.d(TAG, "App cerrándose completamente - Deteniendo servicios")
            stopAllServices()
        } else {
            val quakeService = NetworkQuakeService.getInstance()
            val isAlarmActive = quakeService?.let {
                false
            } ?: false

            if (isAlarmActive) {
                Log.d(TAG, "Alarma activa - Manteniendo servicios ejecutándose")
            } else {
                Log.d(TAG, "Navegación normal - Manteniendo servicios ejecutándose")
            }
        }
    }

    override fun onBackPressed() {
        Log.d(TAG, "Usuario presionó back - Cerrando app completamente")
        isAppClosing = true
        super.onBackPressed()
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

    private fun startBluetoothService() {
        Log.d(TAG, "Iniciando NetworkBluetoothService")
        val intent = Intent(this, NetworkBluetoothService::class.java).apply {
            action = AppStrings.ACTION_START_BLUETOOTH_SERVICE
        }
        startService(intent)
    }

    private fun stopBluetoothService() {
        Log.d(TAG, "Deteniendo NetworkBluetoothService")
        val intent = Intent(this, NetworkBluetoothService::class.java).apply {
            action = AppStrings.ACTION_STOP_BLUETOOTH_SERVICE
        }
        startService(intent)
    }

    // Método para detener TODOS los servicios cuando la app se cierra completamente
    private fun stopAllServices() {
        Log.d(TAG, "Deteniendo TODOS los servicios")
        stopBluetoothService()
        // Aquí puedes agregar otros servicios que necesites detener
    }

    // Método público para cerrar la app completamente (si lo necesitas desde otro lugar)
    fun closeAppCompletely() {
        Log.d(TAG, "Cerrando app completamente desde método público")
        isAppClosing = true
        finish()
    }

    suspend fun sendBluetoothCommand(cmd: Char): Boolean {
        val bluetoothService = NetworkBluetoothService.getInstance()
        return if (bluetoothService != null && bluetoothService.isConnected) {
            bluetoothService.sendBluetoothCommand(cmd)
        } else {
            Log.e(TAG, "NetworkBluetoothService no disponible o no conectado")
            false
        }
    }

    val isBluetoothConnected: Boolean
        get() = NetworkBluetoothService.getInstance()?.isConnected ?: false

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

                "ventilador" -> {
                    toggleFan()
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

    private fun toggleFan() {
        if (!isBluetoothConnected) {
            showToast("Bluetooth no conectado")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val success = sendBluetoothCommand('V')

                withContext(Dispatchers.Main) {
                    if (success) {
                        fanState = !fanState
                        val statusMessage = if (fanState) {
                            "🌬️ Ventilador ENCENDIDO"
                        } else {
                            "🌬️ Ventilador APAGADO"
                        }
                        showToast(statusMessage)
                        Log.d(TAG, statusMessage)
                    } else {
                        showToast("Error al controlar el ventilador")
                        Log.e(TAG, "Error enviando comando 'V' al Arduino")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("Error de comunicación")
                    Log.e(TAG, "Excepción al enviar comando: ${e.message}")
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