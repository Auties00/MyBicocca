package it.attendance100.mybicocca.ui.screen.registry.subscreen.procedures.subscreen.courseChange

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.procedures.state.ProcedureEvent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

@HiltViewModel
class CourseChangeViewModel @Inject constructor() : ViewModel() {

    private val _events = Channel<ProcedureEvent>(Channel.BUFFERED)
    val events: Flow<ProcedureEvent> = _events.receiveAsFlow()

    // No REST service exists for the passaggio wizard and the scrape entrypoint isn't
    // modelled yet (Esse3LegacyApi.submitCourseChange is a stub). UI is in place; for now
    // it reports the feature is coming.
    fun submit() {
        // TODO: route through a ProcedureRepository → Esse3LegacyApi.submitCourseChange once implemented.
        _events.trySend(ProcedureEvent.ShowMessage("Questa funzione sarà disponibile a breve."))
    }
}
