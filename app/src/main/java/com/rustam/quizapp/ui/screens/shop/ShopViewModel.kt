package com.rustam.quizapp.ui.screens.shop

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rustam.quizapp.audio.SoundManager
import com.rustam.quizapp.audio.SoundResources
import com.rustam.quizapp.audio.SoundType
import com.rustam.quizapp.data.PlayerRepository
import com.rustam.quizapp.data.QuestionRepository
import com.rustam.quizapp.data.SettingsRepository
import com.rustam.quizapp.data.ShopState
import com.rustam.quizapp.data.StreakRepository
import com.rustam.quizapp.domain.AvatarItem
import com.rustam.quizapp.domain.BoosterItem
import com.rustam.quizapp.domain.PowerUpItem
import com.rustam.quizapp.domain.ShopCatalog
import com.rustam.quizapp.domain.ThemeItem
import com.rustam.quizapp.ui.theme.AccentTheme
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
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

data class BoosterUi(
    val item: BoosterItem,
    val owned: Int
)

data class PowerUpUi(
    val item: PowerUpItem,
    val owned: Int
)

data class ShopUiState(
    val coins: Int = 0,
    val avatars: List<AvatarUi> = emptyList(),
    val themes: List<ThemeUi> = emptyList(),
    val boosters: List<BoosterUi> = emptyList(),
    val powerUps: List<PowerUpUi> = emptyList(),
    val streakFreezeCount: Int = 0,
    val streakFreezePrice: Int = ShopCatalog.STREAK_FREEZE_PRICE
)

class ShopViewModel(application: Application) : AndroidViewModel(application) {

    private val playerRepository =
        PlayerRepository(application, QuestionRepository(application))
    private val streakRepository = StreakRepository(application)

    private val soundManager = SoundManager(
        context = application,
        sounds = SoundResources.load(application),
        soundEnabled = SettingsRepository(application).soundEnabled,
        scope = viewModelScope
    )

    /** One-shot stream of XP amounts granted by booster activation, for the snackbar. */
    private val _boosterActivated = MutableSharedFlow<Int>(extraBufferCapacity = 1)
    val boosterActivated: SharedFlow<Int> = _boosterActivated

    val uiState: StateFlow<ShopUiState> = combine(
        playerRepository.observeShop(),
        playerRepository.observeInventory(),
        playerRepository.observePowerUps(),
        streakRepository.observeFreezeCount()
    ) { shop, inventory, powerUps, freezeCount ->
        toUiState(shop, inventory, powerUps, freezeCount)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ShopUiState())

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

    /** Buys one booster, adding it to the backpack if the player can afford it. */
    fun onBoosterBuy(booster: BoosterUi) {
        viewModelScope.launch {
            playerRepository.purchaseBooster(booster.item.id, booster.item.priceCoins)
        }
    }

    /** Consumes one booster from the backpack, granting its free XP. */
    fun onBoosterActivate(booster: BoosterUi) {
        if (booster.owned <= 0) return
        viewModelScope.launch {
            if (playerRepository.activateBooster(booster.item.id)) {
                soundManager.play(SoundType.CLICK)
                _boosterActivated.tryEmit(booster.item.rewardPoints)
            }
        }
    }

    /** Buys one in-quiz power-up, adding it to the backpack if affordable. */
    fun onPowerUpBuy(powerUp: PowerUpUi) {
        viewModelScope.launch {
            playerRepository.purchasePowerUp(powerUp.item.id, powerUp.item.priceCoins)
        }
    }

    /** Buys one Streak Freeze: deducts coins, then adds the freeze to the streak store. */
    fun onStreakFreezeBuy() {
        viewModelScope.launch {
            if (playerRepository.spendCoins(ShopCatalog.STREAK_FREEZE_PRICE)) {
                streakRepository.addFreeze()
                soundManager.play(SoundType.CLICK)
            }
        }
    }

    override fun onCleared() {
        soundManager.release()
    }

    private fun toUiState(
        state: ShopState,
        inventory: Map<String, Int>,
        powerUps: Map<String, Int>,
        freezeCount: Int
    ): ShopUiState = ShopUiState(
        coins = state.coins,
        streakFreezeCount = freezeCount,
        boosters = ShopCatalog.boosters.map { item ->
            BoosterUi(item = item, owned = inventory[item.id] ?: 0)
        },
        powerUps = ShopCatalog.powerUps.map { item ->
            PowerUpUi(item = item, owned = powerUps[item.id] ?: 0)
        },
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
