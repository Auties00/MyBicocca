package it.attendance100.mybicocca.screens

import android.app.*
import android.content.*
import android.os.*
import androidx.appcompat.app.*
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.ui.theme.*
import it.attendance100.mybicocca.utils.*

val fallbackLangList = listOf("en", "it")

@Suppress("AssignedValueIsNeverRead")
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SettingsScreen(
  navController: NavHostController,
  sharedTransitionScope: SharedTransitionScope,
  animatedContentScope: AnimatedContentScope,
  onThemeChange: (Boolean) -> Unit,
) {
  val context = LocalContext.current
  val preferencesManager = rememberPreferencesManager()
  var selectedThemeMode by remember { mutableStateOf(preferencesManager.themeMode) }
  var showThemeDialog by remember { mutableStateOf(false) }

  // Get supported locales from LocaleManager
  val supportedLocales = remember {
    val locales = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
      try {
        val localeManager = context.getSystemService(LocaleManager::class.java)
        val overrideConfig = localeManager.overrideLocaleConfig
        val localeList = overrideConfig?.supportedLocales ?: localeManager.systemLocales

        // Extract language codes from LocaleList
        (0 until localeList.size()).mapNotNull { index ->
          localeList.get(index)?.language
        }.distinct()
      } catch (_: Exception) {
        fallbackLangList // Fallback
      }
    } else {
      fallbackLangList // Fallback for older versions
    }

    // Add system default option at the beginning
    listOf(PreferencesManager.LOCALE_SYSTEM_DEFAULT) + locales
  }

  // Get current app locale
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

  val primaryColor = MaterialTheme.colorScheme.primary
  val textColor = MaterialTheme.colorScheme.onBackground
  val grayColor = if (MaterialTheme.colorScheme.background == BackgroundColor) GrayColor else GrayColorLight

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
          IconButton(onClick = { navController.navigateUp() }) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = stringResource(R.string.arrow_back),
              tint = textColor
            )
          }
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = stringResource(R.string.settings),
            color = textColor,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
          .fillMaxSize()
          .padding(paddingValues)
          .background(MaterialTheme.colorScheme.background)
    ) {
      // Appearance Section
      Text(
        text = stringResource(R.string.settings_appearance),
        color = primaryColor,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
      )

      // Theme Selection
      Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showThemeDialog = true },
        color = MaterialTheme.colorScheme.background
      ) {
        Row(
          modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
          ) {
            with(sharedTransitionScope) {
              Icon(
                imageVector = when (selectedThemeMode) {
                  PreferencesManager.THEME_DARK -> Icons.Default.DarkMode
                  PreferencesManager.THEME_LIGHT -> Icons.Default.LightMode
                  else -> Icons.Default.Brightness4
                },
                contentDescription = null,
                tint = primaryColor,
                modifier = Modifier
                    .size(24.dp)
                    .sharedElement(
                      sharedContentState = rememberSharedContentState(key = "settings_icon"),
                      animatedVisibilityScope = animatedContentScope,
                      boundsTransform = { _, _ ->
                        tween(durationMillis = 400)
                      }
                    )
              )
            }
            Column {
              Text(
                text = stringResource(R.string.settings_theme),
                color = textColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
              )
              Text(
                text = when (selectedThemeMode) {
                  PreferencesManager.THEME_LIGHT -> stringResource(R.string.theme_light)
                  PreferencesManager.THEME_DARK -> stringResource(R.string.theme_dark)
                  else -> stringResource(R.string.theme_system_default)
                },
                color = grayColor,
                fontSize = 13.sp
              )
            }
          }
          Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = grayColor
          )
        }
      }

      HorizontalDivider(color = grayColor.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 16.dp))

      // General Section
      Text(
        text = stringResource(R.string.settings_general),
        color = primaryColor,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
      )

      // Language Selection
      Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showLanguageDialog = true },
        color = MaterialTheme.colorScheme.background
      ) {
        Row(
          modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Language,
              contentDescription = null,
              tint = primaryColor,
              modifier = Modifier.size(24.dp)
            )
            Column {
              Text(
                text = stringResource(R.string.settings_language),
                color = textColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
              )
              Text(
                text = when (selectedLocale) {
                  PreferencesManager.LOCALE_SYSTEM_DEFAULT -> stringResource(R.string.language_system_default)
                  "it" -> stringResource(R.string.language_italian)
                  "en" -> stringResource(R.string.language_english)
                  else -> selectedLocale
                },
                color = grayColor,
                fontSize = 13.sp
              )
            }
          }
          Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = grayColor
          )
        }
      }
    }

    // Language Selection Dialog
    if (showLanguageDialog) {
      AlertDialog(
        onDismissRequest = { showLanguageDialog = false },
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        title = {
          Text(text = stringResource(R.string.settings_language))
        },
        text = {
          Column {
            supportedLocales.forEach { languageCode ->
              val languageName = when (languageCode) {
                PreferencesManager.LOCALE_SYSTEM_DEFAULT -> stringResource(R.string.language_system_default)
                "it" -> stringResource(R.string.language_italian)
                "en" -> stringResource(R.string.language_english)
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
                  colors = RadioButtonDefaults.colors(
                    selectedColor = primaryColor
                  )
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

    // Theme Selection Dialog
    if (showThemeDialog) {
      val themeOptions = listOf(
        PreferencesManager.THEME_SYSTEM_DEFAULT,
        PreferencesManager.THEME_LIGHT,
        PreferencesManager.THEME_DARK
      )

      AlertDialog(
        onDismissRequest = { showThemeDialog = false },
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        title = {
          Text(text = stringResource(R.string.settings_theme))
        },
        text = {
          Column {
            themeOptions.forEach { themeMode ->
              val themeName = when (themeMode) {
                PreferencesManager.THEME_SYSTEM_DEFAULT -> stringResource(R.string.theme_system_default)
                PreferencesManager.THEME_LIGHT -> stringResource(R.string.theme_light)
                PreferencesManager.THEME_DARK -> stringResource(R.string.theme_dark)
                else -> themeMode
              }

              Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                      selectedThemeMode = themeMode
                      preferencesManager.themeMode = themeMode
                      preferencesManager.applyTheme()

                      onThemeChange(preferencesManager.isDarkMode)
                    },
                verticalAlignment = Alignment.CenterVertically
              ) {
                RadioButton(
                  selected = selectedThemeMode == themeMode,
                  onClick = {
                    selectedThemeMode = themeMode
                    preferencesManager.themeMode = themeMode
                    preferencesManager.applyTheme()
                    // Note: Icon follows system theme automatically
                    onThemeChange(preferencesManager.isDarkMode)
                  },
                  colors = RadioButtonDefaults.colors(
                    selectedColor = primaryColor
                  )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = themeName)
              }
            }
          }
        },
        confirmButton = {
          Button(onClick = { showThemeDialog = false }) {
            Text(text = stringResource(R.string.ok))
          }
        },
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

