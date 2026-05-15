package com.example.myapplication

import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import androidx.compose.runtime.DisposableEffect
import androidx.core.content.ContextCompat
class Temporizador : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TimerScreen()
        }
    }
}


@Composable
fun TimerScreen() {

    var tempoRestante by remember {mutableStateOf(60)}
    var rodando by remember {mutableStateOf(false) }
    val context = LocalContext.current

    var timer by remember {mutableStateOf<CountDownTimer?>(null)}

    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(
                context: Context?,
                intent: Intent?
            ){
                tempoRestante =
                    intent?.getIntExtra(
                        "tempo",
                        60
                    )?: 60
            }
        }
        val filter = IntentFilter(
            TelaDeBloqueio.ACTION_UPDATE_TIMER
        )
        ContextCompat.registerReceiver(
            context,
            receiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "$tempoRestante",
            fontSize = 40.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = {

            val serviceIntent =
                Intent(context, TelaDeBloqueio::class.java)

            serviceIntent.putExtra("tempo", tempoRestante)
            context.startService(serviceIntent)

            if (!rodando) {

                timer = object : CountDownTimer(
                    (tempoRestante * 1000).toLong(),
                    1000
                ) {

                    override fun onTick(millisUntilFinished: Long) {
                        tempoRestante =
                            (millisUntilFinished / 1000).toInt()

                        val serviceIntent =
                            Intent(context, TelaDeBloqueio::class.java)

                        serviceIntent.putExtra(
                            "tempo",
                            tempoRestante
                        )

                        context.startService(serviceIntent)
                    }

                    override fun onFinish() {
                        rodando = false
                    }
                }.start()

                rodando = true
            }
        }) {
            Text("start")
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(onClick = {
            timer?.cancel()
            rodando = false
        }) {
            Text("PAUSE")
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(onClick = {
            timer?.cancel()
            tempoRestante = 60
            rodando = false
        }) {
            Text("RESET")
        }
    }
}