package it.attendance100.mybicocca.ui.screen.settings.subscreen.settingsSecurity

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.manager.UnlockResult
import it.attendance100.mybicocca.ui.component.input.PasswordTextField
import it.attendance100.mybicocca.ui.screen.lock.BiometricCapability
import it.attendance100.mybicocca.ui.screen.lock.errorMessage
import it.attendance100.mybicocca.ui.screen.lock.findFragmentActivity
import it.attendance100.mybicocca.ui.screen.lock.promptBiometric
import it.attendance100.mybicocca.ui.screen.lock.rememberBiometricCapability
import it.attendance100.mybicocca.ui.screen.settings.component.preference.OpenDialogTile
import it.attendance100.mybicocca.ui.screen.settings.component.preference.SettingsSectionTitle
import it.attendance100.mybicocca.ui.screen.settings.component.preference.SwitchSettingTile

private fun timeoutLabel(minutes: Int): String = when (minutes) {
    0 -> "Immediatamente"
    60 -> "1 ora"
    240 -> "4 ore"
    else -> "$minutes min"
}

private val TIMEOUT_ENTRIES = listOf(0, 1, 5, 10, 15, 30, 60, 240).associateWith(::timeoutLabel)

@Suppress("AssignedValueIsNeverRead")
@Composable
fun SettingsSecurityScreen(
    viewModel: SettingsSecurityViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findFragmentActivity() }
    val capability = rememberBiometricCapability()

    val enabled by viewModel.enabled.collectAsStateWithLifecycle()
    val timeoutMinutes by viewModel.timeoutMinutes.collectAsStateWithLifecycle()
    val secureScreen by viewModel.secureScreen.collectAsStateWithLifecycle()

    var showPasswordDialog by remember { mutableStateOf(false) }
    var pendingTarget by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var authorizing by remember { mutableStateOf(false) }

    val startToggle: () -> Unit = {
        val target = !enabled
        if (capability == BiometricCapability.Available && activity != null) {
            promptBiometric(
                activity = activity,
                title = "Conferma identità",
                subtitle = if (target) "Attiva il blocco app" else "Disattiva il blocco app",
                negativeButton = "Usa password",
                onSuccess = { viewModel.setEnabled(target) },
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 16.dp),
    ) {
        SettingsSectionTitle("Blocco app")

        SwitchSettingTile(
            label = "Richiedi sblocco",
            description = "Sblocca con impronta, volto o password all'avvio",
            isToggled = enabled,
            onToggle = { startToggle() },
        )

        AnimatedVisibility(visible = enabled) {
            Column {
                OpenDialogTile(
                    title = "Blocca quando inattivo",
                    value = timeoutMinutes,
                    entries = TIMEOUT_ENTRIES,
                    onValueChange = { viewModel.setTimeout(it) },
                )
                SwitchSettingTile(
                    label = "Schermo privato",
                    description = "Nascondi l'app nelle anteprime e blocca gli screenshot",
                    isToggled = secureScreen,
                    onToggle = { viewModel.setSecureScreen(it) },
                )
            }
        }

        if (capability == BiometricCapability.NoneEnrolled) {
            Text(
                text = "Nessun dato biometrico registrato su questo dispositivo: verrà usata la password.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
    }

    if (showPasswordDialog) {
        val authorize: () -> Unit = {
            if (!authorizing && password.isNotEmpty()) {
                authorizing = true
                passwordError = null
                viewModel.verifyPassword(password) { result ->
                    authorizing = false
                    if (result == UnlockResult.Success) {
                        viewModel.setEnabled(pendingTarget)
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
