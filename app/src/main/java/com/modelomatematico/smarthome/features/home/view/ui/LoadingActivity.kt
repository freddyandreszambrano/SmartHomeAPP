package com.modelomatematico.smarthome.features.home.view.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.modelomatematico.smarthome.features.home.view.components.LoadingScreen

class LoadingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LoadingScreen()
        }
    }
}