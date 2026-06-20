package com.rustam.quizapp.ui.screens.shop

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rustam.quizapp.data.PlayerRepository
import com.rustam.quizapp.data.QuestionRepository
import com.rustam.quizapp.data.ShopState
import com.rustam.quizapp.domain.AvatarItem
import com.rustam.quizapp.domain.ShopCatalog
import com.rustam.quizapp.domain.ThemeItem
import com.rustam.quizapp.ui.theme.AccentTheme
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AvatarUi(
    val item: AvatarItem,
    val owned: Boolean,
    val equipped: Boolean
)

data class ThemeUi(
    val item: ThemeItem,
    val accent: AccentTheme,
    val owned: Boolean,
    val equipped: Boolean
)

data class ShopUiState(
    val coins: Int = 0,
    val avatars: List<AvatarUi> = emptyList(),
    val themes: List<ThemeUi> = emptyList()
)

class ShopViewModel(application: Application) : AndroidViewModel(application) {

    private val playerRepository =
        PlayerRepository(application, QuestionRepository(application))

    val uiState: StateFlow<ShopUiState> = playerRepository.observeShop()
        .map { toUiState(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ShopUiState())

    /** Buys the avatar if not owned, otherwise equips it. */
    fun onAvatarClick(avatar: AvatarUi) {
        viewModelScope.launch {
            if (avatar.owned) {
                playerRepository.equipAvatar(avatar.item.id)
            } else if (playerRepository.purchase(avatar.item.id, avatar.item.priceCoins)) {
                playerRepository.equipAvatar(avatar.item.id)
            }
        }
    }

    /** Buys the theme if not owned, otherwise equips it. */
    fun onThemeClick(theme: ThemeUi) {
        viewModelScope.launch {
            if (theme.owned) {
                playerRepository.equipTheme(theme.item.id)
            } else if (playerRepository.purchase(theme.item.id, theme.item.priceCoins)) {
                playerRepository.equipTheme(theme.item.id)
            }
        }
    }

    private fun toUiState(state: ShopState): ShopUiState = ShopUiState(
        coins = state.coins,
        avatars = ShopCatalog.avatars.map { item ->
            AvatarUi(
                item = item,
                owned = item.id in state.ownedItemIds,
                equipped = item.id == state.equippedAvatarId
            )
        },
        themes = ShopCatalog.themes.map { item ->
            ThemeUi(
                item = item,
                accent = AccentTheme.fromId(item.id),
                owned = item.id in state.ownedItemIds,
                equipped = item.id == state.equippedThemeId
            )
        }
    )
}
