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
import com.modelomatematico.smarthome.features.lights.data.model.LightCardModel
import com.modelomatematico.smarthome.features.lights.view.ui.adapter.LightsCardAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LightsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLightsBinding
    private lateinit var lightsAdapter: LightsCardAdapter
    private lateinit var lightCards: MutableList<LightCardModel>
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
                Log.d("LightsActivity", "Navigating to All Lights Control")
            }

            "baño" -> {
                Log.d("LightsActivity", "Navigating to Bathroom Lights")
            }

            "cuarto" -> {
                Log.d("LightsActivity", "Navigating to Bedroom Lights")
            }

            "cocina" -> {
                Log.d("LightsActivity", "Navigating to Kitchen Lights")
            }

            "sala" -> {
                Log.d("LightsActivity", "Navigating to Living Room Lights")
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
        when (deviceName.lowercase()) {
            "todo" -> {
                if (isOn) {
                    Log.d("LightsActivity", "Encendiendo TODAS las luces")
                } else {
                    Log.d("LightsActivity", "Apagando TODAS las luces")
                }
            }

            "baño" -> {
                if (isOn) {
                    Log.d("LightsActivity", "Encendiendo luces del BAÑO")
                } else {
                    Log.d("LightsActivity", "Apagando luces del BAÑO")
                }
            }

            "cuarto" -> {
                if (isOn) {
                    Log.d("LightsActivity", "Encendiendo luces del CUARTO")
                    // Ejemplo: apiClient.turnOnBedroomLights()
                } else {
                    Log.d("LightsActivity", "Apagando luces del CUARTO")
                }
            }

            "cocina" -> {
                if (isOn) {
                    Log.d("LightsActivity", "Encendiendo luces de la COCINA")
                } else {
                    Log.d("LightsActivity", "Apagando luces de la COCINA")
                }
            }

            "sala" -> {
                if (isOn) {
                    Log.d("LightsActivity", "Encendiendo luces de la SALA")
                } else {
                    Log.d("LightsActivity", "Apagando luces de la SALA")
                }
            }
        }
    }
}