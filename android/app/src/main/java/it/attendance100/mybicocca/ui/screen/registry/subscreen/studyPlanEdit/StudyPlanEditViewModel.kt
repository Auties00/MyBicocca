package it.attendance100.mybicocca.ui.screen.registry.subscreen.studyPlanEdit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.studyplan.EditableRule
import it.attendance100.mybicocca.domain.usecase.studyplan.GetStudyPlanDraftUseCase
import it.attendance100.mybicocca.domain.usecase.studyplan.SubmitStudyPlanUseCase
import it.attendance100.mybicocca.ui.navigation.AppRoute
import it.attendance100.mybicocca.ui.screen.registry.subscreen.studyPlanEdit.state.StudyPlanEditEvent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex

// Plan-edit wizard: the schema's rules grouped by course year, one step per year. The
// rules list is the single piece of edited state; year navigation is plain user input.
@HiltViewModel(assistedFactory = StudyPlanEditViewModel.Factory::class)
class StudyPlanEditViewModel @AssistedInject constructor(
    @Assisted private val key: AppRoute.StudyPlanEdit,
    private val getStudyPlanDraft: GetStudyPlanDraftUseCase,
    private val submitStudyPlan: SubmitStudyPlanUseCase,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(key: AppRoute.StudyPlanEdit): StudyPlanEditViewModel
    }

    // CareerId and Esse3 stuId are the same identifier.
    private val careerId = CareerId(key.studentId)

    private val _rules = MutableStateFlow<Loadable<List<EditableRule>>>(Loadable.NotYetLoaded)
    val rules: StateFlow<Loadable<List<EditableRule>>> = _rules.asStateFlow()

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    // 0 until the draft loads, then the first real year (rules with courseYear == 0 are
    // shown on every step).
    private val _currentYear = MutableStateFlow(0)
    val currentYear: StateFlow<Int> = _currentYear.asStateFlow()

    private val _submitting = MutableStateFlow(false)
    val submitting: StateFlow<Boolean> = _submitting.asStateFlow()

    // Submit failures stay visible in the bottom bar until the next attempt or edit.
    private val _submitError = MutableStateFlow<String?>(null)
    val submitError: StateFlow<String?> = _submitError.asStateFlow()

    private val _events = Channel<StudyPlanEditEvent>(Channel.BUFFERED)
    val events: Flow<StudyPlanEditEvent> = _events.receiveAsFlow()

    private val loadMutex = Mutex()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            if (!loadMutex.tryLock()) return@launch
            try {
                _syncStatus.value = SyncStatus.Refreshing
                runCatching {
                    getStudyPlanDraft(
                        careerId = careerId,
                        planId = key.planId.takeIf { it > 0 },
                        choiceRegulationId = key.choiceRegulationId,
                        schemaId = key.schemaId,
                    )
                }.fold(
                    onSuccess = { rules ->
                        _rules.value = Loadable.Loaded(rules)
                        _syncStatus.value = SyncStatus.Idle
                        if (_currentYear.value == 0) {
                            _currentYear.value = rules.years().firstOrNull() ?: 0
                        }
                    },
                    onFailure = { cause -> _syncStatus.value = SyncStatus.Failed(cause) },
                )
            } finally {
                loadMutex.unlock()
            }
        }
    }

    fun toggleCourse(ruleChoiceId: Long, courseChoiceId: Long) {
        _submitError.value = null
        val current = _rules.value.valueOrNull() ?: return
        _rules.value = Loadable.Loaded(
            current.map { rule ->
                if (rule.choiceId != ruleChoiceId) return@map rule
                rule.copy(
                    courses = rule.courses.map inner@{ course ->
                        if (course.choiceId != courseChoiceId) return@inner course
                        if (course.isMandatory) return@inner course
                        if (!course.isSelected) {
                            // The same activity can appear under several rules — block a
                            // second selection, and respect the rule's credit cap.
                            val selectedElsewhere = current.any { other ->
                                other.choiceId != ruleChoiceId &&
                                    other.courses.any { it.code == course.code && it.isSelected }
                            }
                            if (selectedElsewhere || !rule.isCourseSelectable(course)) return@inner course
                        }
                        course.copy(isSelected = !course.isSelected)
                    },
                )
            },
        )
    }

    fun setYear(year: Int) {
        _currentYear.value = year
    }

    fun previousYear() {
        val years = _rules.value.valueOrNull()?.years() ?: return
        val index = years.indexOf(_currentYear.value)
        if (index > 0) _currentYear.value = years[index - 1]
    }

    fun nextYear() {
        val years = _rules.value.valueOrNull()?.years() ?: return
        val index = years.indexOf(_currentYear.value)
        if (index >= 0 && index < years.lastIndex) _currentYear.value = years[index + 1]
    }

    fun submit() {
        val rules = _rules.value.valueOrNull() ?: return
        if (_submitting.value) return
        viewModelScope.launch {
            _submitting.value = true
            _submitError.value = null
            runCatching { submitStudyPlan(careerId, rules) }.fold(
                onSuccess = { _events.trySend(StudyPlanEditEvent.Submitted) },
                onFailure = { cause -> _submitError.value = cause.submitFriendlyMessage() },
            )
            _submitting.value = false
        }
    }

    fun hasChanges(): Boolean =
        _rules.value.valueOrNull().orEmpty().any { rule ->
            rule.courses.any { it.isSelected != it.isInitialSelected }
        }
}

fun List<EditableRule>.years(): List<Int> =
    map { it.courseYear }.filter { it > 0 }.distinct().sorted()

private fun Throwable.submitFriendlyMessage(): String {
    val raw = message ?: "Errore nell'invio del piano."
    return when {
        raw.contains("Security failed", ignoreCase = true) ||
            raw.contains("profilo", ignoreCase = true) ->
            "La modifica del piano non è attualmente consentita dal tuo profilo."

        raw.contains("403") -> "Accesso negato: impossibile inviare il piano di studi."
        else -> raw
    }
}
