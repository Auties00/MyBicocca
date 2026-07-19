package it.attendance100.mybicocca

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import it.attendance100.mybicocca.core.crash.GlobalExceptionHandler
import it.attendance100.mybicocca.ui.screen.crash.CrashScreen
import it.attendance100.mybicocca.ui.theme.BicoccaTheme

/**
 * Host for [CrashScreen], launched by [GlobalExceptionHandler] after an uncaught exception.
 * Runs in the separate `:crash` process (see the manifest) so it survives the main process's death.
 *
 * Deliberately not @AndroidEntryPoint and themed with the default palette + system dark mode:
 * the DataStore-backed theme pipeline is not worth spinning up in a dying-app context.
 * Restart drops this task and cold-starts [MyBicoccaActivity] fresh.
 */
class CrashActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val stackTrace = GlobalExceptionHandler.stackTraceFromIntent(intent)
        setContent {
            BicoccaTheme(dark = isSystemInDarkTheme()) {
                CrashScreen(
                    stackTrace = stackTrace,
                    onRestart = {
                        finishAffinity()
                        startActivity(
                            Intent(this, MyBicoccaActivity::class.java)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                        )
                    },
                )
            }
        }
    }
}
