package com.modelomatematico.smarthome.features.home.view.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.modelomatematico.smarthome.R
import com.modelomatematico.smarthome.core.task.TaskNetworkQuakeService
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_control_buttons)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val cardLights = findViewById<CardView>(R.id.card_lights)
        val cardBathroom = findViewById<CardView>(R.id.card_bathroom)
        val cardDoors = findViewById<CardView>(R.id.card_doors)
        val cardDining = findViewById<CardView>(R.id.card_dining)

        cardLights.setOnClickListener {
            Toast.makeText(this, "Control de luces", Toast.LENGTH_SHORT).show()
        }

        cardBathroom.setOnClickListener {
            Toast.makeText(this, "Control de baño", Toast.LENGTH_SHORT).show()
        }

        cardDoors.setOnClickListener {
            Toast.makeText(this, "Control de puertas", Toast.LENGTH_SHORT).show()
        }

        cardDining.setOnClickListener {
            Toast.makeText(this, "Control de comedor", Toast.LENGTH_SHORT).show()
        }

        startNetworkQuakeService()
    }

    private fun startNetworkQuakeService() {
        TaskNetworkQuakeService().startService(this)
    }
}