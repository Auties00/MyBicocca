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

    // Enable edge-to-edge content
    // enableEdgeToEdge(
    //   // Set the status bar to be transparent
    //   statusBarStyle =
    //       if (preferencesManager.isDarkMode)
    //         SystemBarStyle.dark(
    //           Color.Transparent.toArgb(),
    //         ) else SystemBarStyle.light(
    //         Color.Transparent.toArgb(),
    //         darkScrim = Color.Transparent.toArgb(),
    //       ),
    //   // Set the navigation bar to a solid black color
    //   navigationBarStyle = SystemBarStyle.auto(
    //     lightScrim = PrimaryColor.toArgb(),
    //     darkScrim = PrimaryColor.toArgb(),
    //   )
    // )

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

      MyBicoccaTheme(darkTheme = isDarkMode) {
        Surface(
          modifier = Modifier
              .fillMaxSize()
              .statusBarsPadding(), // manual top padding because of enableEdgeToEdge()
          color = MaterialTheme.colorScheme.background,
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
  val predictiveBackEasingFactor = 10.0f
  val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
  val scope = rememberCoroutineScope()

  SharedTransitionLayout {
    NavHost(
      navController = navController,
      startDestination = Screen.Home.route,
      popExitTransition = {
        scaleOut(
          targetScale = 0.9f,
          transformOrigin = TransformOrigin(pivotFractionX = 1.5f, pivotFractionY = 0.5f),
          animationSpec = tween(300, easing = /*CubicBezierEasing(0f,0f,0f,1f))*/ { fraction -> hybridEaseLog(fraction, 2.5f, 2.5f, 0.01f) * predictiveBackEasingFactor })
        ) + fadeOut(animationSpec = tween(300, easing = { fraction -> fraction * 2 })) + slideOutHorizontally(
          targetOffsetX = { hybridEaseLog(it / 3f, 30f, 30f, 20f).toInt() },
          animationSpec = tween(300)
        )
      },
      popEnterTransition = {
        slideInHorizontally(
          initialOffsetX = { -it / 3 },
          animationSpec = tween(300)
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

