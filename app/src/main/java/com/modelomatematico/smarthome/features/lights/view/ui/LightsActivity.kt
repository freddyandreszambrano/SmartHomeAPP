package com.modelomatematico.smarthome.features.lights.view.ui

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.modelomatematico.smarthome.R
import com.modelomatematico.smarthome.core.view.decoration.GridSpacingItemDecoration
import com.modelomatematico.smarthome.databinding.ActivityLightsBinding
import com.modelomatematico.smarthome.features.home.view.ui.HomeActivity
import com.modelomatematico.smarthome.features.lights.data.model.LightCardModel
import com.modelomatematico.smarthome.features.lights.view.ui.adapter.LightsCardAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LightsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLightsBinding
    private lateinit var lightsAdapter: LightsCardAdapter
    private lateinit var lightCards: MutableList<LightCardModel>
    private lateinit var cardTitles: List<String>
    private lateinit var cardActions: Array<String>

    private val TAG = "LightsActivity"

    companion object {
        const val CMD_SALA = '1'
        const val CMD_COCINA = '2'
        const val CMD_BANO = '3'
        const val CMD_CUARTO = '4'
        const val CMD_ALL_OFF = '0'
        const val CMD_ALL_ON = '9'
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLightsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.tvHeaderTitleLights.text = getString(R.string.lights_title)
        cardTitles = resources.getStringArray(R.array.lights_card_titles).toList()
        cardActions = resources.getStringArray(R.array.lights_card_actions)

        initializeData()
        initRecyclerView()
    }

    private fun initializeData() {
        lightCards = mutableListOf<LightCardModel>().apply {
            cardTitles.forEach { title ->
                add(LightCardModel(title, false))
            }
        }
    }

    private fun initRecyclerView() {
        lightsAdapter = LightsCardAdapter(
            lightCards = lightCards,
            onItemClick = { lightCard, position ->
                handleCardClick(lightCard, position)
            },
            onSwitchToggle = { lightCard, position, isOn ->
                handleSwitchToggle(lightCard, position, isOn)
            }
        )
        binding.rvHomeCards.layoutManager = GridLayoutManager(this, 1)
        binding.rvHomeCards.adapter = lightsAdapter
        val spacingInPixels = resources.getDimensionPixelSize(R.dimen.grid_spacing)
        binding.rvHomeCards.addItemDecoration(
            GridSpacingItemDecoration(1, spacingInPixels, true)
        )
    }

    private fun handleCardClick(lightCard: LightCardModel, position: Int) {
        Toast.makeText(this, "Clicked on: ${lightCard.title}", Toast.LENGTH_SHORT).show()

        when (lightCard.title.lowercase()) {
            "todo" -> {
                Log.d(TAG, "Navigating to All Lights Control")
                sendBluetoothCommand(CMD_ALL_ON, "Todas las luces activadas")
            }
            "baño" -> {
                Log.d(TAG, "Navigating to Bathroom Lights")
                sendBluetoothCommand(CMD_BANO, "Luces del baño")
            }
            "cuarto" -> {
                Log.d(TAG, "Navigating to Bedroom Lights")
                sendBluetoothCommand(CMD_CUARTO, "Luces del cuarto")
            }
            "cocina" -> {
                Log.d(TAG, "Navigating to Kitchen Lights")
                sendBluetoothCommand(CMD_COCINA, "Luces de la cocina")
            }
            "sala" -> {
                Log.d(TAG, "Navigating to Living Room Lights")
                sendBluetoothCommand(CMD_SALA, "Luces de la sala")
            }
        }
    }

    private fun handleSwitchToggle(lightCard: LightCardModel, position: Int, isOn: Boolean) {
        Toast.makeText(
            this,
            "${lightCard.title} turned ${if (isOn) "ON" else "OFF"}",
            Toast.LENGTH_SHORT
        ).show()

        toggleLightState(lightCard.title, isOn)
    }

    private fun toggleLightState(deviceName: String, isOn: Boolean) {
        // Determinar comando según la tarjeta y el estado del switch
        val command = when (deviceName.lowercase()) {
            "todo" -> {
                if (isOn) {
                    Log.d(TAG, "Encendiendo TODAS las luces")
                    CMD_ALL_ON
                } else {
                    Log.d(TAG, "Apagando TODAS las luces")
                    CMD_ALL_OFF
                }
            }
            "baño" -> {
                if (isOn) {
                    Log.d(TAG, "Encendiendo luces del BAÑO")
                } else {
                    Log.d(TAG, "Apagando luces del BAÑO")
                }
                CMD_BANO
            }
            "cuarto" -> {
                if (isOn) {
                    Log.d(TAG, "Encendiendo luces del CUARTO")
                } else {
                    Log.d(TAG, "Apagando luces del CUARTO")
                }
                CMD_CUARTO
            }
            "cocina" -> {
                if (isOn) {
                    Log.d(TAG, "Encendiendo luces de la COCINA")
                } else {
                    Log.d(TAG, "Apagando luces de la COCINA")
                }
                CMD_COCINA
            }
            "sala" -> {
                if (isOn) {
                    Log.d(TAG, "Encendiendo luces de la SALA")
                } else {
                    Log.d(TAG, "Apagando luces de la SALA")
                }
                CMD_SALA
            }
            else -> {
                Log.w(TAG, "Comando no definido para: $deviceName")
                return
            }
        }

        val actionMessage = "$deviceName ${if (isOn) "ON" else "OFF"}"
        sendBluetoothCommand(command, actionMessage)
    }

    private fun sendBluetoothCommand(command: Char, description: String) {
        val homeActivity = HomeActivity.instance

        if (homeActivity == null) {
            Log.e(TAG, "HomeActivity no disponible")
            showToast("❌ Error: Conexión Bluetooth no disponible")
            return
        }

        if (!homeActivity.isConnected) {
            Log.w(TAG, "Bluetooth no conectado")
            showToast("❌ Bluetooth no conectado")
            return
        }

        lifecycleScope.launch {
            try {
                showToast("🔄 Enviando comando...")
                val success = homeActivity.sendBluetoothCommand(command)

                val message = if (success) {
                    "✅ $description"
                } else {
                    "❌ Error enviando comando"
                }

                showToast(message)
                Log.i(TAG, "Comando '$command' enviado. Resultado: ${if (success) "ÉXITO" else "FALLO"}")

            } catch (e: Exception) {
                Log.e(TAG, "Error al enviar comando Bluetooth", e)
                showToast("❌ Error de comunicación")
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}