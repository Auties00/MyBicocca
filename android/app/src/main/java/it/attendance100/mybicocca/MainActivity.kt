package it.attendance100.mybicocca

import android.os.*
import androidx.activity.*
import androidx.activity.compose.*
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.navigation.compose.*
import it.attendance100.mybicocca.screens.*
import it.attendance100.mybicocca.ui.theme.*
import it.attendance100.mybicocca.utils.*
import kotlinx.coroutines.*

// Navigation routes
sealed class Screen(val route: String) {
  object Home : Screen("home")
  object LoginManager : Screen("login_manager")
  object Settings : Screen("settings")
  object AppInfo : Screen("app_info")
}


class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    val preferencesManager = PreferencesManager(this)
    preferencesManager.applyTheme() // Ensures theme is applied immediately

    super.onCreate(savedInstanceState)

    // WindowCompat.setDecorFitsSystemWindows(window, false)

    setContent {
      val preferencesManager = rememberPreferencesManager() // Reinstantiation with context
      val systemInDarkTheme = isSystemInDarkTheme()

      var currentThemeMode by remember { mutableStateOf(preferencesManager.themeMode) }

      // Determine theme based on mode
      val isDarkMode by remember(currentThemeMode, systemInDarkTheme) {
        derivedStateOf {
          when (currentThemeMode) {
            PreferencesManager.THEME_DARK -> true
            PreferencesManager.THEME_LIGHT -> false
            else -> systemInDarkTheme
          }
        }
      }

      // Update edge-to-edge when theme changes
      LaunchedEffect(isDarkMode) {
        enableEdgeToEdge(
          statusBarStyle =
              if (isDarkMode)
                SystemBarStyle.dark(
                  Color.Transparent.toArgb(),
                ) else SystemBarStyle.light(
                Color.Transparent.toArgb(),
                darkScrim = Color.Transparent.toArgb(),
              ),
          navigationBarStyle = SystemBarStyle.auto(
            lightScrim = OnPrimaryColor.toArgb(),
            darkScrim = OnPrimaryColor.toArgb(),
          )
        )
      }

      MyBicoccaTheme(darkTheme = isDarkMode) {
        Surface(
          modifier = Modifier
              .fillMaxSize()
              .statusBarsPadding(), // manual top padding because of enableEdgeToEdge()
        ) {
          AppNavigation(
            onThemeChange = { _ ->
              // Update the state to trigger recomposition
              currentThemeMode = preferencesManager.themeMode
            }
          )
        }
      }
    }
  }
}

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(onThemeChange: (Boolean) -> Unit) {
  val navController = rememberNavController()
  val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
  val scope = rememberCoroutineScope()

  SharedTransitionLayout {
    NavHost(
      navController = navController,
      startDestination = Screen.Home.route,
      popExitTransition = {
        scaleOut(
          targetScale = 0.9f,
          transformOrigin = TransformOrigin(pivotFractionX = 0.5f, pivotFractionY = 0.5f),
          animationSpec = tween(300, easing = CubicBezierEasing(0f, 1f, 0.57f, 0.93f))
        ) + fadeOut(
          targetAlpha = 0.1f,
          animationSpec = tween(300, easing = CubicBezierEasing(0f, 1f, 0.57f, 0.93f))
        ) + slideOutHorizontally(
          targetOffsetX = { it / 4 },
          animationSpec = tween(300, easing = CubicBezierEasing(0f, 1f, 0.57f, 0.93f))
        )
      },
      popEnterTransition = {
        slideInHorizontally(
          initialOffsetX = { -it / 2 },
          animationSpec = tween(300, easing = CubicBezierEasing(0f, 1f, 0.57f, 0.93f))
        )
      },
    ) {
      composable(Screen.Home.route) { _ ->
        BackHandler(enabled = drawerState.isOpen) {
          if (drawerState.isOpen) {
            // If the drawer is open, launch a coroutine to close it
            scope.launch {
              drawerState.close()
            }
          }
        }

        HomePage(navController, this@SharedTransitionLayout, this, drawerState)
      }
      composable(Screen.LoginManager.route) { _ ->
        LoginManagerScreen(navController, this@SharedTransitionLayout, this)
      }
      composable(Screen.Settings.route) { _ ->
        SettingsScreen(navController, this@SharedTransitionLayout, this, onThemeChange)
      }
      composable(Screen.AppInfo.route) { _ ->
        AppInfoScreen(navController, this@SharedTransitionLayout, this)
      }
    }
  }
}

