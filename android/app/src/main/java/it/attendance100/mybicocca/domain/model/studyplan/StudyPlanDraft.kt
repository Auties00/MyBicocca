package it.attendance100.mybicocca.domain.model.studyplan

// One selectable activity inside a plan-compilation rule.
data class EditableCourse(
    // teachingActivityChoiceId — unique within the schema, identity for toggling.
    val choiceId: Long,
    val code: String,
    val name: String,
    val credits: Float,
    // Carried through untouched: Esse3 requires them back on submit.
    val courseOfStudyCode: String?,
    val studyPlanCode: String?,
    val academicYearOfferId: Int?,
    val isSelected: Boolean = false,
    val isMandatory: Boolean = false,
    // Selection state when the draft was loaded — lets the UI detect unsaved changes.
    val isInitialSelected: Boolean = false,
)

// What a rule's min/max constraint counts: credits picked or activities picked.
enum class ChoiceConstraintUnit {
    Credits,
    Activities,
}

// A compilation rule of the plan schema with its selectable activities. courseYear == 0
// means the rule isn't tied to a specific year of study.
data class EditableRule(
    val choiceId: Long,
    val orderNumber: Int,
    val description: String,
    val courseYear: Int,
    val typeDescription: String,
    // Obligatory rules ("O"): every activity is pre-selected and untoggleable.
    val isMandatoryRule: Boolean,
    // min/max are measured in `unit`: CFU for credit rules, picks for activity rules.
    val unit: ChoiceConstraintUnit,
    val minUnits: Float?,
    val maxUnits: Float?,
    val isOptional: Boolean,
    val courses: List<EditableCourse>,
    val preNote: String?,
    val postNote: String?,
) {
    val selectedCredits: Float
        get() = courses.filter { it.isSelected }.sumOf { it.credits.toDouble() }.toFloat()

    val selectedCount: Int
        get() = courses.count { it.isSelected }

    // The selection total in the rule's own unit of measure.
    val selectedUnits: Float
        get() = when (unit) {
            ChoiceConstraintUnit.Credits -> selectedCredits
            ChoiceConstraintUnit.Activities -> selectedCount.toFloat()
        }

    private val totalMandatoryCredits: Float
        get() = courses.filter { it.isMandatory }.sumOf { it.credits.toDouble() }.toFloat()

    val effectiveMinUnits: Float?
        get() = if (isMandatoryRule) totalMandatoryCredits else minUnits

    val effectiveMaxUnits: Float?
        get() = if (isMandatoryRule) totalMandatoryCredits else maxUnits

    val isSatisfied: Boolean
        get() {
            if (isMandatoryRule) return true
            if (isOptional && courses.none { it.isSelected }) return true
            val selected = selectedUnits
            val minOk = minUnits == null || selected >= minUnits
            val maxOk = maxUnits == null || selected <= maxUnits
            return minOk && maxOk
        }

    fun isCourseSelectable(course: EditableCourse): Boolean {
        // Mandatory courses are always checked, never toggleable.
        if (course.isMandatory) return false
        // Deselecting is always allowed.
        if (course.isSelected) return true
        val max = maxUnits ?: return true
        val weight = when (unit) {
            ChoiceConstraintUnit.Credits -> course.credits
            ChoiceConstraintUnit.Activities -> 1f
        }
        return selectedUnits + weight <= max
    }
}
