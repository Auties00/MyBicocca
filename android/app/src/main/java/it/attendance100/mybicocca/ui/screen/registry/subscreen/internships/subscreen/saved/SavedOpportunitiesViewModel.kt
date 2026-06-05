package it.attendance100.mybicocca.ui.screen.registry.subscreen.internships.subscreen.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.domain.model.internship.SavedOpportunity
import it.attendance100.mybicocca.domain.usecase.internship.ObserveSavedOpportunitiesUseCase
import it.attendance100.mybicocca.domain.usecase.internship.RemoveSavedOpportunityUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SavedOpportunitiesViewModel @Inject constructor(
    observeSaved: ObserveSavedOpportunitiesUseCase,
    private val removeSaved: RemoveSavedOpportunityUseCase,
) : ViewModel() {

    val saved: StateFlow<Loadable<List<SavedOpportunity>>> = observeSaved()
        .map<List<SavedOpportunity>, Loadable<List<SavedOpportunity>>> { Loadable.Loaded(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Loadable.NotYetLoaded)

    fun remove(id: String) {
        viewModelScope.launch { runCatching { removeSaved(id) } }
    }
}
