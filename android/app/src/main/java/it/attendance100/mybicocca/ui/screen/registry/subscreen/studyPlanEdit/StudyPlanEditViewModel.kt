package it.attendance100.mybicocca.ui.screen.registry.subscreen.studyPlanEdit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.core.text.UiText
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.studyplan.EditableRule
import it.attendance100.mybicocca.domain.model.studyplan.PlanApprovalType
import it.attendance100.mybicocca.domain.model.studyplan.StudyPath
import it.attendance100.mybicocca.domain.model.studyplan.StudyPathOption
import it.attendance100.mybicocca.domain.usecase.studyplan.GetStudyPathUseCase
import it.attendance100.mybicocca.domain.usecase.studyplan.GetStudyPlanDraftUseCase
import it.attendance100.mybicocca.domain.usecase.studyplan.SubmitStudyPlanUseCase
import it.attendance100.mybicocca.ui.screen.registry.subscreen.studyPlanEdit.StudyPlanEditViewModel.Companion.PATH_SEGMENT
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

/**
 * Backs the percorso-first plan wizard: step zero picks the schema (= percorso/
 * orientamento/profilo/part-time tuple) when the regulation offers any, then the
 * schema's rules follow grouped by course year, one step per year. Edited drafts are
 * cached per schema so flipping between percorsi never loses picks.
 *
 * Streams: [path] and [rules] carry the percorso alternatives and the editable rule
 * groups; [selectedSchemaId] is the schema the rules are compiled against;
 * [currentSegment] records the wizard step in view across config changes; [syncStatus]
 * tracks the rules fetch; [submitting] and [submitError] track the submission; [events]
 * carries the one-shot submitted notification.
 *
 * Actions: [selectSchema], [loadSelectedRules], [toggleCourse], [setSegment], [submit],
 * [refresh], [reset], plus [hasChanges] for the hosting sheet's dismiss decision.
 */
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

    /** CareerId and Esse3 stuId are the same identifier. */
    private val careerId = CareerId(key.studentId)

    private val _path = MutableStateFlow<Loadable<StudyPath?>>(Loadable.NotYetLoaded)

    /**
     * The percorso alternatives backing step zero. Loaded(null) = lookup failed — the
     * wizard degrades to rules-only against the request's schema.
     */
    val path: StateFlow<Loadable<StudyPath?>> = _path.asStateFlow()

    private val _selectedSchemaId = MutableStateFlow(key.schemaId.takeIf { it > 0 })

    /**
     * The schema the rules are compiled against: the student's current one from the
     * request, or null when the career has no plan yet and nothing was picked.
     */
    val selectedSchemaId: StateFlow<Long?> = _selectedSchemaId.asStateFlow()

    private val _rules = MutableStateFlow<Loadable<List<EditableRule>>>(Loadable.NotYetLoaded)
    val rules: StateFlow<Loadable<List<EditableRule>>> = _rules.asStateFlow()

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val _currentSegment = MutableStateFlow(PATH_SEGMENT)

    /**
     * The wizard segment in view: [PATH_SEGMENT] for the percorso step, otherwise the
     * choiceId of the single rule the page shows.
     */
    val currentSegment: StateFlow<Long> = _currentSegment.asStateFlow()

    private val _submitting = MutableStateFlow(false)
    val submitting: StateFlow<Boolean> = _submitting.asStateFlow()

    private val _submitError = MutableStateFlow<UiText?>(null)

    /** Submit failures stay visible until the next attempt or edit clears them. */
    val submitError: StateFlow<UiText?> = _submitError.asStateFlow()

    fun clearSubmitError() {
        _submitError.value = null
    }

    private val _events = Channel<StudyPlanEditEvent>(Channel.BUFFERED)
    val events: Flow<StudyPlanEditEvent> = _events.receiveAsFlow()

    /** Edited drafts parked per schema so switching back restores the picks. */
    private val draftCache = mutableMapOf<Long, List<EditableRule>>()
    private val loadMutex = Mutex()

    init {
        loadPath()
    }

    fun refresh() {
        loadPath()
    }

    /**
     * Discards an in-progress compilation. The ViewModel is sheet-hosted and outlives
     * the editor page, so leaving without submitting must not replay old picks, segment,
     * or parked drafts on the next open.
     */
    fun reset() {
        draftCache.clear()
        _rules.value = Loadable.NotYetLoaded
        _syncStatus.value = SyncStatus.Idle
        _submitError.value = null
        _currentSegment.value = PATH_SEGMENT
        _selectedSchemaId.value = key.schemaId.takeIf { it > 0 }
        loadPath()
    }

    /**
     * In the rules-only flow (no percorso step to defer behind) the rules load right
     * away; with a percorso step the fetch waits for Avanti via [loadSelectedRules], and
     * nothing is preselected (not even the student's current schema) — picking the
     * percorso is an explicit, mandatory act, so Avanti stays disabled until a
     * deliberate choice is made.
     */
    private fun loadPath() {
        viewModelScope.launch {
            val path = runCatching { getStudyPath(careerId) }.getOrNull()
            _path.value = Loadable.Loaded(path)
            val options = path?.options.orEmpty()
            if (options.isEmpty()) {
                _selectedSchemaId.value?.let { loadDraft(it, useCache = false) }
            } else {
                _selectedSchemaId.value = null
            }
        }
    }

    /**
     * Parks the edits of the schema being left so they survive coming back, then swaps
     * the selection. The fetch is deferred to Avanti; only a parked draft restores
     * instantly.
     */
    fun selectSchema(schemaId: Long) {
        if (_submitting.value) return
        val previous = _selectedSchemaId.value
        if (previous == schemaId) return
        if (previous != null) {
            _rules.value.valueOrNull()?.let { draftCache[previous] = it }
        }
        _submitError.value = null
        _selectedSchemaId.value = schemaId
        val cached = draftCache[schemaId]
        _rules.value = if (cached != null) Loadable.Loaded(cached) else Loadable.NotYetLoaded
        _syncStatus.value = SyncStatus.Idle
    }

    /**
     * The deferred half of [selectSchema]: the percorso page calls this when the user
     * advances, so picks that never leave the page never hit the network.
     */
    fun loadSelectedRules() {
        val schemaId = _selectedSchemaId.value ?: return
        loadDraft(schemaId, useCache = true)
    }

    /**
     * Fetches the schema's editable rules. A queued load can be stale (schema re-picked
     * meanwhile) or already satisfied by the load that held the lock before it, and the
     * fetched draft may target a schema no longer selected — results only publish while
     * their schema is still the selection, though they always land in the draft cache.
     */
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

    /**
     * Flips a course pick. The same activity can appear under several rules — a second
     * selection elsewhere is blocked, and the rule's own credit cap is respected;
     * mandatory courses never toggle.
     */
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

    /**
     * Sends the compiled plan to Esse3. On success the submitted drafts are dropped and
     * the path reloads — the ViewModel is sheet-hosted and outlives the editor page, so
     * a later session must start from the server's fresh plan rather than replaying
     * stale picks as pending changes.
     */
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
                    draftCache.clear()
                    _rules.value = Loadable.NotYetLoaded
                    _currentSegment.value = PATH_SEGMENT
                    loadPath()
                },
                onFailure = { cause -> _submitError.value = submitFriendlyMessage(cause) },
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

    /** What the student should expect next depends on the schema's approval flavour. */
    private fun submittedMessage(chosen: StudyPathOption?): UiText = UiText.StringResource(
        when (chosen?.approval) {
            PlanApprovalType.Automatic -> R.string.studyplanedit_submitted_automatic
            PlanApprovalType.AutomaticIfCompliant -> R.string.studyplanedit_submitted_if_compliant
            PlanApprovalType.Manual -> R.string.studyplanedit_submitted_pending
            else -> R.string.studyplanedit_submitted_generic
        },
    )

    /**
     * Maps a submission failure to student-facing copy: a profile-permission wall and a
     * 403 access denial get a plain explanation, anything else surfaces the raw message.
     */
    private fun submitFriendlyMessage(cause: Throwable): UiText {
        val raw = cause.message
            ?: return UiText.StringResource(R.string.studyplanedit_submit_error_generic)
        return when {
            raw.contains("Security failed", ignoreCase = true) ||
                raw.contains("profilo", ignoreCase = true) ->
                UiText.StringResource(R.string.studyplanedit_submit_error_profile)

            raw.contains("403") -> UiText.StringResource(R.string.studyplanedit_submit_error_forbidden)
            else -> UiText.DynamicString(raw)
        }
    }

    companion object {
        /** Sentinel segment for the percorso step; rule choiceIds are always positive. */
        const val PATH_SEGMENT = -1L
    }
}
