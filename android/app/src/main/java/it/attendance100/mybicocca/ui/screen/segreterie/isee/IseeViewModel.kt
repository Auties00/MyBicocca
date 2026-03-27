package it.attendance100.mybicocca.ui.screen.segreterie.isee

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.data.model.isee.IseeDeclaration
import it.attendance100.mybicocca.data.repository.TaxRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IseeViewModel @Inject constructor(
    private val taxRepository: TaxRepository,
) : ViewModel() {

    val iseeDeclaration: StateFlow<IseeDeclaration?> = taxRepository.observeLatestIsee()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            taxRepository.refreshIsee(0)
        }
    }
}
