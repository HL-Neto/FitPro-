package com.example.workoutapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.workoutapp.ui.TelaTreino
import com.example.workoutapp.ui.theme.WorkoutTrackerTheme

class MainActivity : ComponentActivity() {

    private val viewModel: TreinoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.inicializar(applicationContext)

        setContent {
            WorkoutTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TelaTreino(viewModel)
                }
            }
        }
    }
}
