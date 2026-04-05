package it.attendance100.mybicocca.ui.screen.segreterie.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.data.model.exam.ExamBooking
import it.attendance100.mybicocca.data.repository.CareerRepository
import it.attendance100.mybicocca.data.repository.ExamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookedViewModel @Inject constructor(
    private val examRepository: ExamRepository,
    private val careerRepository: CareerRepository,
) : ViewModel() {

    val bookings: StateFlow<List<ExamBooking>> = examRepository.observeBookings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _error.value = null
            try {
                val career = careerRepository.observeAll()
                    .first { it.isNotEmpty() }
                    .first()
                examRepository.refreshBookings(career.matricolaId)
            } catch (e: Exception) {
                _error.value = e.message ?: "Errore durante il caricamento"
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}
