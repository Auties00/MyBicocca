package it.attendance100.mybicocca.ui.screen.auth

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.text.LocalAutofillHighlightColor
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.outlined.WifiOff
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalAutofillManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.domain.model.account.Account
import it.attendance100.mybicocca.domain.model.account.SignInFailure
import it.attendance100.mybicocca.ui.component.brand.MyBicoccaWordmark
import it.attendance100.mybicocca.ui.component.button.rememberPressShrink
import it.attendance100.mybicocca.ui.component.button.rememberPressShrinkShape
import it.attendance100.mybicocca.ui.screen.auth.state.AuthEvent
import it.attendance100.mybicocca.ui.theme.BicoccaTheme
import it.attendance100.mybicocca.ui.theme.BicoccaWordmarkAccent
import kotlinx.coroutines.launch

// Initial-login entry: lives inside AppRoot's NavDisplay.
// Wraps the form body in a Scaffold + SnackbarHost.
@Composable
fun AuthScreen(
    onSignedIn: (account: Account, requiresCareerPick: Boolean) -> Unit,
    viewModel: AuthViewModel = hiltViewModel(
        checkNotNull(
            LocalViewModelStoreOwner.current
        ) {
            "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
        }, null
    ),
) {
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { scaffoldPadding ->
        AuthScreenBody(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding),
            onSignedIn = onSignedIn,
            onCancel = null,
            snackbarHostState = snackbarHostState,
            morphWordmark = true,
            interceptBack = true,
            viewModel = viewModel,
        )
    }
}

// In-sheet variant: rendered as the "login mode" scene of AccountSwitcherSheet's AnimatedContent.
// interceptBack = false so the sheet's outer PredictiveBackHandler receives the back event first.
@Composable
fun AuthScreenSheetContent(
    modifier: Modifier = Modifier,
    onSignedIn: (account: Account, requiresCareerPick: Boolean) -> Unit,
    onCancel: () -> Unit,
    cancelPaddingProvider: () -> Dp = { 32.dp },
    cancelOpacityProvider: () -> Float = { 1f },
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val snackbarHostState = remember { SnackbarHostState() }
    Box(modifier = modifier) {
        AuthScreenBody(
            modifier = Modifier.fillMaxSize(),
            onSignedIn = onSignedIn,
            onCancel = onCancel,
            cancelPaddingProvider = cancelPaddingProvider,
            cancelOpacityProvider = cancelOpacityProvider,
            snackbarHostState = snackbarHostState,
            morphWordmark = false,
            interceptBack = false,
            viewModel = viewModel,
        )
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun AuthScreenBody(
    modifier: Modifier,
    onSignedIn: (Account, Boolean) -> Unit,
    onCancel: (() -> Unit)?,
    cancelPaddingProvider: () -> Dp = { 32.dp },
    cancelOpacityProvider: () -> Float = { 1f },
    snackbarHostState: SnackbarHostState,
    morphWordmark: Boolean,
    interceptBack: Boolean,
    viewModel: AuthViewModel,
) {
    val username by viewModel.username.collectAsStateWithLifecycle()
    val password by viewModel.password.collectAsStateWithLifecycle()
    val inflight by viewModel.inflight.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()
    var passwordVisible by remember { mutableStateOf(false) }

    val autofillManager = LocalAutofillManager.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is AuthEvent.SignedIn -> {
                    autofillManager?.commit()
                    onSignedIn(event.account, event.requiresCareerPick)
                }
            }
        }
    }

    if (onCancel != null && interceptBack) BackHandler(enabled = !inflight) { onCancel() }

    val fieldsInError = error is SignInFailure.BadCredentials

    Box(modifier = modifier) {
        val autofillColorHighlight = BicoccaWordmarkAccent.copy(alpha = 0.1f)
        @Suppress("DEPRECATION")
        CompositionLocalProvider(LocalAutofillHighlightColor provides autofillColorHighlight) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(146.dp))

                MyBicoccaWordmark(fontSize = 34.sp, sharedElement = morphWordmark)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Accedi con le tue credenziali di Ateneo",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(60.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = viewModel::setUsername,
                    label = { Text("Username o Email") },
                    placeholder = { Text("m.rossi1") },
                    leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null) },
                    enabled = !inflight,
                    singleLine = true,
                    isError = fieldsInError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            contentType = ContentType.Username + ContentType.EmailAddress
                        },
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
                    isError = fieldsInError,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentType = ContentType.Password },
                )

                AuthFailureCard(failure = error)

                Spacer(Modifier.height(24.dp))
                val (accediInteractionSource, accediShape) = rememberPressShrink()
                Button(
                    onClick = viewModel::submit,
                    enabled = !inflight && username.isNotBlank() && password.isNotBlank(),
                    interactionSource = accediInteractionSource,
                    shape = accediShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                ) {
                    if (inflight) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    } else {
                        Text("Accedi", color = Color.White)
                    }
                }

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
                Spacer(Modifier.height(28.dp))

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

        if (onCancel != null) {
            val (cancelInteractionSource, cancelShape) = rememberPressShrink()

            OutlinedButton(
                onClick = onCancel,
                enabled = !inflight,
                interactionSource = cancelInteractionSource,
                shape = cancelShape,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(horizontal = cancelPaddingProvider())
                    .padding(top = cancelPaddingProvider())
                    .fillMaxWidth()
                    .height(48.dp)
                    .alpha((0.5f - cancelOpacityProvider()).coerceIn(0f, 1f))
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowUpward,
                    contentDescription = "Annulla",
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AlternativeLoginButtonPreview() {
    BicoccaTheme(dark = false) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AlternativeLoginButton(
                label = "Entra con SPID",
                icon = painterResource(R.drawable.ic_spid),
                iconTint = Color.Unspecified,
                enabled = true,
                onClick = {}
            )
            AlternativeLoginButton(
                label = "Entra con CIE",
                icon = painterResource(R.drawable.ic_cie),
                iconTint = MaterialTheme.colorScheme.onSurface,
                enabled = true,
                onClick = {}
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0606)
@Composable
private fun AuthFailureCardBadCredentialsPreview() {
    BicoccaTheme(dark = true) {
        AuthFailureCard(failure = SignInFailure.BadCredentials)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B0808)
@Composable
private fun AuthFailureCardNoConnectionPreview() {
    BicoccaTheme(dark = true) {
        AuthFailureCard(failure = SignInFailure.NoConnection)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0606)
@Composable
private fun AuthFailureCardUnknownPreview() {
    BicoccaTheme(dark = true) {
        AuthFailureCard(failure = SignInFailure.Unknown)
    }
}

private val AlternativeLoginPressedColor = Color(0xFF0066CC)

@Composable
private fun AlternativeLoginButton(
    label: String,
    icon: Painter,
    iconTint: Color,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val containerColor by animateColorAsState(
        targetValue = if (isPressed) AlternativeLoginPressedColor else Color.Transparent,
        label = "altLoginContainer",
    )
    val contentColor by animateColorAsState(
        targetValue = if (isPressed) Color.White else MaterialTheme.colorScheme.onSurface,
        label = "altLoginContent",
    )
    val borderColor by animateColorAsState(
        targetValue = if (isPressed) AlternativeLoginPressedColor else MaterialTheme.colorScheme.outline,
        label = "altLoginBorder",
    )

    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
        border = BorderStroke(1.dp, borderColor),
        shape = rememberPressShrinkShape(interactionSource),
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
    ) {
        Icon(
            painter = icon,
            contentDescription = null,
            tint = if (isPressed) Color.White else iconTint,
        )
        Spacer(Modifier.width(12.dp))
        Text(label)
    }
}

@Composable
private fun AuthFailureCard(failure: SignInFailure?) {
    AnimatedVisibility(
        visible = failure != null,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
    ) {
        val (icon, title, body) = when (failure) {
            is SignInFailure.BadCredentials -> Triple(
                Icons.Outlined.ErrorOutline,
                "Credenziali non valide",
                "Controlla username e password e riprova.",
            )

            is SignInFailure.NoConnection -> Triple(
                Icons.Outlined.WifiOff,
                "Connessione non disponibile",
                "Controlla la rete e riprova.",
            )

            is SignInFailure.Unknown, null -> Triple(
                Icons.Outlined.ErrorOutline,
                "Accesso non riuscito",
                "Si è verificato un errore imprevisto. Riprova tra qualche istante.",
            )
        }
        Column {
            Spacer(Modifier.height(12.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(14.dp),
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(20.dp),
                    )
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = body,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                        )
                    }
                }
            }
        }
    }
}