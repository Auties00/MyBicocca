package it.attendance100.mybicocca.ui.screen.settings

import android.content.Intent
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import it.attendance100.mybicocca.BuildConfig
import it.attendance100.mybicocca.manager.UnlockResult
import it.attendance100.mybicocca.ui.component.input.PasswordTextField
import it.attendance100.mybicocca.ui.screen.lock.BiometricCapability
import it.attendance100.mybicocca.ui.screen.lock.errorMessage
import it.attendance100.mybicocca.ui.screen.lock.findFragmentActivity
import it.attendance100.mybicocca.ui.screen.lock.promptBiometric
import it.attendance100.mybicocca.ui.screen.lock.rememberBiometricCapability
import it.attendance100.mybicocca.ui.screen.settings.component.SettingsEntrySection
import it.attendance100.mybicocca.ui.screen.settings.state.SettingsEntry
import it.attendance100.mybicocca.ui.screen.settings.state.SettingsEntryGroup
import it.attendance100.mybicocca.ui.screen.settings.subscreen.language.LanguageSheet
import it.attendance100.mybicocca.ui.screen.settings.subscreen.language.currentAppLanguageLabel
import it.attendance100.mybicocca.ui.screen.settings.subscreen.settingsAppearance.SettingsAppearanceSheet

private const val GITHUB_URL = "https://github.com/Auties00/MyBicocca"

private val versionText: String = buildString {
    append("Versione ${BuildConfig.VERSION_NAME}")
    if (BuildConfig.DEBUG) append(" [Debug]")
}

// Landing of Settings: a directory grouped into connected segmented cards, mirroring the
// Registry tab's service directory style. Everything is handled in place — Aspetto and
// Lingua open modal sheets, Sicurezza toggles inline — so there are no sub-routes.
@Suppress("AssignedValueIsNeverRead")
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val activity = remember(context) { context.findFragmentActivity() }
    val capability = rememberBiometricCapability()

    val lockEnabled by viewModel.appLockEnabled.collectAsStateWithLifecycle()

    var showAppearanceSheet by remember { mutableStateOf(false) }
    var showLanguageSheet by remember { mutableStateOf(false) }

    // App-lock toggling requires authorization: biometric first, password as fallback.
    var showPasswordDialog by remember { mutableStateOf(false) }
    var pendingTarget by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var authorizing by remember { mutableStateOf(false) }

    val startLockToggle: () -> Unit = {
        val target = !lockEnabled
        if (capability == BiometricCapability.Available && activity != null) {
            promptBiometric(
                activity = activity,
                title = "Conferma identità",
                subtitle = if (target) "Attiva il blocco app" else "Disattiva il blocco app",
                negativeButton = "Usa password",
                onSuccess = { viewModel.setAppLockEnabled(target) },
                onError = { _, _ ->
                    pendingTarget = target
                    password = ""
                    passwordError = null
                    showPasswordDialog = true
                },
            )
        } else {
            pendingTarget = target
            password = ""
            passwordError = null
            showPasswordDialog = true
        }
    }

    // Refreshes when the sheet closes; a language change also recreates the activity anyway.
    val languageLabel = remember(showLanguageSheet) { currentAppLanguageLabel(context) }

    // GitHub opens in an in-app browser (Custom Tab) instead of kicking the user out
    // to the system browser, matching the Registry tab's external links.
    val openInAppBrowser: (String) -> Unit = remember(context) {
        { url -> CustomTabsIntent.Builder().setShowTitle(true).build().launchUrl(context, url.toUri()) }
    }

    // (group, iconChip container, iconChip onContainer) per section.
    val sections = listOf(
        Triple(
            SettingsEntryGroup(
                name = "Preferenze",
                caption = "Personalizza la tua esperienza",
                entries = listOf(
                    SettingsEntry("appearance", "Aspetto", "Tema e colori dell'app", Icons.Outlined.Palette, onClick = { showAppearanceSheet = true }),
                    SettingsEntry("language", "Lingua", languageLabel, Icons.Outlined.Translate, onClick = { showLanguageSheet = true }),
                    SettingsEntry("app_lock", "Sicurezza", "Blocco app", Icons.Outlined.Lock, switch = lockEnabled, onClick = startLockToggle),
                ),
            ),
            scheme.primaryContainer, scheme.onPrimaryContainer,
        ),
        Triple(
            SettingsEntryGroup(
                name = "Informazioni",
                caption = versionText,
                entries = listOf(
                    SettingsEntry("github", "GitHub", "Codice sorgente del progetto", Icons.Outlined.Code, external = true, onClick = { openInAppBrowser(GITHUB_URL) }),
                    SettingsEntry("license", "Licenza", "Licenze open source delle librerie", Icons.Outlined.Description, onClick = {
                        // Provided by play-services-oss-licenses; lists the bundled open-source libraries.
                        OssLicensesMenuActivity.setActivityTitle("Licenze open source")
                        context.startActivity(Intent(context, OssLicensesMenuActivity::class.java))
                    }),
                ),
            ),
            scheme.secondaryContainer, scheme.onSecondaryContainer,
        ),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        sections.forEach { (group, container, onContainer) ->
            SettingsEntrySection(
                group = group,
                accentContainer = container,
                accentOnContainer = onContainer,
            )
        }
    }

    if (showAppearanceSheet) {
        SettingsAppearanceSheet(onDismiss = { showAppearanceSheet = false })
    }

    if (showLanguageSheet) {
        LanguageSheet(onDismiss = { showLanguageSheet = false })
    }

    if (showPasswordDialog) {
        val authorize: () -> Unit = {
            if (!authorizing && password.isNotEmpty()) {
                authorizing = true
                passwordError = null
                viewModel.verifyPassword(password) { result ->
                    authorizing = false
                    if (result == UnlockResult.Success) {
                        viewModel.setAppLockEnabled(pendingTarget)
                        showPasswordDialog = false
                        password = ""
                    } else {
                        passwordError = result.errorMessage()
                    }
                }
            }
        }
        AlertDialog(
            onDismissRequest = { if (!authorizing) showPasswordDialog = false },
            title = { Text(if (pendingTarget) "Attiva blocco app" else "Disattiva blocco app") },
            text = {
                Column {
                    Text("Inserisci la tua password per confermare.")
                    Spacer(Modifier.height(12.dp))
                    PasswordTextField(
                        value = password,
                        onValueChange = { password = it; passwordError = null },
                        enabled = !authorizing,
                        errorText = passwordError,
                        onImeAction = authorize,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = authorize,
                    enabled = !authorizing && password.isNotEmpty(),
                ) { Text("Conferma") }
            },
            dismissButton = {
                TextButton(
                    onClick = { showPasswordDialog = false },
                    enabled = !authorizing,
                ) { Text("Annulla") }
            },
        )
    }
}
