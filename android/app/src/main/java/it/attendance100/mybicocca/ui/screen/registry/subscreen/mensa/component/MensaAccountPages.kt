package it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.component

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.runtime.Composable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.AlternateEmail
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.domain.model.mensa.MensaSession
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.MensaTestTags
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.state.MensaAccountField
import it.attendance100.mybicocca.ui.theme.LocalDarkTheme

/** Account overview: who is signed in, the editable details, and sign-out. */
@Composable
internal fun MensaAccountPage(
    session: MensaSession,
    onEdit: (MensaAccountField) -> Unit,
    onLogout: () -> Unit,
    onDeleteAccount: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val haptic = rememberHapticManager()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 760.dp)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp)
            .testTag(MensaTestTags.ACCOUNT_PAGE),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(shape = RoundedCornerShape(24.dp), color = scheme.surfaceContainerHigh) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Surface(shape = CircleShape, color = scheme.primaryContainer, modifier = Modifier.size(56.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.Person, contentDescription = null, tint = scheme.onPrimaryContainer, modifier = Modifier.size(28.dp))
                    }
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = session.userName ?: session.email,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = session.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    session.barcode?.let {
                        Text(text = it, style = MaterialTheme.typography.labelMedium, color = scheme.onSurfaceVariant)
                    }
                }
            }
        }

        AccountRow(Icons.Outlined.Person, stringResource(R.string.mensa_account_edit_profile), session.phone ?: session.userName.orEmpty()) {
            onEdit(MensaAccountField.PROFILE)
        }
        AccountRow(Icons.Outlined.AlternateEmail, stringResource(R.string.mensa_account_change_email), session.email) {
            onEdit(MensaAccountField.EMAIL)
        }
        AccountRow(Icons.Outlined.Lock, stringResource(R.string.mensa_account_change_password), "••••••••") {
            onEdit(MensaAccountField.PASSWORD)
        }

        OutlinedButton(
            onClick = { haptic.tap(); onLogout() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
        ) {
            Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.size(8.dp))
            Text(stringResource(R.string.mensa_logout_action))
        }

        TextButton(
            onClick = { haptic.tap(); onDeleteAccount() },
            modifier = Modifier.align(Alignment.CenterHorizontally),
        ) {
            Text(text = stringResource(R.string.mensa_delete_action), color = scheme.error)
        }
    }
}

@Composable
private fun AccountRow(icon: ImageVector, title: String, value: String, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val haptic = rememberHapticManager()
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = scheme.surfaceContainer,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { haptic.tap(); onClick() }
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(icon, contentDescription = null, tint = scheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                if (value.isNotBlank()) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = scheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        }
    }
}

/** Form for one account detail: name and phone, e-mail, or password. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun MensaAccountEditPage(
    field: MensaAccountField,
    session: MensaSession,
    busy: Boolean,
    onSaveProfile: (name: String, phone: String) -> Unit,
    onSaveEmail: (String) -> Unit,
    onSavePassword: (oldPassword: String, newPassword: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val dark = LocalDarkTheme.current
    val haptic = rememberHapticManager()
    val brandContainer = if (dark) scheme.primaryContainer else scheme.primary
    val brandContent = if (dark) scheme.onPrimaryContainer else scheme.onPrimary

    var name by rememberSaveable { mutableStateOf(session.userName.orEmpty()) }
    var phone by rememberSaveable { mutableStateOf(session.phone.orEmpty()) }
    var email by rememberSaveable { mutableStateOf(session.email) }
    var oldPassword by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var repeatPassword by rememberSaveable { mutableStateOf("") }
    var showPasswords by rememberSaveable { mutableStateOf(false) }
    val passwordsMatch = newPassword == repeatPassword

    val canSave = !busy && when (field) {
        MensaAccountField.PROFILE -> name.isNotBlank()
        MensaAccountField.EMAIL -> email.isNotBlank() && email.trim() != session.email
        MensaAccountField.PASSWORD -> oldPassword.isNotBlank() && newPassword.isNotBlank() && passwordsMatch
    }

    Column(modifier = modifier.fillMaxWidth().heightIn(max = 760.dp)) {
        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            when (field) {
                MensaAccountField.PROFILE -> {
                    FormField(name, { name = it }, stringResource(R.string.mensa_account_name), Icons.Outlined.Person, !busy)
                    FormField(phone, { phone = it }, stringResource(R.string.mensa_account_phone), Icons.Outlined.Phone, !busy, KeyboardType.Phone)
                }

                MensaAccountField.EMAIL -> {
                    FormField(email, { email = it }, stringResource(R.string.mensa_login_email), Icons.Outlined.AlternateEmail, !busy, KeyboardType.Email)
                    Text(
                        text = stringResource(R.string.mensa_account_email_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                    )
                }

                MensaAccountField.PASSWORD -> {
                    val transformation = if (showPasswords) VisualTransformation.None else PasswordVisualTransformation()
                    val toggle: @Composable () -> Unit = {
                        IconButton(onClick = { showPasswords = !showPasswords }) {
                            Icon(if (showPasswords) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility, contentDescription = null)
                        }
                    }
                    FormField(oldPassword, { oldPassword = it }, stringResource(R.string.mensa_account_old_password), Icons.Outlined.Lock, !busy, KeyboardType.Password, transformation, toggle)
                    FormField(newPassword, { newPassword = it }, stringResource(R.string.mensa_account_new_password), Icons.Outlined.Lock, !busy, KeyboardType.Password, transformation)
                    FormField(
                        repeatPassword, { repeatPassword = it }, stringResource(R.string.mensa_account_repeat_password),
                        Icons.Outlined.Lock, !busy, KeyboardType.Password, transformation,
                        isError = repeatPassword.isNotEmpty() && !passwordsMatch,
                    )
                    if (repeatPassword.isNotEmpty() && !passwordsMatch) {
                        Text(
                            text = stringResource(R.string.mensa_account_password_mismatch),
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.error,
                        )
                    }
                }
            }
            Spacer(Modifier.size(4.dp))
        }

        Button(
            onClick = {
                haptic.tap()
                when (field) {
                    MensaAccountField.PROFILE -> onSaveProfile(name, phone)
                    MensaAccountField.EMAIL -> onSaveEmail(email)
                    MensaAccountField.PASSWORD -> onSavePassword(oldPassword, newPassword)
                }
            },
            enabled = canSave,
            shapes = ButtonDefaults.shapes(),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = ButtonDefaults.MediumContainerHeight)
                .padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 24.dp),
            contentPadding = ButtonDefaults.MediumContentPadding,
            colors = ButtonDefaults.buttonColors(containerColor = brandContainer, contentColor = brandContent),
        ) {
            if (busy) {
                LoadingIndicator(modifier = Modifier.size(32.dp), color = brandContent)
            } else {
                Text(
                    text = stringResource(R.string.mensa_account_save),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun FormField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    enabled: Boolean,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: (@Composable () -> Unit)? = null,
    isError: Boolean = false,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        singleLine = true,
        isError = isError,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null) },
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
    )
}

@Preview(name = "Account", widthDp = MENSA_PREVIEW_WIDTH, heightDp = 560)
@Composable
private fun AccountPreview(@PreviewParameter(MensaPreviewThemeProvider::class) theme: MensaPreviewTheme) {
    MensaPreviewHost(theme) {
        MensaAccountPage(
            session = MensaPreviewData.session,
            onEdit = {},
            onLogout = {},
            onDeleteAccount = {},
        )
    }
}
