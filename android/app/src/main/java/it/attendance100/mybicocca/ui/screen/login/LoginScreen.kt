package it.attendance100.mybicocca.ui.screen.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.ui.component.SquircleAnimatedButton
import it.attendance100.mybicocca.ui.component.appbar.AppTitle

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.loginSuccess) {
        if (uiState.loginSuccess) onLoginSuccess()
    }

    Scaffold { padding ->
        LoginContent(
            modifier = Modifier.padding(padding),
            isLoading = uiState.isLoading,
            onLogin = viewModel::login,
            usernameOrEmail = viewModel.email,
            updateState = { input -> viewModel.updateEmail(input) },
            validatorHasErrors = viewModel.emailHasErrors,
            errorMessage = uiState.error,
            onInputChange = viewModel::clearError,
        )
    }
}

@Composable
private fun LoginContent(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    onLogin: (String) -> Unit,
    usernameOrEmail: String = "",
    updateState: (String) -> Unit,
    validatorHasErrors: Boolean,
    errorMessage: String?,
    onInputChange: () -> Unit,
) {
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val canSubmit =
        !isLoading && usernameOrEmail.isNotBlank() && password.isNotBlank() && !validatorHasErrors

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .padding(bottom = 96.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AppTitle(height = 64.dp, modifier = Modifier.width(160.dp))

        Spacer(Modifier.height(64.dp))

        OutlinedTextField(
            value = usernameOrEmail,
            onValueChange = updateState,
            label = { Text("Username") },
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentType = ContentType.EmailAddress },
            singleLine = true,
            isError = validatorHasErrors,
            keyboardActions = KeyboardActions(
                onNext = {
                    focusManager.moveFocus(FocusDirection.Down)
                },
                onDone = {
                    if (canSubmit) {
                        focusManager.clearFocus()
                        onLogin(password)
                    }
                },
            ),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                )
            },
            supportingText = { if (validatorHasErrors) Text("Incorrect email format.") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
            ),
        )


        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                onInputChange()
            },
            label = { Text("Password") },
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentType = ContentType.Password },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (canSubmit) {
                        focusManager.clearFocus()
                        onLogin(password)
                    }
                },
            ),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Password,
                    contentDescription = null,
                )
            },
            trailingIcon = {
                val image =
                    if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff

                val description = if (passwordVisible) "Hide password" else "Show password"

                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, description)
                }
            }
        )

        Spacer(Modifier.height(24.dp))

        SquircleAnimatedButton(
            onClick = {
                if (canSubmit) {
                    focusManager.clearFocus()
                    onLogin(password)
                }
            },
            enabled = canSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 3.dp,
                )
            } else {
                Text("Login")
            }
        }

        if (errorMessage != null) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
