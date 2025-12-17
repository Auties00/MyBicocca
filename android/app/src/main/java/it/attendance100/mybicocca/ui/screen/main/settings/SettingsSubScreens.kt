package it.attendance100.mybicocca.ui.screen.main.settings

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.biometric.BiometricManager
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PhotoSizeSelectLarge
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.ui.component.DialogOpenerSettingItem
import it.attendance100.mybicocca.ui.component.SimpleSwitchSettingItem
import it.attendance100.mybicocca.ui.screen.main.MainViewModel
import it.attendance100.mybicocca.manager.StorageManager
import it.attendance100.mybicocca.manager.rememberHapticManager
import it.attendance100.mybicocca.manager.rememberPreferencesManager

@Composable
fun SettingsSubScreenScaffold(
    title: String,
    navController: NavHostController,
    mainViewModel: MainViewModel = hiltViewModel(),
    content: @Composable (PaddingValues) -> Unit,
) {
    val textColor = MaterialTheme.colorScheme.onBackground
    val haptic = rememberHapticManager()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(60.dp),
                color = MaterialTheme.colorScheme.background
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 13.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            haptic.tap()
                            navController.navigateUp()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.arrow_back),
                            tint = textColor
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        color = textColor,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { paddingValues ->
        content(paddingValues)
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
@Suppress("unused")
fun AppearanceSettingsScreen(
    navController: NavHostController,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onThemeChange: (Boolean) -> Unit,
) {
    val preferencesManager = rememberPreferencesManager()
    var selectedThemeMode by remember { mutableStateOf(preferencesManager.themeMode) }
    var showThemeDialog by remember { mutableStateOf(false) }
    val primaryColor = MaterialTheme.colorScheme.primary

    SettingsSubScreenScaffold(
        title = stringResource(R.string.settings_appearance),
        navController = navController
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
        ) {
            DialogOpenerSettingItem(
                title = stringResource(R.string.settings_appearance_theme),
                subtitle = when (selectedThemeMode) {
                    StorageManager.THEME_LIGHT -> stringResource(R.string.settings_appearance_theme_light)
                    StorageManager.THEME_DARK -> stringResource(R.string.settings_appearance_theme_dark)
                    else -> stringResource(R.string.settings_appearance_theme_system_default)
                },
                icon = when (selectedThemeMode) {
                    StorageManager.THEME_DARK -> Icons.Default.DarkMode
                    StorageManager.THEME_LIGHT -> Icons.Default.LightMode
                    else -> Icons.Default.Brightness4
                },
                onClick = { showThemeDialog = true }
            )
        }

        if (showThemeDialog) {
            val themeOptions = listOf(
                StorageManager.THEME_SYSTEM_DEFAULT,
                StorageManager.THEME_LIGHT,
                StorageManager.THEME_DARK
            )

            AlertDialog(
                onDismissRequest = {
                    @Suppress("AssignedValueIsNeverRead")
                    showThemeDialog = false
                },
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                title = { Text(text = stringResource(R.string.settings_appearance_theme)) },
                text = {
                    Column {
                        themeOptions.forEach { themeMode ->
                            val themeName = when (themeMode) {
                                StorageManager.THEME_SYSTEM_DEFAULT -> stringResource(R.string.settings_appearance_theme_system_default)
                                StorageManager.THEME_LIGHT -> stringResource(R.string.settings_appearance_theme_light)
                                StorageManager.THEME_DARK -> stringResource(R.string.settings_appearance_theme_dark)
                                else -> themeMode
                            }
                            val default = isSystemInDarkTheme()

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedThemeMode = themeMode
                                        preferencesManager.themeMode = themeMode
                                        preferencesManager.applyTheme()
                                        onThemeChange(preferencesManager.isDarkMode ?: default)

                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedThemeMode == themeMode,
                                    onClick = {
                                        selectedThemeMode = themeMode
                                        preferencesManager.themeMode = themeMode
                                        preferencesManager.applyTheme()
                                        onThemeChange(preferencesManager.isDarkMode ?: default)
                                    },
                                    colors = RadioButtonDefaults.colors(selectedColor = primaryColor)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = themeName)
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            @Suppress("AssignedValueIsNeverRead")
                            showThemeDialog = false
                        }
                    ) {
                        Text(text = stringResource(R.string.ok))
                    }
                },
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
@Suppress("unused")
fun GeneralSettingsScreen(
    navController: NavHostController,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
) {
    val context = LocalContext.current
    val preferencesManager = rememberPreferencesManager()
    val primaryColor = MaterialTheme.colorScheme.primary

    // Get supported locales logic (simplified for brevity, same as before)
    val supportedLocales = remember {
        val fallbackLangList = listOf("en", "it")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            try {
                val localeManager = context.getSystemService(LocaleManager::class.java)
                val overrideConfig = localeManager.overrideLocaleConfig
                val localeList = overrideConfig?.supportedLocales ?: localeManager.systemLocales
                (0 until localeList.size()).mapNotNull { index -> localeList.get(index)?.language }
                    .distinct()
            } catch (_: Exception) {
                fallbackLangList
            }
        } else {
            fallbackLangList
        }
            .let { listOf(StorageManager.LOCALE_SYSTEM_DEFAULT) + it }
    }

    val currentAppLocale = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                val localeManager = context.getSystemService(LocaleManager::class.java)
                val appLocales = localeManager.applicationLocales
                if (appLocales.isEmpty) StorageManager.LOCALE_SYSTEM_DEFAULT
                else appLocales.get(0)?.language ?: StorageManager.LOCALE_SYSTEM_DEFAULT
            } catch (_: Exception) {
                preferencesManager.locale
            }
        } else {
            val appLocales = AppCompatDelegate.getApplicationLocales()
            if (appLocales.isEmpty) StorageManager.LOCALE_SYSTEM_DEFAULT
            else appLocales.get(0)?.language ?: StorageManager.LOCALE_SYSTEM_DEFAULT
        }
    }

    var selectedLocale by remember { mutableStateOf(currentAppLocale) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    SettingsSubScreenScaffold(
        title = stringResource(R.string.settings_general),
        navController = navController
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
        ) {
            DialogOpenerSettingItem(
                title = stringResource(R.string.settings_general_language),
                subtitle = when (selectedLocale) {
                    StorageManager.LOCALE_SYSTEM_DEFAULT -> stringResource(R.string.language_system_default)
                    "it" -> stringResource(R.string.settings_general_language_italian)
                    "en" -> stringResource(R.string.settings_general_language_english)
                    else -> selectedLocale
                },
                icon = Icons.Default.Language,
                onClick = { showLanguageDialog = true }
            )
        }

        if (showLanguageDialog) {
            AlertDialog(
                onDismissRequest = {
                    @Suppress("AssignedValueIsNeverRead")
                    showLanguageDialog = false
                },
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                title = { Text(text = stringResource(R.string.settings_general_language)) },
                text = {
                    Column {
                        supportedLocales.forEach { languageCode ->
                            val languageName = when (languageCode) {
                                StorageManager.LOCALE_SYSTEM_DEFAULT -> stringResource(R.string.language_system_default)
                                "it" -> stringResource(R.string.settings_general_language_italian)
                                "en" -> stringResource(R.string.settings_general_language_english)
                                else -> languageCode
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedLocale = languageCode
                                        preferencesManager.locale = languageCode
                                        setAppLocale(context, languageCode)
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedLocale == languageCode,
                                    onClick = {
                                        selectedLocale = languageCode
                                        preferencesManager.locale = languageCode
                                        setAppLocale(context, languageCode)
                                    },
                                    colors = RadioButtonDefaults.colors(selectedColor = primaryColor)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = languageName)
                            }
                        }
                    }
                },
                confirmButton = { },
            )
        }
    }
}

private fun setAppLocale(context: Context, languageCode: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val localeManager = context.getSystemService(LocaleManager::class.java)
        localeManager.applicationLocales =
            if (languageCode == StorageManager.LOCALE_SYSTEM_DEFAULT) {
                LocaleList.getEmptyLocaleList()
            } else {
                LocaleList.forLanguageTags(languageCode)
            }
    } else {
        AppCompatDelegate.setApplicationLocales(
            if (languageCode == StorageManager.LOCALE_SYSTEM_DEFAULT) {
                LocaleListCompat.getEmptyLocaleList()
            } else {
                LocaleListCompat.forLanguageTags(languageCode)
            }
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
@Suppress("unused")
fun BehaviourSettingsScreen(
    navController: NavHostController,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
) {
    SettingsSubScreenScaffold(
        title = stringResource(R.string.settings_behaviour),
        navController = navController
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Placeholder
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
@Suppress("unused")
fun SecuritySettingsScreen(
    navController: NavHostController,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
) {
    val context = LocalContext.current
    val preferencesManager = rememberPreferencesManager()
    val biometricManager = BiometricManager.from(context)
    val canUseBiometric =
        biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS

    var fingerprintLoginEnabled by remember { mutableStateOf(preferencesManager.fingerprintLogin) }

    SettingsSubScreenScaffold(
        title = stringResource(R.string.settings_security),
        navController = navController
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
        ) {
            if (canUseBiometric) {
                SimpleSwitchSettingItem(
                    title = stringResource(R.string.settings_security_fingerprint_login),
                    subtitle = stringResource(R.string.settings_security_fingerprint_login_desc),
                    icon = Icons.Default.Fingerprint,
                    checked = fingerprintLoginEnabled,
                    onCheckedChange = {
                        fingerprintLoginEnabled = it
                        preferencesManager.fingerprintLogin = it
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
@Suppress("unused")
fun DeveloperSettingsScreen(
    navController: NavHostController,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
) {
    val preferencesManager = rememberPreferencesManager()
    var badgeParallax by remember { mutableStateOf(preferencesManager.badgeParallax) }
    var whiteBadge by remember { mutableStateOf(preferencesManager.badgeWhite) }
    var progressBarToggle by remember { mutableStateOf(preferencesManager.progressBarToggle) }

    SettingsSubScreenScaffold(
        title = stringResource(R.string.settings_developer),
        navController = navController
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
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
                    badgeParallax = it
                    preferencesManager.badgeParallax = it
                }
            )

            SimpleSwitchSettingItem(
                title = stringResource(R.string.settings_developer_badge_white),
                subtitle = "badgeWhite",
                icon = Icons.Filled.Badge,
                checked = whiteBadge,
                onCheckedChange = {
                    whiteBadge = it
                    preferencesManager.badgeWhite = it
                }
            )

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.onSecondary
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
                    progressBarToggle = it
                    preferencesManager.progressBarToggle = it
                }
            )

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.onSecondary
            )
        }
    }
}
