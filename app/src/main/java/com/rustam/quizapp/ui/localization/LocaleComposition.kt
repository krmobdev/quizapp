package com.rustam.quizapp.ui.localization

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.rustam.quizapp.data.AppLanguage
import java.util.Locale

@Composable
fun LocalizedApp(
    language: AppLanguage,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val locale = remember(language) { Locale.forLanguageTag(language.tag) }
    val localizedContext = remember(language) {
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        context.createConfigurationContext(configuration)
    }
    val configuration = remember(language) {
        Configuration(context.resources.configuration).apply { setLocale(locale) }
    }
    CompositionLocalProvider(
        LocalContext provides localizedContext,
        LocalConfiguration provides configuration,
    ) {
        content()
    }
}