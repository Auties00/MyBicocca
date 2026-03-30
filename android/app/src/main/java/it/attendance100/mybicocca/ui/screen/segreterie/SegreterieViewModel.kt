package it.attendance100.mybicocca.ui.screen.segreterie

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.data.repository.CareerRepository
import it.attendance100.mybicocca.data.repository.ExamRepository
import it.attendance100.mybicocca.data.repository.TaxRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SegreterieViewModel @Inject constructor(
    taxRepository: TaxRepository,
    examRepository: ExamRepository,
    careerRepository: CareerRepository,
) : ViewModel() {

    private val activeCareer = careerRepository.observeAll()
        .map { it.firstOrNull() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val unpaidTaxCount: StateFlow<Int> = activeCareer
        .flatMapLatest { career ->
            taxRepository.observeCharges(career?.studentId ?: 0)
                .map { charges -> charges.count { it.status != "PAGATO" } }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val bookedExamCount: StateFlow<Int> = examRepository.observeBookings()
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)
}
