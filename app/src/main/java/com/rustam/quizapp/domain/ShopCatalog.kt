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

/**
 * A buyable cosmetic title shown on the player card (e.g. "🔥 Огненный разум"). Purely a status
 * symbol — owned via the same `owned_item` table as avatars/themes and equipped like them. Prices
 * climb steeply so titles stay a long-term coin sink and a reason to keep earning.
 */
data class TitleItem(
    val id: String,
    val emoji: String,
    @param:StringRes val labelRes: Int,
    val priceCoins: Int
)

/** The effect a [PowerUpItem] applies when used during a quiz. */
enum class PowerUpType { FIFTY_FIFTY, ADD_TIME, SKIP }

/** What a [BoostItem] doubles while it is active. */
enum class BoostType { COINS, XP }

/**
 * A temporary, consumable boost ("ништяк"): once switched on it doubles its [type] reward for the
 * next [quizzes] finished quizzes. Bought into the backpack like boosters, then activated from
 * there — it nudges the player to keep playing while the charges last.
 */
data class BoostItem(
    val id: String,
    val type: BoostType,
    val emoji: String,
    @param:StringRes val labelRes: Int,
    @param:StringRes val descRes: Int,
    val priceCoins: Int,
    val multiplier: Int,
    val quizzes: Int
)

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
        AvatarItem("avatar_phoenix", "🔥", 750),
        AvatarItem("avatar_dog", "🐶", 150),
        AvatarItem("avatar_rabbit", "🐰", 150),
        AvatarItem("avatar_tiger", "🐯", 200),
        AvatarItem("avatar_koala", "🐨", 200),
        AvatarItem("avatar_bee", "🐝", 200),
        AvatarItem("avatar_turtle", "🐢", 250),
        AvatarItem("avatar_monkey", "🐵", 250),
        AvatarItem("avatar_eagle", "🦅", 300),
        AvatarItem("avatar_butterfly", "🦋", 300),
        AvatarItem("avatar_parrot", "🦜", 350),
        AvatarItem("avatar_shark", "🦈", 350),
        AvatarItem("avatar_wolf", "🐺", 400),
        AvatarItem("avatar_raccoon", "🦝", 400),
        AvatarItem("avatar_dino", "🦖", 450),
        AvatarItem("avatar_cowboy", "🤠", 500),
        AvatarItem("avatar_tophat", "🎩", 550),
        AvatarItem("avatar_vampire", "🧛", 600),
        AvatarItem("avatar_merperson", "🧜", 650),
        AvatarItem("avatar_peacock", "🦚", 700),
        AvatarItem("avatar_superhero", "🦸", 800),
        AvatarItem("avatar_star", "🌟", 900),
        AvatarItem("avatar_gem", "💎", 1100),
        AvatarItem("avatar_genie", "🧞", 1500)
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
        ThemeItem("theme_midnight", R.string.theme_accent_midnight, 500),
        ThemeItem("theme_aqua", R.string.theme_accent_aqua, 350),
        ThemeItem("theme_lavender", R.string.theme_accent_lavender, 400),
        ThemeItem("theme_slate", R.string.theme_accent_slate, 550)
    )

    /**
     * Consumable XP boosters. The base booster grants 1000 free XP for 500 coins;
     * the larger tiers give better value to reward saving up.
     */
    val boosters: List<BoosterItem> = listOf(
        BoosterItem("booster_xp_small", "📘", R.string.shop_booster_small, 500, 1000),
        BoosterItem("booster_xp_medium", "📗", R.string.shop_booster_medium, 1200, 2500),
        BoosterItem("booster_xp_large", "📕", R.string.shop_booster_large, 2200, 5000),
        BoosterItem("booster_xp_huge", "📚", R.string.shop_booster_huge, 4000, 11000)
    )

    fun booster(id: String): BoosterItem? = boosters.find { it.id == id }

    /**
     * Cosmetic titles, ordered from cheap to prestigious. The steep price ladder gives coins a
     * long-term purpose: the top titles take many strong quizzes (or saved daily rewards) to afford.
     */
    val titles: List<TitleItem> = listOf(
        TitleItem("title_curious", "🌱", R.string.title_curious, 300),
        TitleItem("title_bookworm", "📖", R.string.title_bookworm, 500),
        TitleItem("title_sharp", "⚡", R.string.title_sharp, 750),
        TitleItem("title_scholar", "🎓", R.string.title_scholar, 1000),
        TitleItem("title_genius", "🧠", R.string.title_genius, 1400),
        TitleItem("title_quizmaster", "🏅", R.string.title_quizmaster, 1900),
        TitleItem("title_sage", "🦉", R.string.title_sage, 2500),
        TitleItem("title_champion", "🏆", R.string.title_champion, 3200),
        TitleItem("title_legend", "🌟", R.string.title_legend, 4200),
        TitleItem("title_firemind", "🔥", R.string.title_firemind, 5500),
        TitleItem("title_cosmic", "🌌", R.string.title_cosmic, 7500),
        TitleItem("title_immortal", "💎", R.string.title_immortal, 10000)
    )

    fun title(id: String?): TitleItem? = id?.let { tid -> titles.find { it.id == tid } }

    /** In-quiz power-ups, used mid-question. Cheap because they are spent every quiz. */
    val powerUps: List<PowerUpItem> = listOf(
        PowerUpItem("pu_fifty", PowerUpType.FIFTY_FIFTY, "✂️", R.string.powerup_fifty_title, R.string.powerup_fifty_desc, 120),
        PowerUpItem("pu_time", PowerUpType.ADD_TIME, "⏱️", R.string.powerup_time_title, R.string.powerup_time_desc, 80),
        PowerUpItem("pu_skip", PowerUpType.SKIP, "⏭️", R.string.powerup_skip_title, R.string.powerup_skip_desc, 100)
    )

    fun powerUp(id: String): PowerUpItem? = powerUps.find { it.id == id }

    /**
     * Temporary 2× reward boosts. Cheap enough to buy often, but each only lasts a few quizzes —
     * so the player has to come back and play to spend the charges.
     */
    val boosts: List<BoostItem> = listOf(
        BoostItem("boost_coins", BoostType.COINS, "💰", R.string.boost_coins_title, R.string.boost_coins_desc, 350, 2, 3),
        BoostItem("boost_xp", BoostType.XP, "🎓", R.string.boost_xp_title, R.string.boost_xp_desc, 350, 2, 3)
    )

    fun boost(id: String): BoostItem? = boosts.find { it.id == id }

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
