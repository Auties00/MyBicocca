package it.attendance100.mybicocca.ui.screen.segreterie.taxes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.data.model.tax.TaxCharge
import it.attendance100.mybicocca.data.repository.TaxRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class TaxDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    taxRepository: TaxRepository,
) : ViewModel() {

    private val chargeId: Long = savedStateHandle["chargeId"] ?: 0L

    val charge: StateFlow<TaxCharge?> = taxRepository.observeCharges(0)
        .map { charges -> charges.firstOrNull { it.id == chargeId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
