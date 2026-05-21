package it.attendance100.mybicocca.ui.screen.segreterie.studyplan.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.data.api.esse3.Esse3Api
import it.attendance100.mybicocca.data.dto.esse3.Esse3ChoiceRuleSchemaWithDetails
import it.attendance100.mybicocca.data.dto.esse3.Esse3ChoiceType
import it.attendance100.mybicocca.data.dto.esse3.Esse3PostPlanActivity
import it.attendance100.mybicocca.data.dto.esse3.Esse3PostPlanBody
import it.attendance100.mybicocca.data.dto.esse3.Esse3StudyPlanActivity
import it.attendance100.mybicocca.di.IoDispatcher
import it.attendance100.mybicocca.util.PreferencesManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class StudyPlanEditViewModel @Inject constructor(
    private val esse3Api: Esse3Api,
    private val prefs: PreferencesManager,
    savedStateHandle: SavedStateHandle,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val studentId: Long = savedStateHandle["studentId"] ?: 0L
    private val choiceRegulationId: Long = savedStateHandle["choiceRegulationId"] ?: 0L
    private val schemaId: Long = savedStateHandle["schemaId"] ?: 0L
    private val planId: Long = savedStateHandle["planId"] ?: -1L
    private val initialYear: Int? = savedStateHandle["year"]

    private val _state = MutableStateFlow(StudyPlanEditState())
    val state: StateFlow<StudyPlanEditState> = _state.asStateFlow()

    val isValid = _state.map { st ->
        st.rules.filter { it.courseYear == st.currentYear || st.currentYear == 0 }
            .all { it.isSatisfied }
    }

    val allValid = _state.map { st -> st.rules.all { it.isSatisfied } }

    val yearValidity = _state.map { st ->
        st.years.associateWith { year ->
            st.rules.filter { it.courseYear == year || it.courseYear == 0 }
                .all { it.isSatisfied }
        }
    }

    init {
        loadSchema()
    }

    private fun loadSchema() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                withContext(ioDispatcher) {
                    val schemaDeferred = async {
                        esse3Api.choiceRules.getStudyPlanSchemaWithDetails(
                            choiceRegulationId,
                            schemaId
                        )
                    }
                    val existingActivities = if (planId > 0) {
                        async {
                            esse3Api.plans.getStudentPlan(studentId, planId).activity
                        }.await()
                    } else {
                        emptyList()
                    }
                    val schema = schemaDeferred.await()

                    val assignedActivityCodes = mutableSetOf<String>()
                    val rules = schema.choiceRules.mapNotNull { rule ->
                        mapRuleToEditable(rule, existingActivities, assignedActivityCodes)
                    }.sortedWith(compareBy({ it.courseYear }, { it.orderNumber }))

                    val years = rules.map { it.courseYear }.filter { it > 0 }.distinct().sorted()
                    val startYear = initialYear ?: years.firstOrNull() ?: 0

                    _state.value = _state.value.copy(
                        rules = rules,
                        years = years,
                        currentYear = startYear,
                        isLoading = false,
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Errore nel caricamento dello schema",
                )
            }
        }
    }

    private fun mapRuleToEditable(
        rule: Esse3ChoiceRuleSchemaWithDetails,
        existingActivities: List<Esse3StudyPlanActivity>,
        assignedActivityCodes: MutableSet<String>,
    ): EditableRule? {
        val choiceId = rule.choiceId ?: return null
        val isMandatory = rule.choiceType == Esse3ChoiceType.Obligatory
        val maxCredits = rule.maxTeachingUnit

        var currentCredits = 0f

        val courses = rule.blocks.flatMap { block ->
            block.activity.mapNotNull { activity ->
                val key = activity.contextualizedTeachingActivityKey ?: return@mapNotNull null
                val actCode = key.activityCode ?: return@mapNotNull null
                val weight = activity.weight ?: 0f

                var isSelected = isMandatory
                if (!isSelected && !assignedActivityCodes.contains(actCode)) {
                    val isPreSelected = existingActivities.any { existing ->
                        existing.contextualizedTeachingActivityKey?.activityCode == actCode
                    }
                    if (isPreSelected) {
                        if (maxCredits == null || currentCredits + weight <= maxCredits) {
                            isSelected = true
                        }
                    }
                }

                if (isSelected) {
                    assignedActivityCodes.add(actCode)
                    if (!isMandatory) {
                        currentCredits += weight
                    }
                }

                EditableCourse(
                    teachingActivityChoiceId = activity.teachingActivityChoiceId
                        ?: return@mapNotNull null,
                    activityCode = actCode,
                    activityDescription = key.activityDescription ?: actCode,
                    credits = weight,
                    courseOfStudyCode = key.courseOfStudyCode,
                    studyPlanCode = key.studyPlanCode,
                    academicYearOfferId = key.academicYearOfferId.toInt(),
                    blockId = block.blockId,
                    isSelected = isSelected,
                    isMandatory = isMandatory,
                    isInitialSelected = isSelected,
                )
            }
        }

        if (courses.isEmpty()) return null

        return EditableRule(
            choiceId = choiceId,
            orderNumber = rule.orderNumber ?: 0,
            description = rule.description ?: "",
            courseYear = rule.courseYear ?: 0,
            choiceType = rule.choiceType?.value ?: "O",
            choiceTypeDescription = rule.choiceTypeDescription ?: "",
            minCredits = rule.minTeachingUnit,
            maxCredits = rule.maxTeachingUnit,
            isOptional = rule.optionalFlag == 1,
            courses = courses,
            preNote = rule.preNote,
            postNote = rule.postNote,
        )
    }

    fun toggleCourse(ruleChoiceId: Long, courseTeachingActivityChoiceId: Long) {
        _state.value = _state.value.copy(
            rules = _state.value.rules.map { rule ->
                if (rule.choiceId != ruleChoiceId) return@map rule
                rule.copy(
                    courses = rule.courses.map { course ->
                        if (course.teachingActivityChoiceId != courseTeachingActivityChoiceId) return@map course
                        if (course.isMandatory) return@map course

                        if (!course.isSelected) {
                            val isAlreadySelectedElsewhere = _state.value.rules.any { r ->
                                r.choiceId != ruleChoiceId && r.courses.any { it.activityCode == course.activityCode && it.isSelected }
                            }
                            if (isAlreadySelectedElsewhere || !rule.isCourseSelectable(course)) return@map course
                        }

                        course.copy(isSelected = !course.isSelected)
                    }
                )
            }
        )
    }

    fun setYear(year: Int) {
        _state.value = _state.value.copy(currentYear = year)
    }

    fun nextYear(): Boolean {
        val st = _state.value
        val idx = st.years.indexOf(st.currentYear)
        return if (idx < st.years.lastIndex) {
            _state.value = st.copy(currentYear = st.years[idx + 1])
            true
        } else {
            false
        }
    }

    fun previousYear(): Boolean {
        val st = _state.value
        val idx = st.years.indexOf(st.currentYear)
        return if (idx > 0) {
            _state.value = st.copy(currentYear = st.years[idx - 1])
            true
        } else {
            false
        }
    }

    val isLastYear: Boolean
        get() {
            val st = _state.value
            val idx = st.years.indexOf(st.currentYear)
            return idx == st.years.lastIndex
        }

    val isFirstYear: Boolean
        get() {
            val st = _state.value
            val idx = st.years.indexOf(st.currentYear)
            return idx == 0
        }

    fun submitPlan() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSubmitting = true, error = null)
            try {
                withContext(ioDispatcher) {
                    val selectedCourses = _state.value.rules.flatMap { rule ->
                        rule.courses.filter { it.isSelected }.map { course -> rule to course }
                    }

                    val activities = selectedCourses.mapIndexed { index, (rule, course) ->
                        Esse3PostPlanActivity(
                            itemId = index + 1,
                            orderNumber = rule.orderNumber,
                            activityCode = course.activityCode,
                            courseOfStudyTeachingActivityCode = course.courseOfStudyCode,
                            studyPlanTeachingActivityCode = course.studyPlanCode,
                            academicYearOfferActivityId = course.academicYearOfferId,
                        )
                    }

                    val body = Esse3PostPlanBody(
                        type = "S",
                        state = "P",
                        implementationFlag = false,
                        cancelValidPlanFlag = true,
                        activity = activities,
                    )

                    esse3Api.plans.postStudentPlan(studentId, body)
                }
                prefs.studyPlanLastModified = System.currentTimeMillis()
                _state.value = _state.value.copy(isSubmitting = false, submitted = true)
            } catch (e: Exception) {
                val rawMsg = e.message ?: "Errore nell'invio del piano"
                val cleanMsg = if (rawMsg.contains(
                        "Security failed",
                        ignoreCase = true
                    ) || rawMsg.contains("profilo", ignoreCase = true)
                ) {
                    "La modifica del piano non è attualmente consentita dal tuo profilo."
                } else if (rawMsg.contains("403", ignoreCase = true)) {
                    "Accesso negato: Impossibile inviare il piano di studi."
                } else {
                    rawMsg
                }
                _state.value = _state.value.copy(
                    isSubmitting = false,
                    error = cleanMsg,
                )
            }
        }
    }

    fun hasChanges(): Boolean {
        return _state.value.rules.any { rule ->
            rule.courses.any { it.isSelected != it.isInitialSelected }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
