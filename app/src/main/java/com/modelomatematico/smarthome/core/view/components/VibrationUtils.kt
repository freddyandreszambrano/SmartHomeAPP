package com.modelomatematico.smarthome.core.view.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext

@Composable
fun TriggerVibration() {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createWaveform(longArrayOf(0,500, 300, 500), 0)
            vibrator.vibrate(effect)
        } else {
            vibrator.vibrate(longArrayOf(0, 500, 300, 500), 0)
        }
    }
}