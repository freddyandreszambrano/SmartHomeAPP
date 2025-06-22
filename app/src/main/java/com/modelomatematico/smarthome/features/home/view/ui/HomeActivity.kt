package com.modelomatematico.smarthome.features.home.view.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.modelomatematico.smarthome.R
import com.modelomatematico.smarthome.core.task.TaskNetworkQuakeService
import com.modelomatematico.smarthome.core.view.decoration.GridSpacingItemDecoration
import com.modelomatematico.smarthome.databinding.ActivityControlButtonsBinding
import com.modelomatematico.smarthome.features.home.view.ui.adapter.HomeCardAdapter
import com.modelomatematico.smarthome.features.lights.view.ui.LightsActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityControlButtonsBinding
    private lateinit var cardTitles: List<String>
    private lateinit var cardActions: Array<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityControlButtonsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        cardTitles = resources.getStringArray(R.array.home_card_titles).toList()
        cardActions = resources.getStringArray(R.array.home_card_actions)
        initRecyclerView()
        startNetworkQuakeService()
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
        val i = Intent(this, LightsActivity::class.java)
        startActivity(i)
    }

    private fun startNetworkQuakeService() {
        TaskNetworkQuakeService().startService(this)
    }
}