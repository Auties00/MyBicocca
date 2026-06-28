package it.attendance100.mybicocca.ui.component.input

import androidx.annotation.StringRes
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.R

/**
 * Shared password entry field: single-line, password keyboard + autofill semantics, and a built-in
 * show/hide toggle.
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
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
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
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = imeAction
        ),
        keyboardActions = onImeAction
            ?.let { action -> KeyboardActions(onDone = { action() }, onGo = { action() }) }
            ?: KeyboardActions.Default,
    )
}
