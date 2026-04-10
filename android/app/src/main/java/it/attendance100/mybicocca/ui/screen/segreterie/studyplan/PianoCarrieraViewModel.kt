package it.attendance100.mybicocca.ui.screen.segreterie.studyplan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.data.model.studyplan.PlannedCourse
import it.attendance100.mybicocca.data.model.studyplan.StudyPlanHeader
import it.attendance100.mybicocca.data.repository.CareerRepository
import it.attendance100.mybicocca.data.repository.StudyPlanRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PianoCarrieraViewModel @Inject constructor(
    private val studyPlanRepository: StudyPlanRepository,
    private val careerRepository: CareerRepository,
) : ViewModel() {

    private val activeCareer = careerRepository.observeAll()
        .map { it.firstOrNull() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val headers: StateFlow<List<StudyPlanHeader>> = activeCareer
        .flatMapLatest { career -> studyPlanRepository.observeHeaders(career?.studentId ?: 0) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val courses: StateFlow<List<PlannedCourse>> = headers
        .flatMapLatest { hdrs ->
            val planId = hdrs.firstOrNull()?.id ?: return@flatMapLatest flowOf(emptyList())
            studyPlanRepository.observeCourses(planId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _isEditEnabled = MutableStateFlow(false)
    val isEditEnabled: StateFlow<Boolean> = _isEditEnabled.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            val studentId = activeCareer.filterNotNull().first().studentId
            studyPlanRepository.refreshHeaders(studentId)
            val header = studyPlanRepository.observeHeaders(studentId).first().firstOrNull()
            if (header?.id != null) {
                studyPlanRepository.refreshCourses(studentId, header.id)
            }
            _isRefreshing.value = false
            checkEditingWindow(header)
        }
    }

    private fun checkEditingWindow(preFetchedHeader: StudyPlanHeader? = null) {
        viewModelScope.launch {
            val header = preFetchedHeader ?: headers.value.firstOrNull() ?: return@launch
            val regId = header.choiceRegulationId ?: return@launch
            _isEditEnabled.value = runCatching {
                // TODO fix this, currently returns always true
                studyPlanRepository.isEditingWindowOpen(regId)
            }.getOrDefault(false)
        }
    }
}
