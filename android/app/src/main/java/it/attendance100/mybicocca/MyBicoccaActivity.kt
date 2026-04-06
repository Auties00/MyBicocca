package it.attendance100.mybicocca

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dagger.hilt.android.AndroidEntryPoint
import it.attendance100.mybicocca.ui.navigation.MyBicoccaNavHost
import it.attendance100.mybicocca.ui.theme.MyBicoccaTheme
import it.attendance100.mybicocca.util.PreferencesManager
import it.attendance100.mybicocca.util.ProvideHapticManager
import it.attendance100.mybicocca.util.rememberPreferencesManager

@AndroidEntryPoint
class MyBicoccaActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val preferencesManager = rememberPreferencesManager()
            var selectedThemeMode by remember { mutableStateOf(preferencesManager.themeMode) }

            val darkTheme = when (selectedThemeMode) {
                PreferencesManager.THEME_DARK -> true
                PreferencesManager.THEME_LIGHT -> false
                else -> isSystemInDarkTheme()
            }

            MyBicoccaTheme(darkTheme = darkTheme) {
                ProvideHapticManager {
                    MyBicoccaNavHost(
                        onThemeModeChanged = { themeMode ->
                            selectedThemeMode = themeMode
                        },
                    )
                }
            }
        }
    }
}
