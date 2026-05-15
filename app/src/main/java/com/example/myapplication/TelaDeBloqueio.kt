package com.example.myapplication

import android.app.PendingIntent
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationCompat
import android.app.Service
import android.content.Intent
import android.os.CountDownTimer
import android.os.IBinder
import java.util.Locale



class TelaDeBloqueio : Service() {

    companion object {
        const val ACTION_PAUSE = "ACTION_PAUSE"

        const val ACTION_UPDATE_TIMER =
            "ACTION_UPDATE_TIMER"
    }

    private var tempoRestante = 60

    private var timer: CountDownTimer? = null

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        val prefs = this.getSharedPreferences(
                "fitpro_prefs",
                MODE_PRIVATE
            )
        tempoRestante =
            intent?.getIntExtra(
                "tempo",
                prefs.getInt(
                    "tempo_restante",
                    60
                )
            ) ?: 60
        if (intent?.action == ACTION_PAUSE) {

            stopSelf()

            return START_NOT_STICKY
        }


        val tempo = intent?.getIntExtra("tempo", 0)

        if(timer == null) {
            timer = object : CountDownTimer(
                (tempoRestante * 1000).toLong(),
                1000
            ) {
                override fun onTick(
                    millisUntilFinished: Long
                ) {
                    tempoRestante =
                        (millisUntilFinished / 1000).toInt()

                    prefs.edit()
                        .putInt(
                            "tempo_restante",
                            tempoRestante
                        )
                        .apply()

                    val minutos = tempoRestante / 60
                    val segundos = tempoRestante % 60

                    val tempoFormatado =
                        String.format(
                            Locale.getDefault(),
                            "%02d:%02d",
                            minutos,
                            segundos
                        )
                    val updateIntent =
                        Intent(ACTION_UPDATE_TIMER)
                    updateIntent.putExtra(
                        "tempo",
                        tempoRestante
                    )

                    sendBroadcast(updateIntent)

                    val nomeTreino =
                        intent?.getStringExtra(
                            "treino"
                        ) ?: "FitPro"

                    val notification =
                        NotificationCompat.Builder(
                            this@TelaDeBloqueio,
                            "Fitpro_timer"
                        )

                            .setContentTitle(nomeTreino)
                            .setContentText(
                                "Tempo restante: $tempoFormatado"
                            )
                            .setSmallIcon(
                                android.R.drawable.ic_media_play
                            )
                            .build()

                    startForeground(1, notification)
                }

                override fun onFinish() {
                    prefs.edit()
                        .remove("tempo_restante")
                        .apply()
                    stopSelf()
                }
            }.start()
        }

        val channelId = "fitpro_timer"


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel = NotificationChannel(
                channelId,
                "Fitpro timer",
                NotificationManager.IMPORTANCE_LOW
            )

            val manager = getSystemService(
                NotificationManager::class.java
            )

            manager.createNotificationChannel(channel)
        }


        val notificationIntent =
            Intent(this, MainActivity::class.java)

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )


        val minutos = tempo?.div(60) ?: 0
        val segundos = tempo?.div(60) ?: 0

        val tempoFormatado =
            String.format(Locale.getDefault(),
                "%02d:%02d",
                minutos,
                segundos
            )

        val pauseIntent = Intent(
            this,
            TelaDeBloqueio::class.java
        ).apply {
            action = ACTION_PAUSE
        }

        val pausePendingIntent =
            PendingIntent.getService(
                this,
                1,
                pauseIntent,
                PendingIntent.FLAG_IMMUTABLE
            )

        val notification: Notification =
            NotificationCompat.Builder(this, channelId)
                .setContentTitle("Fitpro")
                .setContentText("tempo restante: $tempoFormatado")
                .setContentIntent(pendingIntent)
                .setSmallIcon(android.R.drawable.ic_media_play)

                .addAction(
                    android.R.drawable.ic_media_pause,
                    "PAUSE",
                    pausePendingIntent
                )

                .build()

        startForeground(1, notification)

        return START_STICKY
    }
}
