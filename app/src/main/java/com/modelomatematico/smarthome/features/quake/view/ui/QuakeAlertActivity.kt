package com.modelomatematico.smarthome.features.quake.view.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.modelomatematico.smarthome.features.quake.view.components.QuakeScreen

class QuakeAlertActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QuakeScreen()
        }
    }
}