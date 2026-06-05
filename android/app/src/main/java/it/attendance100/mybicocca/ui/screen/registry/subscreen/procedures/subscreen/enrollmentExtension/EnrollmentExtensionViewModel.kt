package it.attendance100.mybicocca.ui.screen.registry.subscreen.procedures.subscreen.enrollmentExtension

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.procedures.state.ProcedureEvent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

@HiltViewModel
class EnrollmentExtensionViewModel @Inject constructor() : ViewModel() {

    private val _events = Channel<ProcedureEvent>(Channel.BUFFERED)
    val events: Flow<ProcedureEvent> = _events.receiveAsFlow()

    // Proroga has no REST endpoint and its scrape page was observed empty, so the request
    // markup isn't modelled yet (Esse3LegacyApi.submitEnrollmentExtension is a stub). UI is
    // in place; for now it reports the feature is coming.
    fun submit() {
        // TODO: route through a ProcedureRepository → Esse3LegacyApi.submitEnrollmentExtension once implemented.
        _events.trySend(ProcedureEvent.ShowMessage("Questa funzione sarà disponibile a breve."))
    }
}
