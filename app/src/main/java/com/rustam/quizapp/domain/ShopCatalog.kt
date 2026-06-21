package com.rustam.quizapp.domain

import androidx.annotation.StringRes
import com.rustam.quizapp.R

/** A buyable profile avatar (an emoji). */
data class AvatarItem(
    val id: String,
    val emoji: String,
    val priceCoins: Int
)

/** A buyable accent colour theme. Colours live in [com.rustam.quizapp.ui.theme.AccentTheme]. */
data class ThemeItem(
    val id: String,
    @param:StringRes val labelRes: Int,
    val priceCoins: Int
)

/**
 * A consumable booster bought with coins and stored in the player's backpack.
 * Activating one from the inventory grants [rewardPoints] free XP.
 */
data class BoosterItem(
    val id: String,
    val emoji: String,
    @param:StringRes val labelRes: Int,
    val priceCoins: Int,
    val rewardPoints: Int
)

/** The effect a [PowerUpItem] applies when used during a quiz. */
enum class PowerUpType { FIFTY_FIFTY, ADD_TIME, SKIP }

/**
 * A consumable used during a quiz (e.g. 50/50, extra time, skip). Bought with coins and
 * stored in the backpack like a booster, but consumed mid-question instead of granting XP.
 */
data class PowerUpItem(
    val id: String,
    val type: PowerUpType,
    val emoji: String,
    @param:StringRes val labelRes: Int,
    @param:StringRes val descRes: Int,
    val priceCoins: Int
)

/**
 * Static catalogue of cosmetic items sold for coins. Prices are tuned so that
 * items take several quizzes to afford, giving coins a purpose.
 *
 * The free items ([DEFAULT_AVATAR_ID], [DEFAULT_THEME_ID]) are always considered owned.
 */
object ShopCatalog {
    const val DEFAULT_AVATAR_ID = "avatar_smile"
    const val DEFAULT_THEME_ID = "theme_mint"

    val avatars: List<AvatarItem> = listOf(
        AvatarItem(DEFAULT_AVATAR_ID, "🙂", 0),
        AvatarItem("avatar_cat", "🐱", 150),
        AvatarItem("avatar_fox", "🦊", 150),
        AvatarItem("avatar_panda", "🐼", 150),
        AvatarItem("avatar_owl", "🦉", 200),
        AvatarItem("avatar_alien", "👽", 250),
        AvatarItem("avatar_robot", "🤖", 250),
        AvatarItem("avatar_brain", "🧠", 300),
        AvatarItem("avatar_rocket", "🚀", 300),
        AvatarItem("avatar_penguin", "🐧", 200),
        AvatarItem("avatar_ghost", "👻", 250),
        AvatarItem("avatar_octopus", "🐙", 250),
        AvatarItem("avatar_lion", "🦁", 350),
        AvatarItem("avatar_unicorn", "🦄", 400),
        AvatarItem("avatar_ninja", "🥷", 400),
        AvatarItem("avatar_wizard", "🧙", 450),
        AvatarItem("avatar_crown", "👑", 450),
        AvatarItem("avatar_devil", "😈", 500),
        AvatarItem("avatar_dragon", "🐉", 600),
        AvatarItem("avatar_phoenix", "🔥", 750)
    )

    val themes: List<ThemeItem> = listOf(
        ThemeItem(DEFAULT_THEME_ID, R.string.theme_accent_mint, 0),
        ThemeItem("theme_ocean", R.string.theme_accent_ocean, 200),
        ThemeItem("theme_grape", R.string.theme_accent_grape, 250),
        ThemeItem("theme_forest", R.string.theme_accent_forest, 300),
        ThemeItem("theme_sunset", R.string.theme_accent_sunset, 300),
        ThemeItem("theme_rose", R.string.theme_accent_rose, 350),
        ThemeItem("theme_crimson", R.string.theme_accent_crimson, 400),
        ThemeItem("theme_gold", R.string.theme_accent_gold, 450),
        ThemeItem("theme_midnight", R.string.theme_accent_midnight, 500)
    )

    /**
     * Consumable XP boosters. The base booster grants 1000 free XP for 500 coins;
     * the larger tiers give better value to reward saving up.
     */
    val boosters: List<BoosterItem> = listOf(
        BoosterItem("booster_xp_small", "📘", R.string.shop_booster_small, 500, 1000),
        BoosterItem("booster_xp_medium", "📗", R.string.shop_booster_medium, 1200, 2500),
        BoosterItem("booster_xp_large", "📕", R.string.shop_booster_large, 2200, 5000)
    )

    fun booster(id: String): BoosterItem? = boosters.find { it.id == id }

    /** In-quiz power-ups, used mid-question. Cheap because they are spent every quiz. */
    val powerUps: List<PowerUpItem> = listOf(
        PowerUpItem("pu_fifty", PowerUpType.FIFTY_FIFTY, "✂️", R.string.powerup_fifty_title, R.string.powerup_fifty_desc, 120),
        PowerUpItem("pu_time", PowerUpType.ADD_TIME, "⏱️", R.string.powerup_time_title, R.string.powerup_time_desc, 80),
        PowerUpItem("pu_skip", PowerUpType.SKIP, "⏭️", R.string.powerup_skip_title, R.string.powerup_skip_desc, 100)
    )

    fun powerUp(id: String): PowerUpItem? = powerUps.find { it.id == id }

    /** Streak Freeze: a consumable that auto-protects the daily streak when a day is missed. */
    const val STREAK_FREEZE_EMOJI = "🧊"
    const val STREAK_FREEZE_PRICE = 300

    /** Items that every player owns from the start (the free defaults). */
    val freeItemIds: Set<String> =
        avatars.filter { it.priceCoins == 0 }.map { it.id }.toSet() +
            themes.filter { it.priceCoins == 0 }.map { it.id }.toSet()

    fun avatarEmoji(id: String): String =
        avatars.find { it.id == id }?.emoji ?: avatars.first().emoji
}
