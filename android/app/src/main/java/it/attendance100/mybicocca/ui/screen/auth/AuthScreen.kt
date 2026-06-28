package it.attendance100.mybicocca.ui.screen.auth

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalAutofillManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.domain.model.account.Account
import it.attendance100.mybicocca.domain.model.account.SignInFailure
import it.attendance100.mybicocca.ui.component.brand.MyBicoccaWordmark
import it.attendance100.mybicocca.ui.component.input.PasswordTextField
import it.attendance100.mybicocca.ui.screen.auth.component.rememberPressShrink
import it.attendance100.mybicocca.ui.screen.auth.component.rememberPressShrinkShape
import it.attendance100.mybicocca.ui.screen.auth.state.AuthEvent
import it.attendance100.mybicocca.ui.theme.BicoccaTheme
import it.attendance100.mybicocca.ui.theme.BicoccaWordmarkAccent
import it.attendance100.mybicocca.ui.theme.LocalIsOnline
import kotlinx.coroutines.launch

/**
 * Full-screen sign-in with university credentials — the initial-login entry hosted in
 * AppRoot's NavDisplay. Wraps the shared form body in a [Scaffold] with a [SnackbarHost]
 * for failure messages, and lets the wordmark participate in the shared-element morph
 * with the main shell's app bar.
 */
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

/**
 * In-sheet variant of the sign-in form, rendered as the "login mode" scene of
 * AccountSwitcherSheet's AnimatedContent. Back is not intercepted here so the sheet's
 * outer PredictiveBackHandler receives the event first; the wordmark stays static (no
 * shared-element morph) and the snackbar host is drawn inside the sheet, where a system
 * snackbar would otherwise sit behind the modal. [cancelPaddingProvider] and
 * [cancelOpacityProvider] let the host animate the cancel button in sync with the sheet's
 * scene transition.
 */
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

/**
 * The sign-in form itself: wordmark + tagline header, username/email and password fields
 * with autofill semantics and a tinted autofill highlight, a brand-red submit button that
 * swaps its label for a spinner while the request is in flight (and stays disabled while
 * offline or with blank fields), then SPID/CIE alternatives under an "oppure" divider.
 * The SPID/CIE buttons are intentional placeholders that only surface an availability
 * notice. The column scrolls and pads above the IME so the keyboard never covers the
 * fields, and rejected credentials keep both fields in their error state until edited.
 * When [onCancel] is provided, an arrow-up outlined button is pinned to the top with
 * padding/opacity driven by the host's transition providers.
 */
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
    val fieldsInError by viewModel.credentialsRejected.collectAsStateWithLifecycle()

    val haptic = rememberHapticManager()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val autofillManager = LocalAutofillManager.current

    val spidUnavailableMsg = stringResource(R.string.auth_spid_unavailable)
    val cieUnavailableMsg = stringResource(R.string.auth_cie_unavailable)

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is AuthEvent.SignedIn -> {
                    autofillManager?.commit()
                    onSignedIn(event.account, event.requiresCareerPick)
                }

                is AuthEvent.Failed -> {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    snackbarHostState.showSnackbar(context.applicationContext.getString(event.reason.toStringRes()))
                }
            }
        }
    }

    if (onCancel != null && interceptBack) BackHandler(enabled = !inflight) { onCancel() }

    Box(modifier = modifier.testTag(AuthScreenTestTags.ROOT)) {
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
                    text = stringResource(R.string.auth_tagline),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(60.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = viewModel::setUsername,
                    label = { Text(stringResource(R.string.auth_username_label)) },
                    placeholder = { Text(stringResource(R.string.auth_username_placeholder)) },
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
                        .testTag(AuthScreenTestTags.USERNAME_FIELD)
                        .semantics {
                            contentType = ContentType.Username + ContentType.EmailAddress
                        },
                )

                Spacer(Modifier.height(12.dp))
                PasswordTextField(
                    value = password,
                    onValueChange = viewModel::setPassword,
                    enabled = !inflight,
                    isError = fieldsInError,
                    leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(AuthScreenTestTags.PASSWORD_FIELD),
                )

                Spacer(Modifier.height(24.dp))
                val (accediInteractionSource, accediShape) = rememberPressShrink()
                val keyboardController = LocalSoftwareKeyboardController.current
                Button(
                    onClick = {
                        haptic.tap()
                        viewModel.submit()
                        keyboardController?.hide()
                    },
                    enabled = !inflight && username.isNotBlank() && password.isNotBlank() &&
                            LocalIsOnline.current,
                    interactionSource = accediInteractionSource,
                    shape = accediShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag(AuthScreenTestTags.SUBMIT_BUTTON),
                ) {
                    if (inflight) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    } else {
                        Text(stringResource(R.string.auth_sign_in_button), color = Color.White)
                    }
                }

                Spacer(Modifier.height(28.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f))
                    Text(
                        text = stringResource(R.string.common_or),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f))
                }
                Spacer(Modifier.height(28.dp))

                AlternativeLoginButton(
                    labelRes = R.string.auth_spid_button,
                    icon = painterResource(R.drawable.ic_spid),
                    iconTint = Color.Unspecified,
                    enabled = !inflight,
                    onClick = {
                        haptic.tap()
                        scope.launch {
                            snackbarHostState.showSnackbar(spidUnavailableMsg)
                        }
                    },
                    modifier = Modifier.testTag(AuthScreenTestTags.SPID_BUTTON),
                )
                Spacer(Modifier.height(12.dp))
                AlternativeLoginButton(
                    labelRes = R.string.auth_cie_button,
                    icon = painterResource(R.drawable.ic_cie),
                    iconTint = MaterialTheme.colorScheme.onSurface,
                    enabled = !inflight,
                    onClick = {
                        haptic.tap()
                        scope.launch {
                            snackbarHostState.showSnackbar(cieUnavailableMsg)
                        }
                    },
                    modifier = Modifier.testTag(AuthScreenTestTags.CIE_BUTTON),
                )

                Spacer(Modifier.height(32.dp))
            }
        }

        if (onCancel != null) {
            val (cancelInteractionSource, cancelShape) = rememberPressShrink()

            OutlinedButton(
                onClick = { haptic.tap(); onCancel() },
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
                    .testTag(AuthScreenTestTags.CANCEL_BUTTON)
                    .alpha((0.5f - cancelOpacityProvider()).coerceIn(0f, 1f))
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowUpward,
                    contentDescription = stringResource(R.string.common_cancel),
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

/**
 * User-visible snackbar copy for a failed sign-in. NoConnection covers both a dead local
 * network and an unreachable/down backend (both surface as IOException), so the copy must
 * not assert it's the user's fault — it offers retry-later as an alternative.
 */
private fun SignInFailure.toStringRes(): Int = when (this) {
    is SignInFailure.BadCredentials ->
        R.string.auth_error_bad_credentials

    is SignInFailure.NoConnection ->
        R.string.auth_error_no_connection

    is SignInFailure.AlreadySignedIn ->
        R.string.auth_error_already_signed_in

    is SignInFailure.Unknown ->
        R.string.auth_error_unknown
}

private val AlternativeLoginPressedColor = Color(0xFF0066CC)

/**
 * Outlined SSO entry button (icon + label) that floods with the institutional SPID blue
 * while pressed, inverting icon and text to white and shrinking slightly under the finger.
 * Pass [Color.Unspecified] as [iconTint] to preserve a multicolor brand icon's intrinsic
 * palette (the official SPID mark).
 */
@Composable
private fun AlternativeLoginButton(
    modifier: Modifier = Modifier,
    label: String? = null,
    @StringRes labelRes: Int? = null,
    icon: Painter,
    iconTint: Color,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val finalLabel = when {
        label != null -> label
        labelRes != null -> stringResource(labelRes)
        else -> ""
    }
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
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
    ) {
        Icon(
            painter = icon,
            contentDescription = null,
            tint = if (isPressed) Color.White else iconTint,
        )
        Spacer(Modifier.width(12.dp))
        Text(finalLabel)
    }
}