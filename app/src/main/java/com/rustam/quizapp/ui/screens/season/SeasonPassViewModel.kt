package com.rustam.quizapp.ui.screens.season

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rustam.quizapp.data.PlayerRepository
import com.rustam.quizapp.data.QuestionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SeasonPassUiState(
    val seasonXp: Int = 0,
    val seasonClaimedMask: Long = 0L,
    val daysLeft: Int = 30
)

class SeasonPassViewModel(application: Application) : AndroidViewModel(application) {

    private val playerRepository =
        PlayerRepository(application, QuestionRepository(application))

    val uiState: StateFlow<SeasonPassUiState> = playerRepository.observeProfile()
        .map { profile ->
            SeasonPassUiState(
                seasonXp = profile.seasonXp,
                seasonClaimedMask = profile.seasonClaimedMask,
                daysLeft = profile.seasonDaysLeft
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SeasonPassUiState())

    fun claimReward(level: Int) {
        viewModelScope.launch {
            playerRepository.claimSeasonReward(level)
        }
    }
}
