package com.modelomatematico.smarthome.features.quake.view.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import com.modelomatematico.smarthome.R
import kotlinx.coroutines.delay

@Composable
fun TriggerVibration() {
    val context = LocalContext.current

    DisposableEffect(Unit) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        val pattern = longArrayOf(0, 500, 200)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, 0)
        }

        Log.d("TriggerVibration", "📳 Vibración iniciada")

        onDispose {
            vibrator.cancel()
            Log.d("TriggerVibration", "🔇 Vibración detenida en onDispose")
        }
    }
}

@Composable
fun QuakeScreen(
    alarmType: String = "SMOKE_ALARM",
    onDismiss: () -> Unit = {}
) {
    val context = LocalContext.current

    val (animationRes, backgroundColor, titleText, descriptionText) = when (alarmType) {
        "GAS_ALARM" -> Tuple4(
            R.raw.alert_animation,
            Color(0xFFFF5722),
            "🔥 ALARMA DE GAS",
            "Gas detectado en el ambiente\nEvacuar inmediatamente"
        )
        "SMOKE_ALARM" -> Tuple4(
            R.raw.alert_animation,
            Color(0xFFF44336),
            "💨 ALARMA DE HUMO",
            "Humo detectado en el ambiente\nVerificar el área inmediatamente"
        )
        else -> Tuple4(
            R.raw.alert_animation,
            Color(0xFFF44336),
            "🚨 ALARMA ACTIVADA",
            "Situación de emergencia detectada\nTomar medidas inmediatas"
        )
    }

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(animationRes))
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever,
        isPlaying = true,
        speed = 1.5f,
        restartOnPlay = false
    )

    TriggerVibration()

    var isFlashing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(500)
            isFlashing = !isFlashing
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .clickable {
                Log.d("QuakeScreen", "👆 Usuario tocó para cerrar - Invocando onDismiss")
                onDismiss()
            },
        color = if (isFlashing) backgroundColor else backgroundColor.copy(alpha = 0.7f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = titleText,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier
                    .size(250.dp)
                    .padding(vertical = 16.dp)
            )

            Text(
                text = descriptionText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        Log.d("QuakeScreen", "👆 Usuario tocó el botón para cerrar")
                        onDismiss()
                    },
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.2f)
                )
            ) {
                Text(
                    text = "TOCAR PARA SILENCIAR",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "La alarma se silenciará automáticamente en 10 segundos",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}

private data class Tuple4<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)