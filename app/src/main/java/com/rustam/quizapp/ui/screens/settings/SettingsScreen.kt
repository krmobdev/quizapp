package com.rustam.quizapp.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rustam.quizapp.R
import com.rustam.quizapp.data.AppLanguage
import com.rustam.quizapp.data.ThemeMode
import com.rustam.quizapp.ui.components.AppDimens
import com.rustam.quizapp.ui.components.GlassCard
import com.rustam.quizapp.ui.components.ScreenSubtitle
import com.rustam.quizapp.ui.components.ScreenTitle
import com.rustam.quizapp.ui.components.SettingChoiceCard
import com.rustam.quizapp.ui.components.appTextColor
import com.rustam.quizapp.ui.components.rememberAppThemeColors
import com.rustam.quizapp.ui.localization.labelRes
import com.rustam.quizapp.ui.theme.QuizappTheme

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel()
) {
    val soundEnabled by viewModel.soundEnabled.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()
    SettingsContent(
        soundEnabled = soundEnabled,
        themeMode = themeMode,
        appLanguage = appLanguage,
        onSoundEnabledChange = viewModel::setSoundEnabled,
        onThemeModeChange = viewModel::setThemeMode,
        onAppLanguageChange = viewModel::setAppLanguage,
        modifier = modifier
    )
}

@Composable
private fun SettingsContent(
    soundEnabled: Boolean,
    themeMode: ThemeMode,
    appLanguage: AppLanguage,
    onSoundEnabledChange: (Boolean) -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onAppLanguageChange: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = rememberAppThemeColors()
    val textColor = appTextColor()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ScreenTitle(stringResource(R.string.settings))

        SoundSettingCard(
            title = stringResource(R.string.settings_sound_title),
            subtitle = stringResource(R.string.settings_sound_subtitle),
            checked = soundEnabled,
            onCheckedChange = onSoundEnabledChange,
            colors = colors,
            textColor = textColor
        )

        SettingsSection(
            title = stringResource(R.string.settings_language_title),
            subtitle = stringResource(R.string.settings_language_subtitle),
            colors = colors
        ) {
            AppLanguage.entries.forEach { language ->
                SettingChoiceCard(
                    label = stringResource(language.labelRes),
                    selected = appLanguage == language,
                    onClick = { onAppLanguageChange(language) }
                )
            }
        }

        SettingsSection(
            title = stringResource(R.string.settings_theme_title),
            subtitle = stringResource(R.string.settings_theme_subtitle),
            colors = colors
        ) {
            ThemeMode.entries.forEach { mode ->
                SettingChoiceCard(
                    label = stringResource(mode.labelRes),
                    selected = themeMode == mode,
                    onClick = { onThemeModeChange(mode) }
                )
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun SettingsSection(
    title: String,
    subtitle: String,
    colors: com.rustam.quizapp.ui.components.AppThemeColors,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    GlassCard(colors = colors, modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppDimens.CardPaddingH, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = appTextColor()
            )
            ScreenSubtitle(subtitle)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun SoundSettingCard(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    colors: com.rustam.quizapp.ui.components.AppThemeColors,
    textColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier, colors = colors) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(AppDimens.SettingChoiceHeight)
                .padding(horizontal = AppDimens.CardPaddingH),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
                ScreenSubtitle(subtitle)
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = textColor,
                    checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                    uncheckedThumbColor = textColor,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    uncheckedBorderColor = colors.glassBorder
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsContentPreview() {
    QuizappTheme {
        SettingsContent(
            soundEnabled = true,
            themeMode = ThemeMode.SYSTEM,
            appLanguage = AppLanguage.RU,
            onSoundEnabledChange = {},
            onThemeModeChange = {},
            onAppLanguageChange = {}
        )
    }
}