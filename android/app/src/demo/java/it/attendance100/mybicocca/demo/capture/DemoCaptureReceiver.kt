package it.attendance100.mybicocca.demo.capture

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import it.attendance100.mybicocca.core.os.applyAppLanguage
import it.attendance100.mybicocca.domain.model.settings.AppTheme
import it.attendance100.mybicocca.domain.model.settings.ThemeMode
import it.attendance100.mybicocca.domain.repository.AppearanceSettingsRepository
import it.attendance100.mybicocca.ui.navigation.ShellCommand
import it.attendance100.mybicocca.ui.navigation.ShellCommands
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

/**
 * Lets the capture script steer the running demo app over adb:
 *
 *     adb shell am broadcast -n <package>/<this class> --es shot Screenshot_Fees
 *     adb shell am broadcast -n <package>/<this class> --es palette Oceano
 *     adb shell am broadcast -n <package>/<this class> --es language en
 *
 * `shot` jumps to a catalogue entry; `palette` picks the colour palette and leaves light or dark
 * to the system, which the script switches itself; `language` pins the app language, taking
 * effect when the app is next started. `--ez stack true` answers with the routes now on the back
 * stack, so the script can tell a shot that landed on the wrong screen.
 */
@AndroidEntryPoint
class DemoCaptureReceiver : BroadcastReceiver() {

    @Inject
    lateinit var appearance: AppearanceSettingsRepository

    override fun onReceive(context: Context, intent: Intent) {
        intent.getStringExtra(EXTRA_PALETTE)?.let { name ->
            val palette = AppTheme.entries.firstOrNull { it.name.equals(name, ignoreCase = true) }
            if (palette == null) {
                Log.w(TAG, "Unknown palette $name")
            } else {
                runBlocking {
                    appearance.setAppTheme(palette)
                    appearance.setThemeMode(ThemeMode.System)
                }
            }
        }
        intent.getStringExtra(EXTRA_LANGUAGE)?.let { tag ->
            // Same switch the language page flips: without it the app re-applies the system language at startup.
            context.getSharedPreferences("language_prefs", Context.MODE_PRIVATE).edit().putBoolean("is_system", false).apply()
            applyAppLanguage(context, tag)
        }
        if (intent.hasExtra(EXTRA_STACK)) {
            resultData = ShellCommands.stack.joinToString(",") { it::class.java.name.removePrefix(ScreenshotCatalog.ROUTE_PACKAGE).replace('$', '.') }
        }
        intent.getStringExtra(EXTRA_SHOT)?.let { name ->
            val catalog = ScreenshotCatalog.parse(context.assets.open(ScreenshotCatalog.ASSET).reader().use { it.readText() })
            val shot = catalog.shot(name)
            if (shot == null) Log.w(TAG, "Unknown shot $name") else ShellCommands.send(ShellCommand(shot.tab, shot.routes, shot.buildingsList, shot.building))
        }
    }

    private companion object {
        const val TAG = "DemoCapture"
        const val EXTRA_SHOT = "shot"
        const val EXTRA_PALETTE = "palette"
        const val EXTRA_LANGUAGE = "language"
        const val EXTRA_STACK = "stack"
    }
}
