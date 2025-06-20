package com.modelomatematico.smarthome.features.home.view.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.modelomatematico.smarthome.R
import com.modelomatematico.smarthome.core.task.TaskNetworkQuakeService
import com.modelomatematico.smarthome.databinding.ActivityControlButtonsBinding
import com.modelomatematico.smarthome.features.home.view.adapter.HomeCardAdapter
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

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initRecyclerView()
        startNetworkQuakeService()
    }

    private fun initRecyclerView() {
        cardTitles = resources.getStringArray(R.array.home_card_titles).toList()
        cardActions = resources.getStringArray(R.array.home_card_actions)

        binding.rvHomeCards.layoutManager = GridLayoutManager(this, 2)
        binding.rvHomeCards.adapter = HomeCardAdapter(cardTitles) { cardTitle ->
            handleCardClick(cardTitle)
        }
    }

    private fun handleCardClick(cardTitle: String) {
        val index = cardTitles.indexOf(cardTitle)
        if (index != -1 && index < cardActions.size) {
            val action = cardActions[index]

            when (action) {
                "lights" -> {
                    Toast.makeText(this, "Control de luces", Toast.LENGTH_SHORT).show()
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

    private fun startNetworkQuakeService() {
        TaskNetworkQuakeService().startService(this)
    }
}