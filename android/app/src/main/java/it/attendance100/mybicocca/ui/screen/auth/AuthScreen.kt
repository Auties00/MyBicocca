package it.attendance100.mybicocca.ui.screen.auth

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.domain.model.account.Account
import it.attendance100.mybicocca.ui.component.brand.MyBicoccaWordmark
import it.attendance100.mybicocca.ui.screen.auth.state.AuthEvent
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    onSignedIn: (account: Account, requiresCareerPick: Boolean) -> Unit,
    onCancel: (() -> Unit)? = null,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val username by viewModel.username.collectAsStateWithLifecycle()
    val password by viewModel.password.collectAsStateWithLifecycle()
    val inflight by viewModel.inflight.collectAsStateWithLifecycle()
    val esse3Error by viewModel.esse3Error.collectAsStateWithLifecycle()
    val elearningError by viewModel.elearningError.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is AuthEvent.SignedIn -> onSignedIn(event.account, event.requiresCareerPick)
            }
        }
    }

    if (onCancel != null) BackHandler(enabled = !inflight) { onCancel() }

    // Local SnackbarHost: AuthScreen lives in AppRoot, above MainShell's snackbar host, so it
    // can't reach it. The placeholder SPID/CIE actions surface their message through this one.
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { scaffoldPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(scaffoldPadding)) {
            if (onCancel != null) {
                IconButton(
                    onClick = onCancel,
                    enabled = !inflight,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .statusBarsPadding()
                        .padding(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Annulla",
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(96.dp))

                MyBicoccaWordmark(fontSize = 34.sp, sharedElement = true)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Accedi con le tue credenziali di Ateneo",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(40.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = viewModel::setUsername,
                    label = { Text("Username") },
                    leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null) },
                    enabled = !inflight,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = viewModel::setPassword,
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                contentDescription = if (passwordVisible) "Nascondi password" else "Mostra password",
                            )
                        }
                    },
                    enabled = !inflight,
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = viewModel::submit,
                    enabled = !inflight && username.isNotBlank() && password.isNotBlank(),
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                ) {
                    if (inflight) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    } else {
                        Text("Accedi")
                    }
                }

                esse3Error?.let { ErrorBanner(label = "Carriera (Esse3)", error = it) }
                elearningError?.let { ErrorBanner(label = "E-learning", error = it) }

                Spacer(Modifier.height(28.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f))
                    Text(
                        text = "oppure",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f))
                }
                Spacer(Modifier.height(20.dp))

                AlternativeLoginButton(
                    label = "Entra con SPID",
                    icon = painterResource(R.drawable.ic_spid),
                    iconTint = Color.Unspecified, // keep the official SPID blue
                    enabled = !inflight,
                    onClick = {
                        scope.launch {
                            snackbarHostState.showSnackbar("Accesso con SPID non ancora disponibile")
                        }
                    },
                )
                Spacer(Modifier.height(12.dp))
                AlternativeLoginButton(
                    label = "Entra con CIE",
                    icon = painterResource(R.drawable.ic_cie),
                    iconTint = MaterialTheme.colorScheme.onSurface,
                    enabled = !inflight,
                    onClick = {
                        scope.launch {
                            snackbarHostState.showSnackbar("Accesso con CIE non ancora disponibile")
                        }
                    },
                )

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun AlternativeLoginButton(
    label: String,
    icon: Painter,
    iconTint: Color,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        modifier = Modifier.fillMaxWidth().height(52.dp),
    ) {
        // No size override: both vectors are authored at 24dp tall (SPID wide, CIE near square),
        // so their intrinsic size keeps each logo's aspect ratio and a consistent height.
        Icon(
            painter = icon,
            contentDescription = null,
            tint = iconTint,
        )
        Spacer(Modifier.width(12.dp))
        Text(label)
    }
}

@Composable
private fun ErrorBanner(label: String, error: Throwable) {
    Spacer(Modifier.height(12.dp))
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            Text(
                text = error.message ?: error.javaClass.simpleName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
        }
    }
}
