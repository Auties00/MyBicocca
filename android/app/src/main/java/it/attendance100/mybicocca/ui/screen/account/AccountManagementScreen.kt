package it.attendance100.mybicocca.ui.screen.account

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.career.isSelectable

@Composable
fun AccountManagementScreen(
    accountId: AccountId,
    onSignedOut: () -> Unit,
    viewModel: AccountViewModel = hiltViewModel(),
) {
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val selectedId by viewModel.selectedCareerId.collectAsStateWithLifecycle()
    val account = accounts.firstOrNull { it.id == accountId }

    if (account == null) {
        // Account was signed out from elsewhere (or hasn't loaded yet on first frame).
        // Pop back so we don't sit on a stale screen.
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Account non disponibile.")
        }
        return
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            item {
                Text(account.displayName, style = MaterialTheme.typography.headlineSmall)
                Text(
                    account.username,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                account.academic.fiscalCode?.let {
                    Text(
                        "Codice fiscale: $it",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.height(24.dp))
                Text("Careers", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
            }
            items(account.academic.careers, key = { it.id.value }) { career ->
                val onClick: (() -> Unit)? = if (career.status.isSelectable) {
                    { viewModel.switchCareer(account.id, career.id) }
                } else null
                CareerCard(
                    career = career,
                    selected = career.id == selectedId,
                    onClick = onClick,
                )
                Spacer(Modifier.height(8.dp))
            }
            item {
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = {
                        viewModel.signOut(account.id)
                        onSignedOut()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Sign out")
                }
            }
        }
    }
}
