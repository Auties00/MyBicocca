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

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            val studentId = activeCareer.value?.studentId ?: 0L
            studyPlanRepository.refreshHeaders(studentId)
            // After headers are refreshed, load courses for the first plan
            val planId = headers.value.firstOrNull()?.id
            if (planId != null) {
                studyPlanRepository.refreshCourses(studentId, planId)
            }
            _isRefreshing.value = false
        }
    }
}
