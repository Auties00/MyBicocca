package it.attendance100.mybicocca.screens.settings

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.res.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import androidx.hilt.lifecycle.viewmodel.compose.*
import androidx.navigation.*
import it.attendance100.mybicocca.*
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.components.*
import it.attendance100.mybicocca.utils.*
import it.attendance100.mybicocca.viewmodel.*

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
@Suppress("unused")
fun SettingsScreen(
  navController: NavHostController,
  sharedTransitionScope: SharedTransitionScope,
  animatedContentScope: AnimatedContentScope,
  onThemeChange: (Boolean) -> Unit,
  mainViewModel: MainViewModel = hiltViewModel(),
) {
  val haptic = rememberHapticManager()
  val textColor = MaterialTheme.colorScheme.onBackground

  Box(
    modifier = Modifier.statusBarsPadding()
  ) {
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
            .verticalScroll(rememberScrollState())
      ) {
        SimpleCategorySettingItem(
          title = stringResource(R.string.settings_appearance),
          subtitle = stringResource(R.string.settings_appearance_subtitle),
          icon = Icons.Default.Palette,
          onClick = { navController.navigate(Screen.SettingsAppearance.route) },
        )

        SimpleCategorySettingItem(
          title = stringResource(R.string.settings_general),
          subtitle = stringResource(R.string.settings_general_subtitle),
          icon = Icons.Default.Settings,
          onClick = { navController.navigate(Screen.SettingsGeneral.route) },
        )

        SimpleCategorySettingItem(
          title = stringResource(R.string.settings_behaviour),
          subtitle = stringResource(R.string.settings_behaviour_subtitle),
          icon = Icons.Default.Psychology,
          onClick = { navController.navigate(Screen.SettingsBehaviour.route) },
        )

        SimpleCategorySettingItem(
          title = stringResource(R.string.settings_security),
          subtitle = stringResource(R.string.settings_security_subtitle),
          icon = Icons.Default.Fingerprint,
          onClick = { navController.navigate(Screen.SettingsSecurity.route) },
        )

        SimpleCategorySettingItem(
          title = stringResource(R.string.settings_developer),
          subtitle = stringResource(R.string.settings_developer_subtitle),
          icon = Icons.Default.BugReport,
          onClick = { navController.navigate(Screen.SettingsDeveloper.route) },
        )

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))
        Spacer(modifier = Modifier.height(16.dp))

        SimpleCategorySettingItem(
          title = stringResource(R.string.login_manager),
          subtitle = stringResource(R.string.login_manager),
          icon = Icons.Default.Key,
          onClick = { navController.navigate(Screen.LoginManager.route) },
        )

        SimpleCategorySettingItem(
          title = stringResource(R.string.app_info),
          subtitle = stringResource(R.string.app_info),
          icon = Icons.Default.Info,
          onClick = { navController.navigate(Screen.AppInfo.route) },
        )
      }
    }
  }
}
