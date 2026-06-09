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
import it.attendance100.mybicocca.domain.model.studyplan.PlanApprovalType
import it.attendance100.mybicocca.domain.model.studyplan.StudyPath
import it.attendance100.mybicocca.domain.model.studyplan.StudyPathOption
import it.attendance100.mybicocca.domain.usecase.studyplan.GetStudyPathUseCase
import it.attendance100.mybicocca.domain.usecase.studyplan.GetStudyPlanDraftUseCase
import it.attendance100.mybicocca.domain.usecase.studyplan.SubmitStudyPlanUseCase
import it.attendance100.mybicocca.ui.screen.registry.subscreen.studyPlanEdit.state.StudyPlanEditEvent
import it.attendance100.mybicocca.ui.screen.registry.subscreen.studyPlanEdit.state.StudyPlanEditRequest
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

// Percorso-first plan wizard: step zero picks the schema (= percorso/orientamento/
// profilo/part-time tuple) when the regulation offers any, then the schema's rules
// follow grouped by course year, one step per year. Edited drafts are cached per
// schema so flipping between percorsi never loses picks.
@HiltViewModel(assistedFactory = StudyPlanEditViewModel.Factory::class)
class StudyPlanEditViewModel @AssistedInject constructor(
    @Assisted private val key: StudyPlanEditRequest,
    private val getStudyPath: GetStudyPathUseCase,
    private val getStudyPlanDraft: GetStudyPlanDraftUseCase,
    private val submitStudyPlan: SubmitStudyPlanUseCase,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(key: StudyPlanEditRequest): StudyPlanEditViewModel
    }

    // CareerId and Esse3 stuId are the same identifier.
    private val careerId = CareerId(key.studentId)

    // The percorso alternatives backing step zero. Loaded(null) = lookup failed — the
    // wizard degrades to rules-only against the route's schema.
    private val _path = MutableStateFlow<Loadable<StudyPath?>>(Loadable.NotYetLoaded)
    val path: StateFlow<Loadable<StudyPath?>> = _path.asStateFlow()

    // The schema the rules are compiled against: the student's current one from the
    // route, or null when the career has no plan yet and nothing was picked.
    private val _selectedSchemaId = MutableStateFlow(key.schemaId.takeIf { it > 0 })
    val selectedSchemaId: StateFlow<Long?> = _selectedSchemaId.asStateFlow()

    private val _rules = MutableStateFlow<Loadable<List<EditableRule>>>(Loadable.NotYetLoaded)
    val rules: StateFlow<Loadable<List<EditableRule>>> = _rules.asStateFlow()

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    // The wizard segment in view: PATH_SEGMENT for the percorso step, otherwise the
    // choiceId of the single rule the page shows.
    private val _currentSegment = MutableStateFlow(PATH_SEGMENT)
    val currentSegment: StateFlow<Long> = _currentSegment.asStateFlow()

    private val _submitting = MutableStateFlow(false)
    val submitting: StateFlow<Boolean> = _submitting.asStateFlow()

    // Submit failures stay visible in the bottom bar until the next attempt or edit.
    private val _submitError = MutableStateFlow<String?>(null)
    val submitError: StateFlow<String?> = _submitError.asStateFlow()

    private val _events = Channel<StudyPlanEditEvent>(Channel.BUFFERED)
    val events: Flow<StudyPlanEditEvent> = _events.receiveAsFlow()

    // Edited drafts parked per schema so switching back restores the picks.
    private val draftCache = mutableMapOf<Long, List<EditableRule>>()
    private val loadMutex = Mutex()

    init {
        loadPath()
    }

    fun refresh() {
        loadPath()
    }

    // Discard an in-progress compilation. The VM is sheet-hosted and outlives the editor
    // page, so leaving without submitting must not replay old picks, segment, or parked
    // drafts on the next open.
    fun reset() {
        draftCache.clear()
        _rules.value = Loadable.NotYetLoaded
        _syncStatus.value = SyncStatus.Idle
        _submitError.value = null
        _currentSegment.value = PATH_SEGMENT
        _selectedSchemaId.value = key.schemaId.takeIf { it > 0 }
        loadPath()
    }

    private fun loadPath() {
        viewModelScope.launch {
            val path = runCatching { getStudyPath(careerId) }.getOrNull()
            _path.value = Loadable.Loaded(path)
            // Rules-only flow (no percorso step to defer behind): load right away.
            // With a percorso step the fetch waits for Avanti via loadSelectedRules.
            val options = path?.options.orEmpty()
            if (options.isEmpty()) {
                _selectedSchemaId.value?.let { loadDraft(it, useCache = false) }
            } else {
                // Picking the percorso is an explicit, mandatory act: nothing is
                // preselected (not even the student's current schema), so Avanti stays
                // disabled until a deliberate choice is made.
                _selectedSchemaId.value = null
            }
        }
    }

    fun selectSchema(schemaId: Long) {
        if (_submitting.value) return
        val previous = _selectedSchemaId.value
        if (previous == schemaId) return
        // Park the edits of the schema being left so they survive coming back.
        if (previous != null) {
            _rules.value.valueOrNull()?.let { draftCache[previous] = it }
        }
        _submitError.value = null
        _selectedSchemaId.value = schemaId
        // The fetch is deferred to Avanti; only a parked draft restores instantly.
        val cached = draftCache[schemaId]
        _rules.value = if (cached != null) Loadable.Loaded(cached) else Loadable.NotYetLoaded
        _syncStatus.value = SyncStatus.Idle
    }

    // The deferred half of selectSchema: the percorso page calls this when the user
    // advances, so picks that never leave the page never hit the network.
    fun loadSelectedRules() {
        val schemaId = _selectedSchemaId.value ?: return
        loadDraft(schemaId, useCache = true)
    }

    private fun loadDraft(schemaId: Long, useCache: Boolean) {
        if (useCache) {
            draftCache[schemaId]?.let {
                _rules.value = Loadable.Loaded(it)
                _syncStatus.value = SyncStatus.Idle
                return
            }
        }
        viewModelScope.launch {
            loadMutex.withLock {
                // A queued load can be stale (schema re-picked meanwhile) or already
                // satisfied by the load that held the lock before it.
                if (_selectedSchemaId.value != schemaId) return@withLock
                if (useCache && draftCache.containsKey(schemaId)) {
                    _rules.value = Loadable.Loaded(draftCache.getValue(schemaId))
                    _syncStatus.value = SyncStatus.Idle
                    return@withLock
                }
                _rules.value = Loadable.NotYetLoaded
                _syncStatus.value = SyncStatus.Refreshing
                runCatching {
                    getStudyPlanDraft(
                        careerId = careerId,
                        planId = key.planId.takeIf { it > 0 },
                        choiceRegulationId = key.choiceRegulationId,
                        schemaId = schemaId,
                    )
                }.fold(
                    onSuccess = { rules ->
                        // The draft may be for a stale schema if the user re-picked while
                        // loading; only publish when still selected.
                        if (_selectedSchemaId.value == schemaId) {
                            _rules.value = Loadable.Loaded(rules)
                            _syncStatus.value = SyncStatus.Idle
                        }
                        draftCache[schemaId] = rules
                    },
                    onFailure = { cause ->
                        if (_selectedSchemaId.value == schemaId) {
                            _syncStatus.value = SyncStatus.Failed(cause)
                        }
                    },
                )
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

    fun setSegment(segment: Long) {
        _currentSegment.value = segment
    }

    fun submit() {
        val rules = _rules.value.valueOrNull() ?: return
        if (rules.isEmpty() || _submitting.value) return
        val chosen = chosenOption()
        viewModelScope.launch {
            _submitting.value = true
            _submitError.value = null
            runCatching { submitStudyPlan(careerId, rules, chosen) }.fold(
                onSuccess = {
                    _events.trySend(StudyPlanEditEvent.Submitted(submittedMessage(chosen)))
                    // The VM is sheet-hosted and outlives the editor page: drop the
                    // now-submitted drafts so a later session starts from the server's
                    // fresh plan instead of replaying stale picks as pending changes.
                    draftCache.clear()
                    _rules.value = Loadable.NotYetLoaded
                    _currentSegment.value = PATH_SEGMENT
                    loadPath()
                },
                onFailure = { cause -> _submitError.value = cause.submitFriendlyMessage() },
            )
            _submitting.value = false
        }
    }

    fun hasChanges(): Boolean {
        val path = _path.value.valueOrNull()
        val schemaChanged = path?.currentSchemaId != null &&
            _selectedSchemaId.value != null &&
            _selectedSchemaId.value != path.currentSchemaId
        return schemaChanged || _rules.value.valueOrNull().orEmpty().any { rule ->
            rule.courses.any { it.isSelected != it.isInitialSelected }
        }
    }

    private fun chosenOption(): StudyPathOption? {
        val schemaId = _selectedSchemaId.value ?: return null
        return _path.value.valueOrNull()?.options?.firstOrNull { it.schemaId == schemaId }
    }

    // What the student should expect next depends on the schema's approval flavour.
    private fun submittedMessage(chosen: StudyPathOption?): String = when (chosen?.approval) {
        PlanApprovalType.Automatic -> "Piano inviato: l'approvazione è automatica."
        PlanApprovalType.AutomaticIfCompliant ->
            "Piano inviato: verrà approvato automaticamente se conforme al regolamento."
        PlanApprovalType.Manual -> "Piano inviato: in attesa di approvazione."
        else -> "Piano di studi inviato"
    }

    companion object {
        // Sentinel segment for the percorso step; rule choiceIds are always positive.
        const val PATH_SEGMENT = -1L
    }
}

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
