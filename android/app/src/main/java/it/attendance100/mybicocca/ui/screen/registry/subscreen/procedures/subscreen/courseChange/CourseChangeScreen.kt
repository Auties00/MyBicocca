package it.attendance100.mybicocca.ui.screen.registry.subscreen.procedures.subscreen.courseChange

import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AltRoute
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
fun CourseChangeScreen(
    viewModel: CourseChangeViewModel = hiltViewModel(),
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
        icon = Icons.Outlined.AltRoute,
        headline = "Cambio percorso",
        description = "Richiedi il passaggio a un altro corso di studi mantenendo la tua carriera. " +
            "La domanda viene valutata dalla segreteria del corso di destinazione.",
        steps = listOf(
            "Verifica i requisiti del corso di destinazione",
            "Presenta la domanda di passaggio",
            "Attendi la valutazione della segreteria",
            "Completa l'immatricolazione al nuovo corso",
        ),
        note = "Il passaggio è un'operazione delicata: una volta presentata, la domanda non è sempre annullabile.",
        primaryLabel = "Avvia domanda",
        onPrimary = viewModel::submit,
        externalLabel = "Apri nel portale Esse3",
        onExternal = openPortal,
    )
}
