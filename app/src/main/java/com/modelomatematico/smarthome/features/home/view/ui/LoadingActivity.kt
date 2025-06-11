package com.modelomatematico.smarthome.features.home.view.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class LoadingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LoadingScreen()
        }
    }
    private fun goToControlButtons() {
        startActivity(Intent(this, ControlButtonsActivity::class.java))
        finish()
    }
}