package com.rustam.quizapp.ui.theme

import androidx.compose.ui.graphics.Color
import com.rustam.quizapp.domain.ShopCatalog

/**
 * Accent colour palettes sold in the shop. Each palette overrides the
 * primary / tertiary / container roles of the base [QuizappTheme] scheme,
 * for both light and dark modes. [swatch] is the colour shown on the shop card.
 *
 * The palette id matches the corresponding [com.rustam.quizapp.domain.ThemeItem] id.
 */
enum class AccentTheme(
    val id: String,
    val swatch: Color,
    val primaryLight: Color,
    val onPrimaryLight: Color,
    val containerLight: Color,
    val tertiaryLight: Color,
    val primaryDark: Color,
    val onPrimaryDark: Color,
    val containerDark: Color,
    val tertiaryDark: Color
) {
    MINT(
        id = ShopCatalog.DEFAULT_THEME_ID,
        swatch = Color(0xFF1FB8A0),
        primaryLight = Color(0xFF006B5B), onPrimaryLight = Color.White,
        containerLight = Color(0xFF6FF7DD), tertiaryLight = Color(0xFF3A6470),
        primaryDark = Color(0xFF50DBC0), onPrimaryDark = Color(0xFF00382F),
        containerDark = Color(0xFF005144), tertiaryDark = Color(0xFFA2CEDC)
    ),
    OCEAN(
        id = "theme_ocean",
        swatch = Color(0xFF2C7BD6),
        primaryLight = Color(0xFF0061A4), onPrimaryLight = Color.White,
        containerLight = Color(0xFFD1E4FF), tertiaryLight = Color(0xFF3F5C77),
        primaryDark = Color(0xFF9FCAFF), onPrimaryDark = Color(0xFF003258),
        containerDark = Color(0xFF00497D), tertiaryDark = Color(0xFFAAC7E0)
    ),
    GRAPE(
        id = "theme_grape",
        swatch = Color(0xFF7A5BD0),
        primaryLight = Color(0xFF6750A4), onPrimaryLight = Color.White,
        containerLight = Color(0xFFEADDFF), tertiaryLight = Color(0xFF7D5260),
        primaryDark = Color(0xFFD0BCFF), onPrimaryDark = Color(0xFF381E72),
        containerDark = Color(0xFF4F378B), tertiaryDark = Color(0xFFEFB8C8)
    ),
    SUNSET(
        id = "theme_sunset",
        swatch = Color(0xFFE0651C),
        primaryLight = Color(0xFFA23E00), onPrimaryLight = Color.White,
        containerLight = Color(0xFFFFDBCC), tertiaryLight = Color(0xFF6F5B40),
        primaryDark = Color(0xFFFFB59B), onPrimaryDark = Color(0xFF5B1A00),
        containerDark = Color(0xFF7D2D10), tertiaryDark = Color(0xFFDDC3A2)
    ),
    ROSE(
        id = "theme_rose",
        swatch = Color(0xFFD63B72),
        primaryLight = Color(0xFFB3275E), onPrimaryLight = Color.White,
        containerLight = Color(0xFFFFD9E2), tertiaryLight = Color(0xFF7E5260),
        primaryDark = Color(0xFFFFB1C8), onPrimaryDark = Color(0xFF5E1133),
        containerDark = Color(0xFF8E2949), tertiaryDark = Color(0xFFE5BAD0)
    ),
    GOLD(
        id = "theme_gold",
        swatch = Color(0xFFD9A521),
        primaryLight = Color(0xFF7D5700), onPrimaryLight = Color.White,
        containerLight = Color(0xFFFFDEA6), tertiaryLight = Color(0xFF50643F),
        primaryDark = Color(0xFFF5BD48), onPrimaryDark = Color(0xFF422C00),
        containerDark = Color(0xFF5F4100), tertiaryDark = Color(0xFFB5CC9C)
    ),
    FOREST(
        id = "theme_forest",
        swatch = Color(0xFF3F9D4F),
        primaryLight = Color(0xFF2A6B30), onPrimaryLight = Color.White,
        containerLight = Color(0xFFABF5A6), tertiaryLight = Color(0xFF3A6470),
        primaryDark = Color(0xFF8FD98C), onPrimaryDark = Color(0xFF00390A),
        containerDark = Color(0xFF105218), tertiaryDark = Color(0xFFA2CEDC)
    ),
    CRIMSON(
        id = "theme_crimson",
        swatch = Color(0xFFD33A3A),
        primaryLight = Color(0xFFB3261E), onPrimaryLight = Color.White,
        containerLight = Color(0xFFFFDAD5), tertiaryLight = Color(0xFF775656),
        primaryDark = Color(0xFFFFB4AB), onPrimaryDark = Color(0xFF690004),
        containerDark = Color(0xFF93000A), tertiaryDark = Color(0xFFE7BDB7)
    ),
    MIDNIGHT(
        id = "theme_midnight",
        swatch = Color(0xFF4954C7),
        primaryLight = Color(0xFF3A45B0), onPrimaryLight = Color.White,
        containerLight = Color(0xFFDFE0FF), tertiaryLight = Color(0xFF585992),
        primaryDark = Color(0xFFBCC2FF), onPrimaryDark = Color(0xFF09155E),
        containerDark = Color(0xFF222C82), tertiaryDark = Color(0xFFC4C4F8)
    ),
    AQUA(
        id = "theme_aqua",
        swatch = Color(0xFF00BCD4),
        primaryLight = Color(0xFF006879), onPrimaryLight = Color.White,
        containerLight = Color(0xFFA5EEFF), tertiaryLight = Color(0xFF4D616C),
        primaryDark = Color(0xFF52D7F0), onPrimaryDark = Color(0xFF00363F),
        containerDark = Color(0xFF004E5A), tertiaryDark = Color(0xFFB4CAD3)
    ),
    LAVENDER(
        id = "theme_lavender",
        swatch = Color(0xFF9C7BE0),
        primaryLight = Color(0xFF6E4FB0), onPrimaryLight = Color.White,
        containerLight = Color(0xFFEBDDFF), tertiaryLight = Color(0xFF6B5C7E),
        primaryDark = Color(0xFFD3BBFF), onPrimaryDark = Color(0xFF3C1F70),
        containerDark = Color(0xFF553B8C), tertiaryDark = Color(0xFFD6C2E8)
    ),
    SLATE(
        id = "theme_slate",
        swatch = Color(0xFF5C7A99),
        primaryLight = Color(0xFF3D5A73), onPrimaryLight = Color.White,
        containerLight = Color(0xFFCDE5FF), tertiaryLight = Color(0xFF53606E),
        primaryDark = Color(0xFFA6C8E6), onPrimaryDark = Color(0xFF0A3048),
        containerDark = Color(0xFF254860), tertiaryDark = Color(0xFFBAC8D6)
    ),
    EMBER(
        id = "theme_ember",
        swatch = Color(0xFFE2502E),
        primaryLight = Color(0xFFB22E12), onPrimaryLight = Color.White,
        containerLight = Color(0xFFFFDAD2), tertiaryLight = Color(0xFF775650),
        primaryDark = Color(0xFFFFB4A4), onPrimaryDark = Color(0xFF5F1400),
        containerDark = Color(0xFF862200), tertiaryDark = Color(0xFFE7BDB4)
    ),
    JADE(
        id = "theme_jade",
        swatch = Color(0xFF18A66B),
        primaryLight = Color(0xFF006C4C), onPrimaryLight = Color.White,
        containerLight = Color(0xFF89F8C4), tertiaryLight = Color(0xFF4C6358),
        primaryDark = Color(0xFF6CDBA8), onPrimaryDark = Color(0xFF003825),
        containerDark = Color(0xFF005138), tertiaryDark = Color(0xFFB3CCBE)
    ),
    COSMOS(
        id = "theme_cosmos",
        swatch = Color(0xFF6A4DE0),
        primaryLight = Color(0xFF5238C2), onPrimaryLight = Color.White,
        containerLight = Color(0xFFE5DEFF), tertiaryLight = Color(0xFF5F5C71),
        primaryDark = Color(0xFFCABEFF), onPrimaryDark = Color(0xFF230E92),
        containerDark = Color(0xFF3B23AB), tertiaryDark = Color(0xFFC8C3DC)
    ),
    // Premium accent themes (bought with gems).
    AURORA(
        id = "theme_gem_aurora",
        swatch = Color(0xFF00BFA6),
        primaryLight = Color(0xFF00897B), onPrimaryLight = Color.White,
        containerLight = Color(0xFFB2F1E8), tertiaryLight = Color(0xFF4A6360),
        primaryDark = Color(0xFF5FE0CE), onPrimaryDark = Color(0xFF00382F),
        containerDark = Color(0xFF005047), tertiaryDark = Color(0xFFB1CCC7)
    ),
    OBSIDIAN(
        id = "theme_gem_obsidian",
        swatch = Color(0xFF37474F),
        primaryLight = Color(0xFF2E3D44), onPrimaryLight = Color.White,
        containerLight = Color(0xFFCDD8DE), tertiaryLight = Color(0xFF4F5B62),
        primaryDark = Color(0xFFB6C5CD), onPrimaryDark = Color(0xFF20292E),
        containerDark = Color(0xFF37444B), tertiaryDark = Color(0xFFBDC8CE)
    ),
    PRISM(
        id = "theme_gem_prism",
        swatch = Color(0xFFD81B9C),
        primaryLight = Color(0xFFB10A82), onPrimaryLight = Color.White,
        containerLight = Color(0xFFFFD7EE), tertiaryLight = Color(0xFF6F5563),
        primaryDark = Color(0xFFFFAEDC), onPrimaryDark = Color(0xFF5E0046),
        containerDark = Color(0xFF860062), tertiaryDark = Color(0xFFE3BDCF)
    );

    companion object {
        val DEFAULT = MINT
        fun fromId(id: String?): AccentTheme = entries.find { it.id == id } ?: DEFAULT
    }
}
