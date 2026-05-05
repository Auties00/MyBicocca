package it.attendance100.mybicocca.ui.screen.segreterie.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.data.model.exam.ExamCall
import it.attendance100.mybicocca.data.repository.CareerRepository
import it.attendance100.mybicocca.data.repository.ExamRepository
import it.attendance100.mybicocca.util.NetworkMonitor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

@HiltViewModel
class BookingViewModel @Inject constructor(
    private val examRepository: ExamRepository,
    private val careerRepository: CareerRepository,
    networkMonitor: NetworkMonitor,
) : ViewModel() {

    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline

    val examCalls: StateFlow<List<ExamCall>> = examRepository.observeExamCalls()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _selectedExamCall = MutableStateFlow<ExamCall?>(null)
    val selectedExamCall: StateFlow<ExamCall?> = _selectedExamCall.asStateFlow()

    private val _selectedExamError = MutableStateFlow<String?>(null)
    val selectedExamError: StateFlow<String?> = _selectedExamError.asStateFlow()

    private val _isLoadingDetail = MutableStateFlow(false)
    val isLoadingDetail: StateFlow<Boolean> = _isLoadingDetail.asStateFlow()

    init {
        refresh()
    }

    fun clearError() {
        // TODO: maybe implement
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _error.value = null
            try {
                val career = withTimeout(15_000) {
                    careerRepository.observeAll()
                        .first { it.isNotEmpty() }
                        .first()
                }
                examRepository.refreshExamCalls(
                    careerId = career.studentId,
                    matricolaId = career.matricolaId,
                )
            } catch (e: Exception) {
                _error.value = e.message ?: "Errore durante il caricamento"
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun loadExamCallById(id: Long) {
        viewModelScope.launch {
            _isLoadingDetail.value = true
            _selectedExamError.value = null
            try {
                val call = examRepository.getExamCallById(id)
                if (call != null) {
                    _selectedExamCall.value = call
                } else {
                    _selectedExamError.value = "Impossibile caricare i dettagli dell'appello"
                }
            } catch (e: Exception) {
                _selectedExamError.value = e.message ?: "Errore sconosciuto"
            } finally {
                _isLoadingDetail.value = false
            }
        }
    }
}
