package it.attendance100.mybicocca.ui.component.input

import android.content.res.Configuration
import android.provider.Settings
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.ui.theme.BicoccaTheme
import kotlinx.coroutines.delay

/** How long a typed character stays readable before it turns into a dot. */
private const val REVEAL_MILLIS = 1000L

/**
 * Shared password entry field: single-line, password keyboard + autofill semantics, and a built-in
 * show/hide toggle. Like the platform's password fields, a character typed on its own stays
 * readable for a second, unless the system's "Show passwords" setting is off.
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
    var revealIndex by remember { mutableIntStateOf(-1) }
    // Bumped on every keystroke, so typing at the same position again restarts the timer.
    var revealSerial by remember { mutableIntStateOf(0) }
    LaunchedEffect(revealSerial) {
        if (revealIndex >= 0) {
            delay(REVEAL_MILLIS)
            revealIndex = -1
        }
    }

    OutlinedTextField(
        value = value,
        onValueChange = { new ->
            revealIndex = if (revealAllowed) singleInsertionIndex(value, new) else -1
            revealSerial++
            onValueChange(new)
        },
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
        singleLine = true,
        isError = isError || errorText != null,
        supportingText = errorText?.let { { Text(it) } },
        visualTransformation = if (visible) VisualTransformation.None else RevealOneTransformation(revealIndex),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = imeAction
        ),
        keyboardActions = onImeAction
            ?.let { action -> KeyboardActions(onDone = { action() }, onGo = { action() }) }
            ?: KeyboardActions.Default,
    )
}

/**
 * Where [new] gained exactly one character over [old], or -1 for anything else: deletions,
 * pastes and autofill stay hidden.
 */
internal fun singleInsertionIndex(old: String, new: String): Int {
    if (new.length != old.length + 1) return -1
    val at = old.commonPrefixWith(new).length
    return if (new.regionMatches(at + 1, old, at, old.length - at)) at else -1
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

/** Masks every character as a dot, except the one at [revealIndex]. */
private data class RevealOneTransformation(val revealIndex: Int) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val masked = buildString(text.length) {
            text.text.forEachIndexed { i, c -> append(if (i == revealIndex) c else '•') }
        }
        return TransformedText(AnnotatedString(masked), OffsetMapping.Identity)
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
