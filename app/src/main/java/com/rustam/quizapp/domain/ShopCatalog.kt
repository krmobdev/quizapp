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

    /** Items that every player owns from the start (the free defaults). */
    val freeItemIds: Set<String> =
        avatars.filter { it.priceCoins == 0 }.map { it.id }.toSet() +
            themes.filter { it.priceCoins == 0 }.map { it.id }.toSet()

    fun avatarEmoji(id: String): String =
        avatars.find { it.id == id }?.emoji ?: avatars.first().emoji
}
