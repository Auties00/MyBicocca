package it.attendance100.mybicocca.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.ui.screen.account.CareerPickerScreen
import it.attendance100.mybicocca.ui.screen.auth.AuthScreen

@Composable
fun AppRoot(viewModel: RootViewModel = hiltViewModel()) {
    val phase by viewModel.phase.collectAsStateWithLifecycle()
    when (val current = phase) {
        RootPhase.Loading -> LoadingScreen()
        RootPhase.Authenticating -> AuthScreen(
            onSignedIn = { account, requiresPick ->
                viewModel.onSignedIn(account.id, requiresPick)
            },
        )
        is RootPhase.NeedsCareerPick -> CareerPickerScreen(
            account = current.account,
            onPicked = { careerId ->
                viewModel.onCareerPicked(current.account.id, careerId)
            },
        )
        is RootPhase.AddingAccount -> AuthScreen(
            onSignedIn = { account, requiresPick ->
                viewModel.onSignedIn(account.id, requiresPick)
            },
            onCancel = { viewModel.cancelAddAccount() },
        )
        is RootPhase.SignedIn -> MainShell()
    }
}

@Composable
private fun LoadingScreen() {
    Surface(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}
