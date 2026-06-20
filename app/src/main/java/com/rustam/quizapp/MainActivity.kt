package com.rustam.quizapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rustam.quizapp.ui.localization.LocalizedApp
import com.rustam.quizapp.ui.navigation.AppNavHost
import com.rustam.quizapp.ui.screens.settings.SettingsViewModel
import com.rustam.quizapp.ui.theme.QuizappTheme
import com.rustam.quizapp.ui.theme.shouldUseDarkTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsViewModel: SettingsViewModel = viewModel()
            val themeMode by settingsViewModel.themeMode.collectAsState()
            val appLanguage by settingsViewModel.appLanguage.collectAsState()
            val accentTheme by settingsViewModel.accentTheme.collectAsState()
            LocalizedApp(language = appLanguage) {
                QuizappTheme(
                    darkTheme = shouldUseDarkTheme(themeMode),
                    accent = accentTheme
                ) {
                    AppNavHost()
                }
            }
        }
    }
}
