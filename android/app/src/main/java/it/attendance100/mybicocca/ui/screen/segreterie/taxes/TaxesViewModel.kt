package it.attendance100.mybicocca.ui.screen.segreterie.taxes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.data.model.tax.TaxCharge
import it.attendance100.mybicocca.data.repository.CareerRepository
import it.attendance100.mybicocca.data.repository.TaxRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TaxesViewModel @Inject constructor(
    private val taxRepository: TaxRepository,
    private val careerRepository: CareerRepository,
) : ViewModel() {

    private val activeCareer = careerRepository.observeAll()
        .map { it.firstOrNull() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val charges: StateFlow<List<TaxCharge>> = activeCareer
        .flatMapLatest { career -> taxRepository.observeCharges(career?.studentId ?: 0) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            val studentId = activeCareer.value?.studentId ?: 0L
            taxRepository.refreshCharges(studentId, studentId)
            _isRefreshing.value = false
        }
    }
}
