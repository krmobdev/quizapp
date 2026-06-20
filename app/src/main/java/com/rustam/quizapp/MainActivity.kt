package com.rustam.quizapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.rustam.quizapp.ui.navigation.AppNavHost
import com.rustam.quizapp.ui.theme.QuizappTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QuizappTheme {
                AppNavHost()
            }
        }
    }
}
