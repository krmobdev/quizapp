package com.rustam.quizapp.ui.screens.shop

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rustam.quizapp.R
import com.rustam.quizapp.audio.SoundManager
import com.rustam.quizapp.audio.SoundResources
import com.rustam.quizapp.audio.SoundType
import com.rustam.quizapp.data.PlayerRepository
import com.rustam.quizapp.data.QuestionRepository
import com.rustam.quizapp.data.SettingsRepository
import com.rustam.quizapp.data.ShopState
import com.rustam.quizapp.data.StreakRepository
import com.rustam.quizapp.domain.AvatarItem
import com.rustam.quizapp.domain.BoostItem
import com.rustam.quizapp.domain.BoostType
import com.rustam.quizapp.domain.BoosterItem
import com.rustam.quizapp.domain.LootResult
import com.rustam.quizapp.domain.PowerUpItem
import com.rustam.quizapp.domain.ShopCatalog
import com.rustam.quizapp.domain.ThemeItem
import com.rustam.quizapp.domain.TitleItem
import com.rustam.quizapp.ui.theme.AccentTheme
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

data class TitleUi(
    val item: TitleItem,
    val owned: Boolean,
    val equipped: Boolean
)

data class BoostUi(
    val item: BoostItem,
    val owned: Int,
    /** Remaining quizzes this boost is currently active for (0 = not running). */
    val activeQuizzesLeft: Int
)

data class ShopUiState(
    val coins: Int = 0,
    val avatars: List<AvatarUi> = emptyList(),
    val themes: List<ThemeUi> = emptyList(),
    val boosters: List<BoosterUi> = emptyList(),
    val powerUps: List<PowerUpUi> = emptyList(),
    val boosts: List<BoostUi> = emptyList(),
    val titles: List<TitleUi> = emptyList(),
    val streakFreezeCount: Int = 0,
    val streakFreezePrice: Int = ShopCatalog.STREAK_FREEZE_PRICE
)

class ShopViewModel(application: Application) : AndroidViewModel(application) {

    private val playerRepository =
        PlayerRepository(application, QuestionRepository(application))
    private val streakRepository = StreakRepository(application)
    private val settingsRepository = SettingsRepository(application)

    private val soundManager = SoundManager(
        context = application,
        sounds = SoundResources.load(application),
        soundEnabled = settingsRepository.soundEnabled,
        scope = viewModelScope
    )

    /** One-shot stream of ready-to-show snackbar messages (booster / boost activation). */
    private val _snackbar = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val snackbar: SharedFlow<String> = _snackbar

    /** The reward from the most recent Lucky Chest open, shown in a reveal dialog (null = none). */
    private val _lootResult = MutableStateFlow<LootResult?>(null)
    val lootResult: StateFlow<LootResult?> = _lootResult.asStateFlow()

    val uiState: StateFlow<ShopUiState> = combine(
        playerRepository.observeShop(),
        playerRepository.observeInventory(),
        playerRepository.observePowerUps(),
        playerRepository.observeBoosts(),
        streakRepository.observeFreezeCount()
    ) { shop, inventory, powerUps, boosts, freezeCount ->
        toUiState(shop, inventory, powerUps, boosts, freezeCount)
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

    /** Buys the title if not owned, equips an owned one, or removes the equipped one when tapped. */
    fun onTitleClick(title: TitleUi) {
        viewModelScope.launch {
            when {
                title.equipped -> playerRepository.equipTitle(null)
                title.owned -> playerRepository.equipTitle(title.item.id)
                playerRepository.purchase(title.item.id, title.item.priceCoins) ->
                    playerRepository.equipTitle(title.item.id)
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
                _snackbar.tryEmit(
                    getApplication<Application>().getString(
                        R.string.inventory_activated_snackbar,
                        booster.item.rewardPoints
                    )
                )
            }
        }
    }

    /** Buys one in-quiz power-up, adding it to the backpack if affordable. */
    fun onPowerUpBuy(powerUp: PowerUpUi) {
        viewModelScope.launch {
            playerRepository.purchasePowerUp(powerUp.item.id, powerUp.item.priceCoins)
        }
    }

    /** Buys one temporary boost, adding it to the backpack if affordable. */
    fun onBoostBuy(boost: BoostUi) {
        viewModelScope.launch {
            playerRepository.purchaseBoost(boost.item.id, boost.item.priceCoins)
        }
    }

    /** Activates one temporary boost from the backpack, starting its ×2 charges. */
    fun onBoostActivate(boost: BoostUi) {
        if (boost.owned <= 0) return
        viewModelScope.launch {
            if (playerRepository.activateBoost(boost.item.id)) {
                soundManager.play(SoundType.CLICK)
                _snackbar.tryEmit(
                    getApplication<Application>().getString(
                        R.string.boost_activated_snackbar,
                        boost.item.quizzes
                    )
                )
            }
        }
    }

    /** Opens one Lucky Chest if affordable, surfacing the rolled reward for the reveal dialog. */
    fun onOpenLootBox() {
        viewModelScope.launch {
            val result = playerRepository.openLootBox()
            if (result != null) {
                soundManager.play(SoundType.COMPLETE)
                _lootResult.value = result
            }
        }
    }

    /** Dismisses the Lucky Chest reveal dialog. */
    fun onLootDismiss() {
        _lootResult.value = null
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
        boosts: Map<String, Int>,
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
        boosts = ShopCatalog.boosts.map { item ->
            BoostUi(
                item = item,
                owned = boosts[item.id] ?: 0,
                activeQuizzesLeft = when (item.type) {
                    BoostType.COINS -> state.coinBoostQuizzesLeft
                    BoostType.XP -> state.xpBoostQuizzesLeft
                }
            )
        },
        titles = ShopCatalog.titles.map { item ->
            TitleUi(
                item = item,
                owned = item.id in state.ownedItemIds,
                equipped = item.id == state.equippedTitleId
            )
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
