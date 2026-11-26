package it.attendance100.mybicocca.ui.component.input

import android.content.res.Configuration
import android.provider.Settings
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.OutlinedSecureTextField
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.remember
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.ui.theme.BicoccaTheme

/**
 * Shared password entry field: single-line, password keyboard + autofill semantics, and a built-in
 * show/hide toggle. It is a secure text field, so it is announced as a password, offers no copy or
 * cut, and, like the platform's own, shows the last character typed for a moment, unless the
 * system's "Show passwords" setting is off.
 * @param errorText when non-null, shown as the supporting text and puts the field in the error state.
 * @param onImeAction invoked when the IME action key is pressed; null leaves the default.
 */
@Composable
fun PasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    @StringRes labelRes: Int = R.string.common_password,
    enabled: Boolean = true,
    isError: Boolean = false,
    errorText: String? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    imeAction: ImeAction = ImeAction.Done,
    onImeAction: (() -> Unit)? = null,
) {
    val haptic = rememberHapticManager()
    var visible by rememberSaveable { mutableStateOf(false) }
    val revealAllowed = rememberShowPasswordsSetting()

    // The secure field keeps its text in a state of its own; the caller still owns the value, so
    // the two follow each other: typing reports up, and a value set from outside (cleared after a
    // failed sign-in) comes down.
    val state = rememberTextFieldState(value)
    val latestValue by rememberUpdatedState(value)
    val latestOnValueChange by rememberUpdatedState(onValueChange)
    // The last text reported up. Its echo can come back after newer typing, so only a value that
    // is not an echo replaces the field's text; otherwise fast typing would lose characters.
    val reported = remember { mutableStateOf(value) }
    LaunchedEffect(value) {
        if (value != reported.value && state.text.toString() != value) {
            reported.value = value
            state.setTextAndPlaceCursorAtEnd(value)
        }
    }
    LaunchedEffect(state) {
        snapshotFlow { state.text.toString() }.collect { typed ->
            if (typed != latestValue) {
                reported.value = typed
                latestOnValueChange(typed)
            }
        }
    }

    OutlinedSecureTextField(
        state = state,
        modifier = modifier.semantics { contentType = ContentType.Password },
        label = { Text(stringResource(labelRes)) },
        leadingIcon = leadingIcon,
        trailingIcon = {
            IconButton(onClick = {
                haptic.tap()
                visible = !visible
            }) {
                Icon(
                    imageVector = if (visible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                    contentDescription = stringResource(if (visible) R.string.common_hide_password else R.string.common_show_password),
                )
            }
        },
        enabled = enabled,
        isError = isError || errorText != null,
        supportingText = errorText?.let { { Text(it) } },
        textObfuscationMode = when {
            visible -> TextObfuscationMode.Visible
            revealAllowed -> TextObfuscationMode.RevealLastTyped
            else -> TextObfuscationMode.Hidden
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = imeAction
        ),
        onKeyboardAction = onImeAction?.let { action -> KeyboardActionHandler { action() } },
    )
}

/** The system's "Show passwords" switch, read once; on unless the user turned it off. */
@Composable
private fun rememberShowPasswordsSetting(): Boolean {
    if (LocalInspectionMode.current) return true
    val resolver = LocalContext.current.contentResolver
    return remember(resolver) {
        runCatching { Settings.System.getInt(resolver, Settings.System.TEXT_SHOW_PASSWORD, 1) == 1 }
            .getOrDefault(true)
    }
}

@Preview(name = "Password field, light")
@Preview(name = "Password field, dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PasswordTextFieldPreview() {
    val dark = (LocalConfiguration.current.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    BicoccaTheme(dark = dark) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PasswordTextField(value = "", onValueChange = {}, modifier = Modifier.fillMaxWidth())
                PasswordTextField(value = "Secret1!", onValueChange = {}, modifier = Modifier.fillMaxWidth())
                PasswordTextField(
                    value = "Secret1!",
                    onValueChange = {},
                    errorText = "Wrong password",
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
