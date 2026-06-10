package it.attendance100.mybicocca.ui.screen.lock

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.domain.model.security.UnlockResult
import it.attendance100.mybicocca.ui.component.input.PasswordTextField

/**
 * Full-screen gate shown over the signed-in UI while the app is locked: a lock glyph,
 * "App bloccata" and the active username, centered. Auto-presents the system biometric
 * sheet whenever biometric mode applies — on first show and again on every "Sblocca" retry;
 * otherwise (or when the user taps "Usa password", or after a biometric error, cancel or
 * lockout) it falls back to a password field with inline error copy and a spinner in the
 * submit button while verifying. Back must not reveal the content behind, so it sends the
 * task to the background instead.
 *
 * Rendered as an opaque [Surface], which blocks touch propagation to the content behind it.
 */
@Composable
fun AppLockScreen(viewModel: AppLockViewModel) {
    val context = LocalContext.current
    val activity = remember(context) { context.findFragmentActivity() }
    val capability = rememberBiometricCapability()

    val username by viewModel.username.collectAsStateWithLifecycle()
    val verifying by viewModel.verifying.collectAsStateWithLifecycle()

    var usePassword by remember { mutableStateOf(capability != BiometricCapability.Available) }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var biometricTrigger by remember { mutableIntStateOf(0) }

    val submit: () -> Unit = submit@{
        if (password.isEmpty() || verifying) return@submit
        error = null
        viewModel.verifyPassword(password) { result ->
            if (result == UnlockResult.Success) password = ""
            error = result.errorMessage()
        }
    }

    LaunchedEffect(usePassword, biometricTrigger) {
        if (!usePassword && capability == BiometricCapability.Available && activity != null) {
            promptBiometric(
                activity = activity,
                title = "Sblocca MyBicocca",
                subtitle = username.orEmpty(),
                negativeButton = "Usa password",
                onSuccess = viewModel::onBiometricSuccess,
                onError = { _, _ -> usePassword = true },
            )
        }
    }

    BackHandler(enabled = true) { activity?.moveTaskToBack(true) }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp),
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "App bloccata",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            username?.takeIf { it.isNotBlank() }?.let {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(32.dp))

            if (usePassword) {
                PasswordTextField(
                    value = password,
                    onValueChange = { password = it; error = null },
                    enabled = !verifying,
                    errorText = error,
                    onImeAction = submit,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = submit,
                    enabled = !verifying && password.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (verifying) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    } else {
                        Text("Sblocca")
                    }
                }
                if (capability == BiometricCapability.Available) {
                    TextButton(onClick = { usePassword = false; biometricTrigger++ }) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Fingerprint,
                                contentDescription = null,
                            )
                            Text("Usa impronta o volto")
                        }
                    }
                }
            } else {
                Button(
                    onClick = { biometricTrigger++ },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Sblocca") }
                TextButton(onClick = { usePassword = true }) { Text("Usa password") }
            }
        }
    }
}
