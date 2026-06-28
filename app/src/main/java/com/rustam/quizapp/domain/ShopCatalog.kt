package com.rustam.quizapp.domain

import androidx.annotation.StringRes
import com.rustam.quizapp.R

/**
 * A buyable profile avatar (an emoji). Most cost coins; a few premium ones cost [priceGems] (the
 * rare currency) instead, in which case [priceCoins] is 0.
 */
data class AvatarItem(
    val id: String,
    val emoji: String,
    val priceCoins: Int,
    val priceGems: Int = 0
)

/**
 * A buyable accent colour theme. Colours live in [com.rustam.quizapp.ui.theme.AccentTheme].
 * Most cost coins; premium ones cost [priceGems] instead, in which case [priceCoins] is 0.
 */
data class ThemeItem(
    val id: String,
    @param:StringRes val labelRes: Int,
    val priceCoins: Int,
    val priceGems: Int = 0
)

/**
 * A gem-only bundle: spend [priceGems] to receive a fixed mix of [coins], free [xp] and/or
 * inventory [items] (id to count, referencing booster/boost/power-up ids). Powers both the
 * gem→coin exchange (coins only) and the premium consumable packs (items), so one purchase path
 * covers every non-cosmetic gem sink.
 */
data class GemBundle(
    val id: String,
    val emoji: String,
    @param:StringRes val labelRes: Int,
    val priceGems: Int,
    val coins: Int = 0,
    val xp: Int = 0,
    val items: List<Pair<String, Int>> = emptyList()
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
 * symbol — owned via the same `owned_item` table as avatars/themes and equipped like them.
 */
data class TitleItem(
    val id: String,
    val emoji: String,
    @param:StringRes val labelRes: Int,
    val priceCoins: Int,
    val priceGems: Int = 0
)

/** The effect a [PowerUpItem] applies when used during a quiz. */
enum class PowerUpType { FIFTY_FIFTY, ADD_TIME, SKIP }

/** What a [BoostItem] doubles while it is active. */
enum class BoostType { COINS, XP }

/**
 * A temporary, consumable boost: once switched on it doubles its [type] reward for the
 * next [quizzes] finished quizzes.
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
 * stored in the backpack. [packSize] is how many uses one purchase grants (bundles).
 */
data class PowerUpItem(
    val id: String,
    val type: PowerUpType,
    val emoji: String,
    @param:StringRes val labelRes: Int,
    @param:StringRes val descRes: Int,
    val priceCoins: Int,
    val packSize: Int = 1
)

/**
 * Static catalogue of cosmetic items sold for coins. Prices use [EconomyBalance.scale] (−40%).
 */
object ShopCatalog {
    const val DEFAULT_AVATAR_ID = "avatar_smile"
    const val DEFAULT_THEME_ID = "theme_mint"

    private fun c(value: Int): Int = EconomyBalance.scale(value)

    val avatars: List<AvatarItem> = listOf(
        AvatarItem(DEFAULT_AVATAR_ID, "🙂", 0),
        AvatarItem("avatar_cat", "🐱", c(150)),
        AvatarItem("avatar_fox", "🦊", c(150)),
        AvatarItem("avatar_panda", "🐼", c(150)),
        AvatarItem("avatar_owl", "🦉", c(200)),
        AvatarItem("avatar_alien", "👽", c(250)),
        AvatarItem("avatar_robot", "🤖", c(250)),
        AvatarItem("avatar_brain", "🧠", c(300)),
        AvatarItem("avatar_rocket", "🚀", c(300)),
        AvatarItem("avatar_penguin", "🐧", c(200)),
        AvatarItem("avatar_ghost", "👻", c(250)),
        AvatarItem("avatar_octopus", "🐙", c(250)),
        AvatarItem("avatar_lion", "🦁", c(350)),
        AvatarItem("avatar_unicorn", "🦄", c(400)),
        AvatarItem("avatar_ninja", "🥷", c(400)),
        AvatarItem("avatar_wizard", "🧙", c(450)),
        AvatarItem("avatar_crown", "👑", c(450)),
        AvatarItem("avatar_devil", "😈", c(500)),
        AvatarItem("avatar_dragon", "🐉", c(600)),
        AvatarItem("avatar_phoenix", "🔥", c(750)),
        AvatarItem("avatar_dog", "🐶", c(150)),
        AvatarItem("avatar_rabbit", "🐰", c(150)),
        AvatarItem("avatar_tiger", "🐯", c(200)),
        AvatarItem("avatar_koala", "🐨", c(200)),
        AvatarItem("avatar_bee", "🐝", c(200)),
        AvatarItem("avatar_turtle", "🐢", c(250)),
        AvatarItem("avatar_monkey", "🐵", c(250)),
        AvatarItem("avatar_eagle", "🦅", c(300)),
        AvatarItem("avatar_butterfly", "🦋", c(300)),
        AvatarItem("avatar_parrot", "🦜", c(350)),
        AvatarItem("avatar_shark", "🦈", c(350)),
        AvatarItem("avatar_wolf", "🐺", c(400)),
        AvatarItem("avatar_raccoon", "🦝", c(400)),
        AvatarItem("avatar_dino", "🦖", c(450)),
        AvatarItem("avatar_cowboy", "🤠", c(500)),
        AvatarItem("avatar_tophat", "🎩", c(550)),
        AvatarItem("avatar_vampire", "🧛", c(600)),
        AvatarItem("avatar_merperson", "🧜", c(650)),
        AvatarItem("avatar_peacock", "🦚", c(700)),
        AvatarItem("avatar_superhero", "🦸", c(800)),
        AvatarItem("avatar_star", "🌟", c(900)),
        AvatarItem("avatar_gem", "💎", c(1100)),
        AvatarItem("avatar_genie", "🧞", c(1500)),
        AvatarItem("avatar_sloth", "🦥", c(200)),
        AvatarItem("avatar_otter", "🦦", c(250)),
        AvatarItem("avatar_beaver", "🦫", c(250)),
        AvatarItem("avatar_zebra", "🦓", c(300)),
        AvatarItem("avatar_giraffe", "🦒", c(350)),
        AvatarItem("avatar_hedgehog", "🦔", c(300)),
        AvatarItem("avatar_whale", "🐳", c(500)),
        AvatarItem("avatar_seal", "🦭", c(450)),
        AvatarItem("avatar_swan", "🦢", c(550)),
        AvatarItem("avatar_flamingo", "🦩", c(600)),
        AvatarItem("avatar_mammoth", "🦣", c(1000)),
        AvatarItem("avatar_comet", "☄️", c(1300))
    )

    /** Premium avatars bought only with gems (the rare currency). */
    val premiumAvatars: List<AvatarItem> = listOf(
        AvatarItem("avatar_gem_crown", "👑", priceCoins = 0, priceGems = 60),
        AvatarItem("avatar_gem_galaxy", "🌌", priceCoins = 0, priceGems = 80),
        AvatarItem("avatar_gem_trophy", "🏆", priceCoins = 0, priceGems = 100),
        AvatarItem("avatar_gem_diamond", "💠", priceCoins = 0, priceGems = 130),
        AvatarItem("avatar_gem_infinity", "♾️", priceCoins = 0, priceGems = 180)
    )

    val themes: List<ThemeItem> = listOf(
        ThemeItem(DEFAULT_THEME_ID, R.string.theme_accent_mint, 0),
        ThemeItem("theme_ocean", R.string.theme_accent_ocean, c(200)),
        ThemeItem("theme_grape", R.string.theme_accent_grape, c(250)),
        ThemeItem("theme_forest", R.string.theme_accent_forest, c(300)),
        ThemeItem("theme_sunset", R.string.theme_accent_sunset, c(300)),
        ThemeItem("theme_rose", R.string.theme_accent_rose, c(350)),
        ThemeItem("theme_crimson", R.string.theme_accent_crimson, c(400)),
        ThemeItem("theme_gold", R.string.theme_accent_gold, c(450)),
        ThemeItem("theme_midnight", R.string.theme_accent_midnight, c(500)),
        ThemeItem("theme_aqua", R.string.theme_accent_aqua, c(350)),
        ThemeItem("theme_lavender", R.string.theme_accent_lavender, c(400)),
        ThemeItem("theme_slate", R.string.theme_accent_slate, c(550)),
        ThemeItem("theme_ember", R.string.theme_accent_ember, c(450)),
        ThemeItem("theme_jade", R.string.theme_accent_jade, c(500)),
        ThemeItem("theme_cosmos", R.string.theme_accent_cosmos, c(650))
    )

    /** Premium accent themes bought only with gems. */
    val premiumThemes: List<ThemeItem> = listOf(
        ThemeItem("theme_gem_aurora", R.string.theme_accent_aurora, priceCoins = 0, priceGems = 50),
        ThemeItem("theme_gem_obsidian", R.string.theme_accent_obsidian, priceCoins = 0, priceGems = 70),
        ThemeItem("theme_gem_prism", R.string.theme_accent_prism, priceCoins = 0, priceGems = 90)
    )

    /**
     * Gem bundles: the gem→coin exchange (coins only, better rate in bulk) plus premium consumable
     * packs (inventory items at a discount vs their coin price). One [purchaseGemBundle] path serves
     * both. Item ids reference existing power-ups / boosts / boosters.
     */
    val gemBundles: List<GemBundle> = listOf(
        GemBundle("gem_exchange_s", "🪙", R.string.gem_exchange_small, priceGems = 10, coins = 1200),
        GemBundle("gem_exchange_m", "💰", R.string.gem_exchange_medium, priceGems = 25, coins = 3300),
        GemBundle("gem_exchange_l", "🏦", R.string.gem_exchange_large, priceGems = 60, coins = 8500),
        GemBundle(
            "gem_bundle_power", "🎒", R.string.gem_bundle_power, priceGems = 30,
            items = listOf("pu_fifty" to 4, "pu_time" to 4, "pu_skip" to 4)
        ),
        GemBundle(
            "gem_bundle_boost", "⚡", R.string.gem_bundle_boost, priceGems = 35,
            items = listOf("boost_coins_long" to 2, "boost_xp_long" to 2)
        ),
        GemBundle(
            "gem_bundle_booster", "📦", R.string.gem_bundle_booster, priceGems = 40,
            items = listOf("booster_xp_colossal" to 2)
        )
    )

    fun gemBundle(id: String): GemBundle? = gemBundles.find { it.id == id }

    val boosters: List<BoosterItem> = listOf(
        BoosterItem("booster_xp_small", "📘", R.string.shop_booster_small, c(600), c(900)),
        BoosterItem("booster_xp_medium", "📗", R.string.shop_booster_medium, c(1400), c(2200)),
        BoosterItem("booster_xp_large", "📕", R.string.shop_booster_large, c(2600), c(4500)),
        BoosterItem("booster_xp_huge", "📚", R.string.shop_booster_huge, c(4800), c(9500)),
        BoosterItem("booster_xp_colossal", "📦", R.string.shop_booster_colossal, c(8500), c(18000))
    )

    fun booster(id: String): BoosterItem? = boosters.find { it.id == id }

    val titles: List<TitleItem> = listOf(
        TitleItem("title_curious", "🌱", R.string.title_curious, c(300)),
        TitleItem("title_bookworm", "📖", R.string.title_bookworm, c(500)),
        TitleItem("title_sharp", "⚡", R.string.title_sharp, c(750)),
        TitleItem("title_scholar", "🎓", R.string.title_scholar, c(1000)),
        TitleItem("title_genius", "🧠", R.string.title_genius, c(1400)),
        TitleItem("title_quizmaster", "🏅", R.string.title_quizmaster, c(1900)),
        TitleItem("title_sage", "🦉", R.string.title_sage, c(2500)),
        TitleItem("title_champion", "🏆", R.string.title_champion, c(3200)),
        TitleItem("title_legend", "🌟", R.string.title_legend, c(4200)),
        TitleItem("title_firemind", "🔥", R.string.title_firemind, c(5500)),
        TitleItem("title_cosmic", "🌌", R.string.title_cosmic, c(7500)),
        TitleItem("title_immortal", "💎", R.string.title_immortal, c(12000)),
        TitleItem("title_explorer", "🧭", R.string.title_explorer, c(900)),
        TitleItem("title_strategist", "♟️", R.string.title_strategist, c(1700)),
        TitleItem("title_virtuoso", "🎻", R.string.title_virtuoso, c(2900)),
        TitleItem("title_titan", "🗿", R.string.title_titan, c(6500)),
        TitleItem("title_oracle", "🔮", R.string.title_oracle, c(9000))
    )

    /** Premium titles bought only with gems. */
    val premiumTitles: List<TitleItem> = listOf(
        TitleItem("title_gem_mythic", "💎", R.string.title_gem_mythic, priceCoins = 0, priceGems = 70),
        TitleItem("title_gem_ascended", "🌠", R.string.title_gem_ascended, priceCoins = 0, priceGems = 110),
        TitleItem("title_gem_eternal", "♾️", R.string.title_gem_eternal, priceCoins = 0, priceGems = 160)
    )

    fun title(id: String?): TitleItem? =
        id?.let { tid -> (titles + premiumTitles).find { it.id == tid } }

    /** In-quiz power-ups — single uses and discounted bundles for quiz mode. */
    val powerUps: List<PowerUpItem> = listOf(
        PowerUpItem("pu_fifty", PowerUpType.FIFTY_FIFTY, "✂️", R.string.powerup_fifty_title, R.string.powerup_fifty_desc, c(120)),
        PowerUpItem("pu_time", PowerUpType.ADD_TIME, "⏱️", R.string.powerup_time_title, R.string.powerup_time_desc, c(80)),
        PowerUpItem("pu_skip", PowerUpType.SKIP, "⏭️", R.string.powerup_skip_title, R.string.powerup_skip_desc, c(100)),
        PowerUpItem("pu_fifty_pack", PowerUpType.FIFTY_FIFTY, "✂️", R.string.powerup_fifty_pack_title, R.string.powerup_fifty_pack_desc, c(300), packSize = 3),
        PowerUpItem("pu_time_pack", PowerUpType.ADD_TIME, "⏱️", R.string.powerup_time_pack_title, R.string.powerup_time_pack_desc, c(200), packSize = 3),
        PowerUpItem("pu_skip_pack", PowerUpType.SKIP, "⏭️", R.string.powerup_skip_pack_title, R.string.powerup_skip_pack_desc, c(250), packSize = 3),
        PowerUpItem("pu_fifty_mega", PowerUpType.FIFTY_FIFTY, "✂️", R.string.powerup_fifty_mega_title, R.string.powerup_fifty_mega_desc, c(450), packSize = 6),
        PowerUpItem("pu_time_mega", PowerUpType.ADD_TIME, "⏱️", R.string.powerup_time_mega_title, R.string.powerup_time_mega_desc, c(300), packSize = 6),
        PowerUpItem("pu_skip_mega", PowerUpType.SKIP, "⏭️", R.string.powerup_skip_mega_title, R.string.powerup_skip_mega_desc, c(380), packSize = 6)
    )

    fun powerUp(id: String): PowerUpItem? = powerUps.find { it.id == id }

    fun powerUpsOfType(type: PowerUpType): List<PowerUpItem> =
        powerUps.filter { it.type == type }

    val boosts: List<BoostItem> = listOf(
        BoostItem("boost_coins", BoostType.COINS, "💰", R.string.boost_coins_title, R.string.boost_coins_desc, c(350), 2, 3),
        BoostItem("boost_xp", BoostType.XP, "🎓", R.string.boost_xp_title, R.string.boost_xp_desc, c(350), 2, 3),
        BoostItem("boost_coins_long", BoostType.COINS, "💰", R.string.boost_coins_long_title, R.string.boost_coins_long_desc, c(650), 2, 6),
        BoostItem("boost_xp_long", BoostType.XP, "🎓", R.string.boost_xp_long_title, R.string.boost_xp_long_desc, c(650), 2, 6)
    )

    fun boost(id: String): BoostItem? = boosts.find { it.id == id }

    const val STREAK_FREEZE_EMOJI = "🧊"
    val STREAK_FREEZE_PRICE: Int = EconomyBalance.scale(300)

    /** Free (default) cosmetics — only the zero-price coin items; premium gem items are NOT free. */
    val freeItemIds: Set<String> =
        avatars.filter { it.priceCoins == 0 && it.priceGems == 0 }.map { it.id }.toSet() +
            themes.filter { it.priceCoins == 0 && it.priceGems == 0 }.map { it.id }.toSet()

    fun avatarEmoji(id: String): String =
        (avatars + premiumAvatars).find { it.id == id }?.emoji ?: avatars.first().emoji
}