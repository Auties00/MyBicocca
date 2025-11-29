package it.attendance100.mybicocca.screens

import android.app.*
import android.content.*
import android.os.*
import androidx.appcompat.app.*
import androidx.biometric.*
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import androidx.core.os.*
import androidx.navigation.*
import it.attendance100.mybicocca.*
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.components.*
import it.attendance100.mybicocca.utils.*

@Composable
fun SettingsSubScreenScaffold(
  title: String,
  navController: NavHostController,
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
          PreferencesManager.THEME_LIGHT -> stringResource(R.string.settings_appearance_theme_light)
          PreferencesManager.THEME_DARK -> stringResource(R.string.settings_appearance_theme_dark)
          else -> stringResource(R.string.settings_appearance_theme_system_default)
        },
        icon = when (selectedThemeMode) {
          PreferencesManager.THEME_DARK -> Icons.Default.DarkMode
          PreferencesManager.THEME_LIGHT -> Icons.Default.LightMode
          else -> Icons.Default.Brightness4
        },
        onClick = { showThemeDialog = true }
      )
    }

    if (showThemeDialog) {
      val themeOptions = listOf(
        PreferencesManager.THEME_SYSTEM_DEFAULT,
        PreferencesManager.THEME_LIGHT,
        PreferencesManager.THEME_DARK
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
                PreferencesManager.THEME_SYSTEM_DEFAULT -> stringResource(R.string.settings_appearance_theme_system_default)
                PreferencesManager.THEME_LIGHT -> stringResource(R.string.settings_appearance_theme_light)
                PreferencesManager.THEME_DARK -> stringResource(R.string.settings_appearance_theme_dark)
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
        (0 until localeList.size()).mapNotNull { index -> localeList.get(index)?.language }.distinct()
      } catch (_: Exception) {
        fallbackLangList
      }
    } else {
      fallbackLangList
    }
        .let { listOf(PreferencesManager.LOCALE_SYSTEM_DEFAULT) + it }
  }

  val currentAppLocale = remember {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      try {
        val localeManager = context.getSystemService(LocaleManager::class.java)
        val appLocales = localeManager.applicationLocales
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
          PreferencesManager.LOCALE_SYSTEM_DEFAULT -> stringResource(R.string.language_system_default)
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
                PreferencesManager.LOCALE_SYSTEM_DEFAULT -> stringResource(R.string.language_system_default)
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
    localeManager.applicationLocales = if (languageCode == PreferencesManager.LOCALE_SYSTEM_DEFAULT) {
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
@Suppress("unused", "AssignedValueIsNeverRead")
fun SecuritySettingsScreen(
  navController: NavHostController,
  sharedTransitionScope: SharedTransitionScope,
  animatedContentScope: AnimatedContentScope,
) {
  val context = LocalContext.current
  val preferencesManager = rememberPreferencesManager()
  val biometricManager = BiometricManager.from(context)
  val canUseBiometric = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS

  var fingerprintLoginEnabled by remember { mutableStateOf(preferencesManager.fingerprintLogin) }
  var keepLoggedIn by remember { mutableStateOf(preferencesManager.keepLoggedIn) }
  var sessionDuration by remember { mutableLongStateOf(preferencesManager.sessionDuration) }
  var showDurationDialog by remember { mutableStateOf(false) }
  val primaryColor = MaterialTheme.colorScheme.primary

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

      SimpleSwitchSettingItem(
        title = stringResource(R.string.settings_security_keep_logged_in),
        subtitle = if (keepLoggedIn) stringResource(R.string.settings_security_keep_logged_in_desc_true) else stringResource(R.string.settings_security_keep_logged_in_desc_false),
        icon = Icons.Default.Lock,
        checked = keepLoggedIn,
        onCheckedChange = {
          keepLoggedIn = it
          preferencesManager.keepLoggedIn = it
        }
      )

      DialogOpenerSettingItem(
        title = stringResource(R.string.settings_security_session_duration),
        subtitle = getDurationString(sessionDuration),
        icon = Icons.Default.Timer,
        enabled = keepLoggedIn,
        onClick = { showDurationDialog = true }
      )
    }

    if (showDurationDialog) {
      val durations = listOf(
        PreferencesManager.DURATION_30_MIN,
        PreferencesManager.DURATION_1_HOUR,
        PreferencesManager.DURATION_2_HOURS,
        PreferencesManager.DURATION_6_HOURS,
        PreferencesManager.DURATION_12_HOURS,
        PreferencesManager.DURATION_24_HOURS,
        PreferencesManager.DURATION_7_DAYS,
        PreferencesManager.DURATION_FOREVER
      )

      AlertDialog(
        onDismissRequest = { showDurationDialog = false },
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        title = { Text(text = stringResource(R.string.settings_security_select_duration)) },
        text = {
          Column {
            durations.forEach { duration ->
              Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                      sessionDuration = duration
                      preferencesManager.sessionDuration = duration
                      showDurationDialog = false
                    },
                verticalAlignment = Alignment.CenterVertically
              ) {
                RadioButton(
                  selected = sessionDuration == duration,
                  onClick = {
                    sessionDuration = duration
                    preferencesManager.sessionDuration = duration
                    showDurationDialog = false
                  },
                  colors = RadioButtonDefaults.colors(selectedColor = primaryColor)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = getDurationString(duration))
              }
            }
          }
        },
        confirmButton = {
          Button(onClick = { showDurationDialog = false }) {
            Text(text = stringResource(R.string.cancel))
          }
        },
      )
    }
  }
}

@Composable
private fun getDurationString(duration: Long): String {
  return when (duration) {
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
        text = "Badge",
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

      HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.onSecondary)

      Text(
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
        text = "API",
        color = MaterialTheme.colorScheme.onBackground,
        fontSize = 14.sp,
      )

      SimpleCategorySettingItem(
        title = "API Test Page",
        subtitle = "Test API endpoints and view raw JSON",
        icon = Icons.Default.Code,
        onClick = { navController.navigate(Screen.ApiTest.route) }
      )
    }
  }
}
