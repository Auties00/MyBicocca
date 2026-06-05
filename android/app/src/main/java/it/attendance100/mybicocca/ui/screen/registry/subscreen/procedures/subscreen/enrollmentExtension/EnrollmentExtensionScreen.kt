package it.attendance100.mybicocca.ui.screen.registry.subscreen.procedures.subscreen.enrollmentExtension

import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreTime
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import it.attendance100.mybicocca.ui.component.feedback.LocalAppSnackbarController
import it.attendance100.mybicocca.ui.screen.registry.subscreen.procedures.component.ProcedureInfoContent
import it.attendance100.mybicocca.ui.screen.registry.subscreen.procedures.state.ProcedureEvent
import kotlinx.coroutines.flow.collectLatest

private const val ESSE3_STUDENT_AREA = "https://s3w.si.unimib.it/auth/studente/Home.do"

@Composable
fun EnrollmentExtensionScreen(
    viewModel: EnrollmentExtensionViewModel = hiltViewModel(),
) {
    val snackbar = LocalAppSnackbarController.current
    val context = LocalContext.current
    val openPortal = remember(context) {
        { CustomTabsIntent.Builder().setShowTitle(true).build().launchUrl(context, ESSE3_STUDENT_AREA.toUri()) }
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is ProcedureEvent.ShowMessage -> snackbar.showInfo(event.message)
            }
        }
    }

    ProcedureInfoContent(
        icon = Icons.Outlined.MoreTime,
        headline = "Proroga iscrizione",
        description = "Richiedi la proroga dei termini di iscrizione all'anno accademico, " +
            "ad esempio per completare l'immatricolazione oltre la scadenza ordinaria.",
        steps = listOf(
            "Compila la richiesta di proroga",
            "Allega l'eventuale documentazione richiesta",
            "Attendi l'esito dalla segreteria",
        ),
        note = "La proroga può comportare il pagamento di una mora.",
        primaryLabel = "Richiedi proroga",
        onPrimary = viewModel::submit,
        externalLabel = "Apri nel portale Esse3",
        onExternal = openPortal,
    )
}
