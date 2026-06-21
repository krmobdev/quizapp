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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rustam.quizapp.R
import com.rustam.quizapp.data.AppLanguage
import com.rustam.quizapp.data.ThemeMode
import com.rustam.quizapp.ui.components.AppDimens
import com.rustam.quizapp.ui.components.AppShapes
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
    val promoRedeemed by viewModel.promoRedeemed.collectAsState()
    val promoMessageRes by viewModel.promoMessageRes.collectAsState()
    val backupMessageRes by viewModel.backupMessageRes.collectAsState()

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri -> uri?.let(viewModel::exportProgress) }
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let(viewModel::importProgress) }

    SettingsContent(
        soundEnabled = soundEnabled,
        themeMode = themeMode,
        appLanguage = appLanguage,
        promoRedeemed = promoRedeemed,
        promoMessageRes = promoMessageRes,
        backupMessageRes = backupMessageRes,
        onSoundEnabledChange = viewModel::setSoundEnabled,
        onThemeModeChange = viewModel::setThemeMode,
        onAppLanguageChange = viewModel::setAppLanguage,
        onRedeemPromo = viewModel::redeemPromoCode,
        onClearPromoMessage = viewModel::clearPromoMessage,
        onExport = { exportLauncher.launch("quizapp_backup.json") },
        onImport = { importLauncher.launch(arrayOf("application/json")) },
        onReset = viewModel::resetProgress,
        onClearBackupMessage = viewModel::clearBackupMessage,
        modifier = modifier
    )
}

@Composable
private fun SettingsContent(
    soundEnabled: Boolean,
    themeMode: ThemeMode,
    appLanguage: AppLanguage,
    promoRedeemed: Boolean,
    promoMessageRes: Int?,
    backupMessageRes: Int?,
    onSoundEnabledChange: (Boolean) -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onAppLanguageChange: (AppLanguage) -> Unit,
    onRedeemPromo: (String) -> Unit,
    onClearPromoMessage: () -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onReset: () -> Unit,
    onClearBackupMessage: () -> Unit,
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

        PromoCodeSection(
            redeemed = promoRedeemed,
            messageRes = promoMessageRes,
            onRedeem = onRedeemPromo,
            onClearMessage = onClearPromoMessage,
            colors = colors,
            textColor = textColor
        )

        DataSection(
            messageRes = backupMessageRes,
            onExport = onExport,
            onImport = onImport,
            onReset = onReset,
            onClearMessage = onClearBackupMessage,
            colors = colors,
            textColor = textColor
        )

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun DataSection(
    messageRes: Int?,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onReset: () -> Unit,
    onClearMessage: () -> Unit,
    colors: com.rustam.quizapp.ui.components.AppThemeColors,
    textColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    var showImportConfirm by remember { mutableStateOf(false) }
    var showResetConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(messageRes) {
        if (messageRes != null) {
            kotlinx.coroutines.delay(4_000)
            onClearMessage()
        }
    }

    SettingsSection(
        title = stringResource(R.string.settings_data_title),
        subtitle = stringResource(R.string.settings_data_subtitle),
        colors = colors,
        modifier = modifier
    ) {
        Button(
            onClick = onExport,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = AppShapes.Button,
            colors = ButtonDefaults.buttonColors(contentColor = textColor)
        ) {
            Text(
                text = stringResource(R.string.settings_data_export),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
        OutlinedButton(
            onClick = { showImportConfirm = true },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = AppShapes.Button
        ) {
            Text(
                text = stringResource(R.string.settings_data_import),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
        }
        OutlinedButton(
            onClick = { showResetConfirm = true },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = AppShapes.Button
        ) {
            Text(
                text = stringResource(R.string.settings_data_reset),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.error
            )
        }
        messageRes?.let { resId ->
            Text(
                text = stringResource(resId),
                style = MaterialTheme.typography.bodyMedium,
                color = if (resId == R.string.settings_backup_export_success ||
                    resId == R.string.settings_backup_import_success ||
                    resId == R.string.settings_backup_reset_success
                ) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        }
    }

    if (showImportConfirm) {
        ConfirmDialog(
            title = stringResource(R.string.settings_data_import_confirm_title),
            message = stringResource(R.string.settings_data_import_confirm_message),
            confirmLabel = stringResource(R.string.settings_data_import),
            onConfirm = {
                showImportConfirm = false
                onImport()
            },
            onDismiss = { showImportConfirm = false }
        )
    }
    if (showResetConfirm) {
        ConfirmDialog(
            title = stringResource(R.string.settings_data_reset_confirm_title),
            message = stringResource(R.string.settings_data_reset_confirm_message),
            confirmLabel = stringResource(R.string.settings_data_reset),
            onConfirm = {
                showResetConfirm = false
                onReset()
            },
            onDismiss = { showResetConfirm = false }
        )
    }
}

@Composable
private fun ConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(confirmLabel) } },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}

@Composable
private fun PromoCodeSection(
    redeemed: Boolean,
    messageRes: Int?,
    onRedeem: (String) -> Unit,
    onClearMessage: () -> Unit,
    colors: com.rustam.quizapp.ui.components.AppThemeColors,
    textColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    var code by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(messageRes) {
        if (messageRes != null) {
            kotlinx.coroutines.delay(4_000)
            onClearMessage()
        }
    }

    SettingsSection(
        title = stringResource(R.string.settings_promo_title),
        subtitle = stringResource(R.string.settings_promo_subtitle),
        colors = colors,
        modifier = modifier
    ) {
        if (redeemed) {
            Text(
                text = stringResource(R.string.settings_promo_redeemed),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            OutlinedTextField(
                value = code,
                onValueChange = { code = it.uppercase().take(12) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.settings_promo_hint)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (code.isNotBlank()) onRedeem(code)
                    }
                )
            )
            Button(
                onClick = { onRedeem(code) },
                enabled = code.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = AppShapes.Button,
                colors = ButtonDefaults.buttonColors(contentColor = textColor)
            ) {
                Text(
                    text = stringResource(R.string.settings_promo_activate),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        messageRes?.let { resId ->
            Text(
                text = stringResource(resId),
                style = MaterialTheme.typography.bodyMedium,
                color = if (resId == R.string.settings_promo_success) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )
        }
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
            promoRedeemed = false,
            promoMessageRes = null,
            backupMessageRes = null,
            onSoundEnabledChange = {},
            onThemeModeChange = {},
            onAppLanguageChange = {},
            onRedeemPromo = {},
            onClearPromoMessage = {},
            onExport = {},
            onImport = {},
            onReset = {},
            onClearBackupMessage = {}
        )
    }
}