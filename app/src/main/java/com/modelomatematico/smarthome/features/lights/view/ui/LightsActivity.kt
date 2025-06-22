package com.modelomatematico.smarthome.features.lights.view.ui

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.modelomatematico.smarthome.R
import com.modelomatematico.smarthome.core.view.decoration.GridSpacingItemDecoration
import com.modelomatematico.smarthome.databinding.ActivityLightsBinding
import com.modelomatematico.smarthome.features.lights.view.ui.adapter.LightCard
import com.modelomatematico.smarthome.features.lights.view.ui.adapter.LightsCardAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LightsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLightsBinding
    private lateinit var lightsAdapter: LightsCardAdapter
    private lateinit var lightCards: MutableList<LightCard>
    private lateinit var cardTitles: List<String>
    private lateinit var cardActions: Array<String>

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
        lightCards = mutableListOf<LightCard>().apply {
            cardTitles.forEach { title ->
                add(LightCard(title, false))
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

    private fun handleCardClick(lightCard: LightCard, position: Int) {
        Toast.makeText(this, "Clicked on: ${lightCard.title}", Toast.LENGTH_SHORT).show()

        // Aquí puedes manejar la navegación a diferentes pantallas según el tipo de card
        when (lightCard.title.lowercase()) {
            "morning routine", "luces" -> {
                Log.d("LightsActivity", "Navigating to Morning Routine")
            }

            "bathroom", "baño" -> {
                Log.d("LightsActivity", "Navigating to Bathroom Lights")
            }

            "doors", "puertas" -> {
                Log.d("LightsActivity", "Navigating to Doors Lights")
            }

            "dining", "comedor" -> {
                Log.d("LightsActivity", "Navigating to Dining Lights")
            }
        }
    }



    private fun handleSwitchToggle(lightCard: LightCard, position: Int, isOn: Boolean) {
        // Aquí puedes llamar a tu API o servicio para cambiar el estado de las luces
        Toast.makeText(
            this,
            "${lightCard.title} turned ${if (isOn) "ON" else "OFF"}",
            Toast.LENGTH_SHORT
        ).show()

        // Simular llamada a API
        toggleLightState(lightCard.title, isOn)
    }

    private fun toggleLightState(deviceName: String, isOn: Boolean) {
        // Aquí implementarías la lógica para comunicarte con tu sistema de domótica
        // Por ejemplo, llamada a API REST, MQTT, etc.

        // Simulación de respuesta exitosa
        // En caso de error, podrías revertir el estado del switch
        // lightsAdapter.updateCard(position, !isOn)
    }
}