package com.rustam.quizapp.ui.screens.onboarding

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rustam.quizapp.data.SettingsRepository
import kotlinx.coroutines.launch

class OnboardingViewModel(application: Application) : AndroidViewModel(application) {

    private val settings = SettingsRepository(application)

    fun markShown() {
        viewModelScope.launch { settings.setOnboardingShown() }
    }
}
