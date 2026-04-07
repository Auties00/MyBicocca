package it.attendance100.mybicocca.ui.screen.settings

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.biometric.BiometricManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoSizeSelectLarge
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.ui.component.settings.DialogOpenerSettingItem
import it.attendance100.mybicocca.ui.component.settings.SimpleCategorySettingItem
import it.attendance100.mybicocca.ui.component.settings.SimpleSwitchSettingItem
import it.attendance100.mybicocca.util.PreferencesManager
import it.attendance100.mybicocca.util.rememberHapticManager
import it.attendance100.mybicocca.util.rememberPreferencesManager

@Composable
private fun SettingsSubScreenScaffold(
    title: String,
    onNavigateBack: () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        content(PaddingValues())
    }
}

@Composable
fun AppearanceSettingsScreen(
    onNavigateBack: () -> Unit,
    onThemeChange: (String) -> Unit = {},
) {
    val preferencesManager = rememberPreferencesManager()
    var selectedThemeMode by remember { mutableStateOf(preferencesManager.themeMode) }
    var showThemeDialog by remember { mutableStateOf(false) }
    val primaryColor = MaterialTheme.colorScheme.primary
    val haptic = rememberHapticManager()

    SettingsSubScreenScaffold(
        title = stringResource(R.string.settings_appearance),
        onNavigateBack = onNavigateBack,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState()),
        ) {
            DialogOpenerSettingItem(
                title = stringResource(R.string.settings_appearance_theme),
                subtitle = when (selectedThemeMode) {
                    PreferencesManager.THEME_LIGHT -> stringResource(R.string.settings_appearance_theme_light)
                    PreferencesManager.THEME_DARK -> stringResource(R.string.settings_appearance_theme_dark)
                    else -> stringResource(R.string.settings_appearance_theme_system_default)
                },
                icon = when (selectedThemeMode) {
                    PreferencesManager.THEME_DARK -> Icons.Default.DarkMode
                    PreferencesManager.THEME_LIGHT -> Icons.Default.LightMode
                    else -> Icons.Default.Brightness4
                },
                onClick = { showThemeDialog = true; haptic.tap() },
            )
        }

        if (showThemeDialog) {
            val themeOptions = listOf(
                PreferencesManager.THEME_SYSTEM_DEFAULT,
                PreferencesManager.THEME_LIGHT,
                PreferencesManager.THEME_DARK,
            )
            AlertDialog(
                onDismissRequest = { showThemeDialog = false; haptic.tap() },
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                title = { Text(stringResource(R.string.settings_appearance_theme)) },
                text = {
                    Column {
                        themeOptions.forEach { themeMode ->
                            val themeName = when (themeMode) {
                                PreferencesManager.THEME_SYSTEM_DEFAULT -> stringResource(R.string.settings_appearance_theme_system_default)
                                PreferencesManager.THEME_LIGHT -> stringResource(R.string.settings_appearance_theme_light)
                                PreferencesManager.THEME_DARK -> stringResource(R.string.settings_appearance_theme_dark)
                                else -> themeMode
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedThemeMode = themeMode
                                        preferencesManager.themeMode = themeMode
                                        preferencesManager.applyTheme()
                                        haptic.tap()
                                        onThemeChange(themeMode)
                                    },
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RadioButton(
                                    selected = selectedThemeMode == themeMode,
                                    onClick = {
                                        selectedThemeMode = themeMode
                                        preferencesManager.themeMode = themeMode
                                        preferencesManager.applyTheme()
                                        haptic.tap()
                                        onThemeChange(themeMode)
                                    },
                                    colors = RadioButtonDefaults.colors(selectedColor = primaryColor),
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(themeName)
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { showThemeDialog = false; haptic.tap() }) {
                        Text(stringResource(R.string.ok))
                    }
                },
            )
        }
    }
}

@Composable
fun GeneralSettingsScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val preferencesManager = rememberPreferencesManager()
    val haptic = rememberHapticManager()
    val primaryColor = MaterialTheme.colorScheme.primary

    val supportedLocales = remember {
        val fallback = listOf("en", "it")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            try {
                val lm = context.getSystemService(LocaleManager::class.java)
                val localeList = lm.overrideLocaleConfig?.supportedLocales ?: lm.systemLocales
                (0 until localeList.size()).mapNotNull { localeList.get(it)?.language }.distinct()
            } catch (_: Exception) {
                fallback
            }
        } else fallback
    }.let { listOf(PreferencesManager.LOCALE_SYSTEM_DEFAULT) + it }

    val currentAppLocale = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                val lm = context.getSystemService(LocaleManager::class.java)
                val appLocales = lm.applicationLocales
                if (appLocales.isEmpty) PreferencesManager.LOCALE_SYSTEM_DEFAULT
                else appLocales.get(0)?.language ?: PreferencesManager.LOCALE_SYSTEM_DEFAULT
            } catch (_: Exception) {
                preferencesManager.locale
            }
        } else {
            val appLocales = AppCompatDelegate.getApplicationLocales()
            if (appLocales.isEmpty) PreferencesManager.LOCALE_SYSTEM_DEFAULT
            else appLocales.get(0)?.language ?: PreferencesManager.LOCALE_SYSTEM_DEFAULT
        }
    }

    var selectedLocale by remember { mutableStateOf(currentAppLocale) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    SettingsSubScreenScaffold(
        title = stringResource(R.string.settings_general),
        onNavigateBack = onNavigateBack,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState()),
        ) {
            DialogOpenerSettingItem(
                title = stringResource(R.string.settings_general_language),
                subtitle = when (selectedLocale) {
                    PreferencesManager.LOCALE_SYSTEM_DEFAULT -> stringResource(R.string.language_system_default)
                    "it" -> stringResource(R.string.language_italiano)
                    "en" -> stringResource(R.string.language_english)
                    else -> selectedLocale
                },
                icon = Icons.Default.Language,
                onClick = { showLanguageDialog = true; haptic.tap() },
            )
        }

        if (showLanguageDialog) {
            AlertDialog(
                onDismissRequest = { showLanguageDialog = false; haptic.tap() },
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                title = { Text(stringResource(R.string.settings_general_language)) },
                text = {
                    Column {
                        supportedLocales.forEach { languageCode ->
                            val name = when (languageCode) {
                                PreferencesManager.LOCALE_SYSTEM_DEFAULT -> stringResource(R.string.language_system_default)
                                "it" -> stringResource(R.string.language_italiano)
                                "en" -> stringResource(R.string.language_english)
                                else -> languageCode
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedLocale = languageCode
                                        preferencesManager.locale = languageCode
                                        haptic.tap()
                                        setAppLocale(context, languageCode)
                                    },
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RadioButton(
                                    selected = selectedLocale == languageCode,
                                    onClick = {
                                        selectedLocale = languageCode
                                        preferencesManager.locale = languageCode
                                        haptic.tap()
                                        setAppLocale(context, languageCode)
                                    },
                                    colors = RadioButtonDefaults.colors(selectedColor = primaryColor),
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(name)
                            }
                        }
                    }
                },
                confirmButton = {},
            )
        }
    }
}

private fun setAppLocale(context: Context, languageCode: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val lm = context.getSystemService(LocaleManager::class.java)
        lm.applicationLocales = if (languageCode == PreferencesManager.LOCALE_SYSTEM_DEFAULT) {
            LocaleList.getEmptyLocaleList()
        } else {
            LocaleList.forLanguageTags(languageCode)
        }
    } else {
        AppCompatDelegate.setApplicationLocales(
            if (languageCode == PreferencesManager.LOCALE_SYSTEM_DEFAULT) {
                LocaleListCompat.getEmptyLocaleList()
            } else {
                LocaleListCompat.forLanguageTags(languageCode)
            },
        )
    }
}

@Composable
fun BehaviourSettingsScreen(onNavigateBack: () -> Unit) {
    SettingsSubScreenScaffold(
        title = stringResource(R.string.settings_behaviour),
        onNavigateBack = onNavigateBack,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
        ) {
            // Placeholder
        }
    }
}

@Composable
fun SecuritySettingsScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val preferencesManager = rememberPreferencesManager()
    val haptic = rememberHapticManager()
    val biometricManager = BiometricManager.from(context)
    val canUseBiometric =
        biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS
    val primaryColor = MaterialTheme.colorScheme.primary

    var fingerprintLoginEnabled by remember { mutableStateOf(preferencesManager.fingerprintLogin) }
    var keepLoggedIn by remember { mutableStateOf(preferencesManager.keepLoggedIn) }
    var sessionDuration by remember { mutableLongStateOf(preferencesManager.sessionDuration) }
    var showDurationDialog by remember { mutableStateOf(false) }

    SettingsSubScreenScaffold(
        title = stringResource(R.string.settings_security),
        onNavigateBack = onNavigateBack,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState()),
        ) {
            if (canUseBiometric) {
                SimpleSwitchSettingItem(
                    title = stringResource(R.string.settings_security_fingerprint_login),
                    subtitle = stringResource(R.string.settings_security_fingerprint_login_desc),
                    icon = Icons.Default.Fingerprint,
                    checked = fingerprintLoginEnabled,
                    onCheckedChange = {
                        fingerprintLoginEnabled = it; preferencesManager.fingerprintLogin =
                        it; haptic.tap()
                    },
                )
            }
            SimpleSwitchSettingItem(
                title = stringResource(R.string.settings_security_keep_logged_in),
                subtitle = if (keepLoggedIn) stringResource(R.string.settings_security_keep_logged_in_desc_true)
                else stringResource(R.string.settings_security_keep_logged_in_desc_false),
                icon = Icons.Default.Lock,
                checked = keepLoggedIn,
                onCheckedChange = {
                    keepLoggedIn = it; preferencesManager.keepLoggedIn = it; haptic.tap()
                },
            )
            DialogOpenerSettingItem(
                title = stringResource(R.string.settings_security_session_duration),
                subtitle = getDurationString(sessionDuration),
                icon = Icons.Default.Timer,
                enabled = keepLoggedIn,
                onClick = { showDurationDialog = true; haptic.tap() },
            )
        }

        if (showDurationDialog) {
            val durations = listOf(
                PreferencesManager.DURATION_30_MIN, PreferencesManager.DURATION_1_HOUR,
                PreferencesManager.DURATION_2_HOURS, PreferencesManager.DURATION_6_HOURS,
                PreferencesManager.DURATION_12_HOURS, PreferencesManager.DURATION_24_HOURS,
                PreferencesManager.DURATION_7_DAYS, PreferencesManager.DURATION_FOREVER,
            )
            AlertDialog(
                onDismissRequest = { showDurationDialog = false },
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                title = { Text(stringResource(R.string.settings_security_select_duration)) },
                text = {
                    Column {
                        durations.forEach { duration ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        sessionDuration =
                                            duration; preferencesManager.sessionDuration = duration
                                        showDurationDialog = false; haptic.tap()
                                    },
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RadioButton(
                                    selected = sessionDuration == duration,
                                    onClick = {
                                        sessionDuration =
                                            duration; preferencesManager.sessionDuration = duration
                                        showDurationDialog = false; haptic.tap()
                                    },
                                    colors = RadioButtonDefaults.colors(selectedColor = primaryColor),
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(getDurationString(duration))
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { showDurationDialog = false; haptic.tap() }) {
                        Text(stringResource(R.string.cancel))
                    }
                },
            )
        }
    }
}

@Composable
private fun getDurationString(duration: Long): String = when (duration) {
    PreferencesManager.DURATION_30_MIN -> stringResource(R.string.duration_30_min)
    PreferencesManager.DURATION_1_HOUR -> stringResource(R.string.duration_1_hour)
    PreferencesManager.DURATION_2_HOURS -> stringResource(R.string.duration_2_hours)
    PreferencesManager.DURATION_6_HOURS -> stringResource(R.string.duration_6_hours)
    PreferencesManager.DURATION_12_HOURS -> stringResource(R.string.duration_12_hours)
    PreferencesManager.DURATION_24_HOURS -> stringResource(R.string.duration_24_hours)
    PreferencesManager.DURATION_7_DAYS -> stringResource(R.string.duration_7_days)
    PreferencesManager.DURATION_FOREVER -> stringResource(R.string.duration_forever)
    else -> "Unknown"
}

@Composable
fun DeveloperSettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToApiTest: () -> Unit = {},
) {
    val preferencesManager = rememberPreferencesManager()
    var badgeParallax by remember { mutableStateOf(preferencesManager.badgeParallax) }
    var whiteBadge by remember { mutableStateOf(preferencesManager.badgeWhite) }
    var progressBarToggle by remember { mutableStateOf(preferencesManager.progressBarToggle) }
    val haptic = rememberHapticManager()

    SettingsSubScreenScaffold(
        title = stringResource(R.string.settings_developer),
        onNavigateBack = onNavigateBack,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
                text = stringResource(R.string.settings_developer_badge),
                color = MaterialTheme.colorScheme.primary,
                fontSize = 14.sp,
            )
            SimpleSwitchSettingItem(
                title = stringResource(R.string.settings_developer_badge_parallax),
                subtitle = "badgeParallax",
                icon = Icons.Default.PhotoSizeSelectLarge,
                checked = badgeParallax,
                onCheckedChange = {
                    badgeParallax = it; preferencesManager.badgeParallax = it; haptic.tap()
                },
            )
            SimpleSwitchSettingItem(
                title = stringResource(R.string.settings_developer_badge_white),
                subtitle = "badgeWhite",
                icon = Icons.Filled.Badge,
                checked = whiteBadge,
                onCheckedChange = {
                    whiteBadge = it; preferencesManager.badgeWhite = it; haptic.tap()
                },
            )

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.onSecondary,
            )

            Text(
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
                text = stringResource(R.string.settings_developer_api),
                color = MaterialTheme.colorScheme.primary,
                fontSize = 14.sp,
            )
            SimpleCategorySettingItem(
                title = "API Test Page",
                subtitle = "Test API endpoints and view raw JSON",
                icon = Icons.Default.Code,
                onClick = { onNavigateToApiTest(); haptic.tap() },
            )

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.onSecondary,
            )

            Text(
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
                text = "Other",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 14.sp,
            )
            SimpleSwitchSettingItem(
                title = "ProgressBar Type",
                subtitle = "progressBarToggle",
                icon = Icons.Default.HorizontalRule,
                checked = progressBarToggle,
                onCheckedChange = {
                    progressBarToggle = it; preferencesManager.progressBarToggle = it; haptic.tap()
                },
            )
        }
    }
}
