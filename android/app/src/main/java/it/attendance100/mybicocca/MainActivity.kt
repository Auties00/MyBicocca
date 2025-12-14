package it.attendance100.mybicocca

import android.app.*
import android.content.*
import android.content.pm.*
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
import androidx.compose.ui.platform.*
import androidx.navigation.compose.*
import dagger.hilt.android.*
import it.attendance100.mybicocca.screens.*
import it.attendance100.mybicocca.screens.login.*
import it.attendance100.mybicocca.ui.theme.*
import it.attendance100.mybicocca.utils.*
import kotlinx.coroutines.*
import javax.inject.*

// Navigation routes
sealed class Screen(val route: String) {
  object Splash : Screen("splash")
  object Home : Screen("home")
  object LoginManager : Screen("login_manager")
  object Settings : Screen("settings")
  object SettingsAppearance : Screen("settings_appearance")
  object SettingsGeneral : Screen("settings_general")
  object SettingsBehaviour : Screen("settings_behaviour")
  object SettingsSecurity : Screen("settings_security")
  object SettingsDeveloper : Screen("settings_developer")
  object AppInfo : Screen("app_info")
  object Login : Screen("login_webview")
  object ApiTest : Screen("api_test")

}


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
  @Inject
  lateinit var networkMonitor: NetworkMonitor

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

      LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

      // Update edge-to-edge when theme changes
      LaunchedEffect(isDarkMode) { styleStatusBar(isDarkMode) }

      MyBicoccaTheme(darkTheme = isDarkMode) {
        ProvideHapticManager {
          Surface(
            modifier = Modifier.fillMaxSize(),
          ) {
            Box(modifier = Modifier.statusBarsPadding()) { // manual top padding because of enableEdgeToEdge()
              AppNavigation(
                onThemeChange = { isDarkModeInner ->
                  // Update the state to trigger recomposition
                  currentThemeMode = preferencesManager.themeMode
                  styleStatusBar(isDarkModeInner)
                }
              )
            }
          }
        }
      }
    }
  }

  override fun onResume() {
    super.onResume()
    networkMonitor.refresh()
  }

  fun styleStatusBar(isDarkMode: Boolean) {
    enableEdgeToEdge(
      statusBarStyle =
          if (isDarkMode)
            SystemBarStyle.dark(
              BackgroundColor.toArgb()
            )
          else
            SystemBarStyle.light(
              BackgroundColorLight.toArgb(),
              darkScrim = BackgroundColor.toArgb(),
            ),
      navigationBarStyle = SystemBarStyle.auto(
        lightScrim = OnPrimaryColor.toArgb(),
        darkScrim = OnPrimaryColor.toArgb(),
      )
    )
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
      startDestination = Screen.Splash.route,
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
      composable(Screen.Splash.route) {
        SplashScreen(navController)
      }
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
      composable(Screen.SettingsAppearance.route) { _ ->
        AppearanceSettingsScreen(navController, this@SharedTransitionLayout, this, onThemeChange)
      }
      composable(Screen.SettingsGeneral.route) { _ ->
        GeneralSettingsScreen(navController, this@SharedTransitionLayout, this)
      }
      composable(Screen.SettingsBehaviour.route) { _ ->
        BehaviourSettingsScreen(navController, this@SharedTransitionLayout, this)
      }
      composable(Screen.SettingsSecurity.route) { _ ->
        SecuritySettingsScreen(navController, this@SharedTransitionLayout, this)
      }
      composable(Screen.SettingsDeveloper.route) { _ ->
        DeveloperSettingsScreen(navController, this@SharedTransitionLayout, this)
      }
      composable(Screen.AppInfo.route) { _ ->
        AppInfoScreen(navController, this@SharedTransitionLayout, this)
      }
      composable(Screen.Login.route) {
        LoginWebViewScreen(
          onLoginSuccess = {
            navController.navigate(Screen.Home.route) {
              popUpTo(Screen.Login.route) { inclusive = true }
            }
          },
          onBack = { navController.navigateUp() }
        )
      }
      composable(Screen.ApiTest.route) {
        ApiTestScreen(navController)
      }
    }
  }
}

@Composable
fun LockScreenOrientation(orientation: Int) {
  val context = LocalContext.current
  DisposableEffect(Unit) {
    val activity = context.findActivity() ?: return@DisposableEffect onDispose {}
    val originalOrientation = activity.requestedOrientation
    activity.requestedOrientation = orientation
    onDispose {
      // restore original orientation when view disappears
      activity.requestedOrientation = originalOrientation
    }
  }
}

fun Context.findActivity(): Activity? = when (this) {
  is Activity -> this
  is ContextWrapper -> baseContext.findActivity()
  else -> null
}