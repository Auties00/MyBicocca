package it.attendance100.mybicocca.ui.screen.segreterie.studyplan.edit

data class EditableCourse(
    val teachingActivityChoiceId: Long,
    val activityCode: String,
    val activityDescription: String,
    val credits: Float,
    val courseOfStudyCode: String?,
    val studyPlanCode: String?,
    val academicYearOfferId: Int?,
    val blockId: Int? = null,
    val isSelected: Boolean = false,
    val isMandatory: Boolean = false,
    val isInitialSelected: Boolean = false,
)

data class EditableRule(
    val choiceId: Long,
    val orderNumber: Int,
    val description: String,
    val courseYear: Int,
    val choiceType: String,
    val choiceTypeDescription: String,
    val minCredits: Float?,
    val maxCredits: Float?,
    val isOptional: Boolean,
    val courses: List<EditableCourse>,
    val preNote: String?,
    val postNote: String?,
) {
    val selectedCredits: Float
        get() = courses.filter { it.isSelected }.sumOf { it.credits.toDouble() }.toFloat()

    val isMandatoryRule: Boolean
        get() = choiceType == "O"

    val totalMandatoryCredits: Float
        get() = courses.filter { it.isMandatory }.sumOf { it.credits.toDouble() }.toFloat()

    val effectiveMinCredits: Float?
        get() = if (isMandatoryRule) totalMandatoryCredits else minCredits

    val effectiveMaxCredits: Float?
        get() = if (isMandatoryRule) totalMandatoryCredits else maxCredits

    val isSatisfied: Boolean
        get() {
            if (isMandatoryRule) return true
            if (isOptional && courses.none { it.isSelected }) return true
            val selected = selectedCredits
            val minOk = minCredits == null || selected >= minCredits
            val maxOk = maxCredits == null || selected <= maxCredits
            return minOk && maxOk
        }

    val remainingCredits: Float?
        get() {
            val max = maxCredits ?: return null
            return (max - selectedCredits).coerceAtLeast(0f)
        }

    val isMaxReached: Boolean
        get() {
            val max = maxCredits ?: return false
            return selectedCredits >= max
        }

    fun isCourseSelectable(course: EditableCourse): Boolean {
        if (course.isMandatory) return false // mandatory courses are always checked, not toggleable
        if (course.isSelected) return true   // can always deselect
        val max = maxCredits ?: return true
        return selectedCredits + course.credits <= max
    }
}

data class StudyPlanEditState(
    val rules: List<EditableRule> = emptyList(),
    val years: List<Int> = emptyList(),
    val currentYear: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null,
    val isSubmitting: Boolean = false,
    val submitted: Boolean = false,
)
