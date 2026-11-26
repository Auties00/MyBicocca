package it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.component

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AlternateEmail
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.domain.model.mensa.MensaEmailCheck
import it.attendance100.mybicocca.domain.model.mensa.MensaPasswordRule
import it.attendance100.mybicocca.ui.component.button.RetryButton
import it.attendance100.mybicocca.ui.component.input.PasswordTextField
import it.attendance100.mybicocca.ui.component.modal.SheetLoadingIndicator
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.MensaSignUpState
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.MensaSignUpStep
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.MensaSignUpViewModel
import it.attendance100.mybicocca.ui.theme.LocalDarkTheme

private const val PRIVACY_URL = "https://unibicocca.compasscloud.it/privacy"

/**
 * Creating a canteen account: our own form over Compass's sign-up pages, and Compass's web form
 * itself ([MensaRegisterPage]) whenever those pages answer something the app does not recognise.
 * [onActivated] gets the new account's credentials (null if the web form could not provide them).
 */
@Composable
internal fun MensaSignUpRoute(
    onActivated: (MensaNewAccount?) -> Unit,
    modifier: Modifier = Modifier,
    onDirtyChange: (Boolean) -> Unit = {},
) {
    val viewModel: MensaSignUpViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentOnActivated by rememberUpdatedState(onActivated)
    val currentOnDirtyChange by rememberUpdatedState(onDirtyChange)
    // The sheet asks before leaving a form with something in it.
    LaunchedEffect(state.isDirty) { currentOnDirtyChange(state.isDirty) }
    DisposableEffect(Unit) { onDispose { currentOnDirtyChange(false) } }
    LaunchedEffect(viewModel) {
        viewModel.ensureStarted()
        viewModel.activated.collect { currentOnActivated(it) }
    }
    if (state.step == MensaSignUpStep.WEB_FORM) {
        MensaRegisterPage(onActivated = onActivated, modifier = modifier)
    } else {
        MensaSignUpPage(
            state = state,
            onFirstName = viewModel::setFirstName,
            onLastName = viewModel::setLastName,
            onEmail = viewModel::setEmail,
            onPassword = viewModel::setPassword,
            onPasswordConfirm = viewModel::setPasswordConfirm,
            onCaptcha = viewModel::setCaptcha,
            onRefreshCaptcha = viewModel::refreshCaptcha,
            onPrivacy = viewModel::setPrivacyAccepted,
            onSubmit = viewModel::submit,
            onCode = viewModel::setCode,
            onActivate = viewModel::activate,
            onEditForm = viewModel::editForm,
            onRetry = viewModel::start,
            modifier = modifier,
        )
    }
}

@Composable
internal fun MensaSignUpPage(
    state: MensaSignUpState,
    onFirstName: (String) -> Unit,
    onLastName: (String) -> Unit,
    onEmail: (String) -> Unit,
    onPassword: (String) -> Unit,
    onPasswordConfirm: (String) -> Unit,
    onCaptcha: (String) -> Unit,
    onRefreshCaptcha: () -> Unit,
    onPrivacy: (Boolean) -> Unit,
    onSubmit: () -> Unit,
    onCode: (String) -> Unit,
    onActivate: () -> Unit,
    onEditForm: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (state.step) {
        MensaSignUpStep.STARTING -> Box(modifier.fillMaxWidth().height(320.dp), contentAlignment = Alignment.Center) {
            SheetLoadingIndicator(label = stringResource(R.string.mensa_signup_opening))
        }
        MensaSignUpStep.OFFLINE -> Column(
            modifier = modifier.fillMaxWidth().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(Icons.Outlined.CloudOff, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = stringResource(R.string.mensa_register_failed),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            RetryButton(onClick = onRetry)
        }
        MensaSignUpStep.CODE -> CodeStep(state, onCode, onActivate, onEditForm, modifier)
        // The web form is drawn by the route; the form stands in for it in previews.
        MensaSignUpStep.FORM, MensaSignUpStep.WEB_FORM -> FormStep(
            state, onFirstName, onLastName, onEmail, onPassword, onPasswordConfirm, onCaptcha,
            onRefreshCaptcha, onPrivacy, onSubmit, modifier,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FormStep(
    state: MensaSignUpState,
    onFirstName: (String) -> Unit,
    onLastName: (String) -> Unit,
    onEmail: (String) -> Unit,
    onPassword: (String) -> Unit,
    onPasswordConfirm: (String) -> Unit,
    onCaptcha: (String) -> Unit,
    onRefreshCaptcha: () -> Unit,
    onPrivacy: (Boolean) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val emailRefused = state.emailCheck as? MensaEmailCheck.Refused
    val broken = state.brokenPasswordRules

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = state.firstName,
                onValueChange = onFirstName,
                singleLine = true,
                label = { Text(stringResource(R.string.mensa_signup_first_name)) },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                modifier = Modifier.weight(1f).semantics { contentType = ContentType.PersonFirstName },
            )
            OutlinedTextField(
                value = state.lastName,
                onValueChange = onLastName,
                singleLine = true,
                label = { Text(stringResource(R.string.mensa_signup_last_name)) },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                modifier = Modifier.weight(1f).semantics { contentType = ContentType.PersonLastName },
            )
        }

        OutlinedTextField(
            value = state.email,
            onValueChange = onEmail,
            singleLine = true,
            label = { Text(stringResource(R.string.mensa_login_email)) },
            leadingIcon = { Icon(Icons.Outlined.AlternateEmail, contentDescription = null) },
            trailingIcon = {
                when {
                    state.emailChecking -> CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    emailRefused != null -> Icon(Icons.Outlined.ErrorOutline, contentDescription = null, tint = scheme.error)
                    state.emailCheck == MensaEmailCheck.Usable -> Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = scheme.primary)
                }
            },
            isError = emailRefused != null,
            supportingText = emailRefused?.let { { Text(it.message) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentType = ContentType.NewUsername + ContentType.EmailAddress },
        )

        // A new password: password managers offer to generate one here.
        PasswordTextField(
            value = state.password,
            onValueChange = onPassword,
            labelRes = R.string.mensa_login_password,
            imeAction = ImeAction.Next,
            modifier = Modifier.fillMaxWidth().semantics { contentType = ContentType.NewPassword },
        )
        if (state.password.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                MensaPasswordRule.entries.forEach { rule ->
                    PasswordRuleLine(text = rule.label(), met = rule !in broken)
                }
            }
        }
        PasswordTextField(
            value = state.passwordConfirm,
            onValueChange = onPasswordConfirm,
            labelRes = R.string.mensa_signup_password_confirm,
            imeAction = ImeAction.Next,
            errorText = if (state.passwordsMatch) null else stringResource(R.string.mensa_signup_passwords_differ),
            modifier = Modifier.fillMaxWidth().semantics { contentType = ContentType.NewPassword },
        )

        CaptchaBlock(state, onCaptcha, onRefreshCaptcha)

        // The whole line ticks the box; only the link inside it opens the notice.
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .toggleable(value = state.privacyAccepted, role = Role.Checkbox, onValueChange = onPrivacy),
        ) {
            Checkbox(checked = state.privacyAccepted, onCheckedChange = null, modifier = Modifier.padding(12.dp))
            Text(
                text = buildAnnotatedString {
                    append(stringResource(R.string.mensa_signup_privacy_prefix))
                    withLink(LinkAnnotation.Url(PRIVACY_URL, TextLinkStyles(SpanStyle(color = scheme.primary, fontWeight = FontWeight.SemiBold)))) {
                        append(stringResource(R.string.mensa_signup_privacy_link))
                    }
                },
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        state.refusal?.let { reasons -> RefusalCard(reasons) }
        if (state.connectionError) {
            Text(
                text = stringResource(R.string.mensa_signup_connection),
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.error,
            )
        }

        PrimaryAction(
            label = stringResource(R.string.mensa_signup_submit),
            enabled = state.canSubmit,
            busy = state.submitting,
            onClick = onSubmit,
        )
    }
}

@Composable
private fun CaptchaBlock(state: MensaSignUpState, onCaptcha: (String) -> Unit, onRefresh: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val bitmap = remember(state.captchaImage) {
        state.captchaImage?.let { bytes -> BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap() }
    }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        // The captcha is drawn on white whatever the theme, so it stays readable as Compass made it.
        Surface(shape = RoundedCornerShape(14.dp), color = androidx.compose.ui.graphics.Color.White, modifier = Modifier.width(180.dp).height(72.dp)) {
            Box(contentAlignment = Alignment.Center) {
                when {
                    state.captchaLoading -> CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    bitmap != null -> Image(bitmap = bitmap, contentDescription = stringResource(R.string.mensa_signup_captcha), contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize())
                }
            }
        }
        FilledTonalIconButton(onClick = onRefresh, enabled = !state.captchaLoading) {
            Icon(Icons.Outlined.Refresh, contentDescription = stringResource(R.string.mensa_signup_captcha_refresh))
        }
    }
    OutlinedTextField(
        value = state.captcha,
        onValueChange = onCaptcha,
        singleLine = true,
        label = { Text(stringResource(R.string.mensa_signup_captcha)) },
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, keyboardType = KeyboardType.Ascii, imeAction = ImeAction.Done),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun PasswordRuleLine(text: String, met: Boolean) {
    val color = if (met) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(
            imageVector = if (met) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp),
        )
        Text(text = text, style = MaterialTheme.typography.labelMedium, color = color)
    }
}

@Composable
private fun MensaPasswordRule.label(): String = when (this) {
    MensaPasswordRule.LENGTH -> stringResource(R.string.mensa_signup_rule_length)
    MensaPasswordRule.UPPER_AND_LOWER -> stringResource(R.string.mensa_signup_rule_case)
    MensaPasswordRule.DIGIT -> stringResource(R.string.mensa_signup_rule_digit)
    MensaPasswordRule.SPECIAL -> stringResource(R.string.mensa_signup_rule_special, MensaPasswordRule.SPECIALS)
    MensaPasswordRule.ALLOWED_CHARACTERS -> stringResource(R.string.mensa_signup_rule_allowed)
}

@Composable
private fun RefusalCard(reasons: List<String>) {
    val scheme = MaterialTheme.colorScheme
    Surface(shape = RoundedCornerShape(18.dp), color = scheme.errorContainer, contentColor = scheme.onErrorContainer, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(stringResource(R.string.mensa_signup_refused_title), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            reasons.ifEmpty { listOf(stringResource(R.string.mensa_signup_refused_generic)) }.forEach {
                Text("• $it", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun CodeStep(
    state: MensaSignUpState,
    onCode: (String) -> Unit,
    onActivate: () -> Unit,
    onEditForm: () -> Unit,
    modifier: Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Image(
            painter = painterResource(R.drawable.mensa_art_signup_code),
            contentDescription = null,
            modifier = Modifier.width(180.dp).aspectRatio(160f / 120f),
        )
        Text(stringResource(R.string.mensa_signup_code_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Text(
            text = stringResource(R.string.mensa_signup_code_body, state.codeEmail),
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        CodeBoxes(
            code = state.code,
            onCode = onCode,
            isError = state.codeError != null,
            enabled = !state.submitting,
            onComplete = onActivate,
        )
        state.codeError?.let { error ->
            Text(error, style = MaterialTheme.typography.bodyMedium, color = scheme.error, textAlign = TextAlign.Center)
        }
        if (state.connectionError) {
            Text(stringResource(R.string.mensa_signup_connection), style = MaterialTheme.typography.bodyMedium, color = scheme.error)
        }
        PrimaryAction(
            label = stringResource(R.string.mensa_signup_activate),
            enabled = state.canActivate,
            busy = state.submitting,
            onClick = onActivate,
        )
        Text(
            text = stringResource(R.string.mensa_signup_spam),
            style = MaterialTheme.typography.bodySmall,
            color = scheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        TextButton(onClick = onEditForm, enabled = !state.submitting) { Text(stringResource(R.string.mensa_signup_edit)) }
    }
}

/** How many digits Compass's activation code has. */
private const val CODE_LENGTH = 6

/**
 * The emailed code as six boxes, a dash between the third and the fourth. One hidden field takes
 * the typing (and the keyboard's suggestion of the code from the email); the boxes only show it.
 * The sixth digit sends the code.
 */
@Composable
private fun CodeBoxes(
    code: String,
    onCode: (String) -> Unit,
    isError: Boolean,
    enabled: Boolean,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val focus = remember { FocusRequester() }
    var focused by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { focus.requestFocus() }
    val shape = RoundedCornerShape(14.dp)
    BasicTextField(
        value = code,
        onValueChange = { typed ->
            val digits = typed.filter(Char::isDigit).take(CODE_LENGTH)
            if (digits == code) return@BasicTextField
            onCode(digits)
            if (digits.length == CODE_LENGTH) onComplete()
        },
        enabled = enabled,
        singleLine = true,
        textStyle = TextStyle(color = Color.Transparent),
        cursorBrush = SolidColor(Color.Transparent),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { if (code.length == CODE_LENGTH) onComplete() }),
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(focus)
            .onFocusChanged { focused = it.isFocused }
            .semantics { contentType = ContentType.SmsOtpCode },
        decorationBox = { field ->
            Box {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    repeat(CODE_LENGTH) { index ->
                        if (index == CODE_LENGTH / 2) {
                            Text("–", style = MaterialTheme.typography.titleLarge, color = scheme.onSurfaceVariant)
                        }
                        val current = focused && index == code.length.coerceAtMost(CODE_LENGTH - 1)
                        val edge = when {
                            isError -> scheme.error
                            current -> scheme.primary
                            else -> scheme.outlineVariant
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(0.8f)
                                .clip(shape)
                                .background(scheme.surfaceContainerHigh)
                                .border(if (current || isError) 2.dp else 1.dp, edge, shape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = code.getOrNull(index)?.toString().orEmpty(),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
                // The field itself stays, invisible, so it keeps the keyboard and the autofill.
                Box(modifier = Modifier.matchParentSize().alpha(0f)) { field() }
            }
        },
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PrimaryAction(label: String, enabled: Boolean, busy: Boolean, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val dark = LocalDarkTheme.current
    val haptic = rememberHapticManager()
    val container = if (dark) scheme.primaryContainer else scheme.primary
    val content = if (dark) scheme.onPrimaryContainer else scheme.onPrimary
    Spacer(Modifier.height(2.dp))
    Button(
        onClick = { haptic.tap(); onClick() },
        enabled = enabled,
        shapes = ButtonDefaults.shapes(),
        modifier = Modifier.fillMaxWidth().heightIn(min = ButtonDefaults.MediumContainerHeight),
        contentPadding = ButtonDefaults.MediumContentPadding,
        colors = ButtonDefaults.buttonColors(containerColor = container, contentColor = content),
    ) {
        if (busy) {
            LoadingIndicator(modifier = Modifier.size(32.dp), color = content)
        } else {
            Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

private val PreviewForm = MensaSignUpState(
    step = MensaSignUpStep.FORM,
    firstName = "Giulia",
    lastName = "Demo",
    email = "giulia.demo@example.com",
    password = "Mensa2026",
    passwordConfirm = "Mensa202",
    emailCheck = MensaEmailCheck.Usable,
    refusal = listOf("Captcha non valido."),
)

@Composable
private fun PreviewPage(state: MensaSignUpState) {
    MensaSignUpPage(
        state = state, onFirstName = {}, onLastName = {}, onEmail = {}, onPassword = {}, onPasswordConfirm = {},
        onCaptcha = {}, onRefreshCaptcha = {}, onPrivacy = {}, onSubmit = {}, onCode = {}, onActivate = {},
        onEditForm = {}, onRetry = {},
    )
}

@Preview(name = "Sign-up form", widthDp = MENSA_PREVIEW_WIDTH, heightDp = 900)
@Composable
private fun SignUpFormPreview(@PreviewParameter(MensaPreviewThemeProvider::class) theme: MensaPreviewTheme) {
    MensaPreviewHost(theme) { PreviewPage(PreviewForm) }
}

@Preview(name = "Sign-up code", widthDp = MENSA_PREVIEW_WIDTH, heightDp = 640)
@Composable
private fun SignUpCodePreview(@PreviewParameter(MensaPreviewThemeProvider::class) theme: MensaPreviewTheme) {
    MensaPreviewHost(theme) {
        PreviewPage(MensaSignUpState(step = MensaSignUpStep.CODE, codeEmail = "giulia.demo@example.com", code = "1234", codeError = "Il codice non è corretto."))
    }
}
