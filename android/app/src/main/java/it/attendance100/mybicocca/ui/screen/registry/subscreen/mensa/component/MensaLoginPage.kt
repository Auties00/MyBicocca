package it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AlternateEmail
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PersonAddAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.ui.component.input.PasswordTextField
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.MensaTestTags
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.artwork.MensaArt
import it.attendance100.mybicocca.ui.theme.LocalDarkTheme

/**
 * Sign in to the canteen account, or create one. The sheet's header says what this is, so the page
 * is only the plates, the two fields and the two ways forward. A bare university username is
 * accepted too: the view model adds the campus domain.
 *
 * @param initialEmail the email of an account just created, filled in for the user.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun MensaLoginPage(
    submitting: Boolean,
    onLogin: (email: String, password: String) -> Unit,
    onCreateAccount: () -> Unit,
    modifier: Modifier = Modifier,
    initialEmail: String? = null,
) {
    val scheme = MaterialTheme.colorScheme
    val dark = LocalDarkTheme.current
    val haptic = rememberHapticManager()
    val brandContainer = if (dark) scheme.primaryContainer else scheme.primary
    val brandContent = if (dark) scheme.onPrimaryContainer else scheme.onPrimary

    var email by rememberSaveable { mutableStateOf(initialEmail.orEmpty()) }
    var password by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(initialEmail) {
        if (!initialEmail.isNullOrBlank() && email.isBlank()) email = initialEmail
    }
    val canSubmit = !submitting && email.isNotBlank() && password.isNotBlank()
    val submit = {
        if (canSubmit) {
            haptic.tap()
            onLogin(email, password)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, bottom = 24.dp)
            .testTag(MensaTestTags.LOGIN_PAGE),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        LoginPlates()
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            enabled = !submitting,
            singleLine = true,
            label = { Text(stringResource(R.string.mensa_login_email)) },
            leadingIcon = { Icon(Icons.Outlined.AlternateEmail, contentDescription = null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentType = ContentType.Username + ContentType.EmailAddress },
        )
        PasswordTextField(
            value = password,
            onValueChange = { password = it },
            enabled = !submitting,
            labelRes = R.string.mensa_login_password,
            leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
            imeAction = ImeAction.Done,
            onImeAction = submit,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(4.dp))
        Button(
            onClick = submit,
            enabled = canSubmit,
            shapes = ButtonDefaults.shapes(),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = ButtonDefaults.MediumContainerHeight),
            contentPadding = ButtonDefaults.MediumContentPadding,
            colors = ButtonDefaults.buttonColors(containerColor = brandContainer, contentColor = brandContent),
        ) {
            if (submitting) {
                LoadingIndicator(modifier = Modifier.size(32.dp), color = brandContent)
            } else {
                Text(
                    text = stringResource(R.string.mensa_login_submit),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f))
            Text(
                text = stringResource(R.string.mensa_login_or),
                style = MaterialTheme.typography.labelMedium,
                color = scheme.onSurfaceVariant,
            )
            HorizontalDivider(modifier = Modifier.weight(1f))
        }
        OutlinedButton(
            onClick = { haptic.tap(); onCreateAccount() },
            enabled = !submitting,
            shapes = ButtonDefaults.shapes(),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = ButtonDefaults.MediumContainerHeight),
            contentPadding = ButtonDefaults.MediumContentPadding,
        ) {
            Icon(Icons.Outlined.PersonAddAlt, contentDescription = null, modifier = Modifier.size(20.dp))
            Text(
                text = stringResource(R.string.mensa_login_new_account),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

/** Three plates spread above the form, the same art as the hub's menu card. */
@Composable
private fun LoginPlates() {
    Box(modifier = Modifier.fillMaxWidth().height(112.dp), contentAlignment = Alignment.Center) {
        Row(horizontalArrangement = Arrangement.spacedBy((-20).dp)) {
            listOf(MensaArt.SALAD, MensaArt.PASTA_TOMATO, MensaArt.DESSERT).forEachIndexed { index, art ->
                MensaPlate(
                    art = art,
                    size = if (index == 1) 100.dp else 84.dp,
                    modifier = Modifier.offset(y = if (index == 1) 0.dp else 12.dp),
                )
            }
        }
    }
}

@Preview(name = "Sign in", widthDp = MENSA_PREVIEW_WIDTH, heightDp = 640)
@Composable
private fun LoginPreview(@PreviewParameter(MensaPreviewThemeProvider::class) theme: MensaPreviewTheme) {
    MensaPreviewHost(theme) {
        MensaLoginPage(
            submitting = false,
            onLogin = { _, _ -> },
            onCreateAccount = {},
        )
    }
}

@Preview(name = "Sign in, after sign-up", widthDp = MENSA_PREVIEW_WIDTH, heightDp = 640)
@Composable
private fun LoginPrefilledPreview(@PreviewParameter(MensaPreviewThemeProvider::class) theme: MensaPreviewTheme) {
    MensaPreviewHost(theme) {
        MensaLoginPage(
            submitting = false,
            onLogin = { _, _ -> },
            onCreateAccount = {},
            initialEmail = "nome.cognome@campus.unimib.it",
        )
    }
}
