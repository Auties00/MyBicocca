package it.attendance100.mybicocca.domain.model.studyplan

/**
 * One selectable activity inside a plan-compilation rule, toggled by the student in the
 * plan-edit wizard of the registry tab.
 *
 * @property choiceId Esse3 `sceltaAdId` — unique within the schema, the identity used
 *   for toggling.
 * @property code University activity code.
 * @property name Name of the teaching activity.
 * @property credits CFU weight of the activity.
 * @property courseOfStudyCode Carried through untouched: Esse3 requires it back on
 *   submit.
 * @property studyPlanCode Carried through untouched: Esse3 requires it back on submit.
 * @property academicYearOfferId Carried through untouched: Esse3 requires it back on
 *   submit.
 * @property isSelected Whether the activity is currently picked in the draft.
 * @property isMandatory True for activities of obligatory rules, which are pre-selected
 *   and untoggleable.
 * @property isInitialSelected Selection state when the draft was loaded — lets the UI
 *   detect unsaved changes.
 */
data class EditableCourse(
    val choiceId: Long,
    val code: String,
    val name: String,
    val credits: Float,
    val courseOfStudyCode: String?,
    val studyPlanCode: String?,
    val academicYearOfferId: Int?,
    val isSelected: Boolean = false,
    val isMandatory: Boolean = false,
    val isInitialSelected: Boolean = false,
)

/**
 * What a rule's min/max constraint counts, decided by the schema's Esse3 `tipUnt`:
 * credits picked ([Credits]) or activities picked ([Activities], used by BLK rules).
 */
enum class ChoiceConstraintUnit {
    Credits,
    Activities,
}

/**
 * A compilation rule of the plan schema with its selectable activities, one section of
 * the plan-edit wizard. A `courseYear == 0` marks rules not tied to a specific year of
 * study.
 *
 * @property choiceId Esse3 id of the rule.
 * @property orderNumber Position of the rule within the schema; echoed back on submit.
 * @property description Title of the rule as authored in Esse3.
 * @property courseYear Year of the course the rule belongs to; 0 for year-less rules.
 * @property typeDescription Human description of the rule kind.
 * @property isMandatoryRule True for obligatory rules ("O"): every activity is
 *   pre-selected and untoggleable.
 * @property unit Unit [minUnits]/[maxUnits] are measured in: CFU for credit rules, picks
 *   for activity rules.
 * @property minUnits Minimum total the selection must reach, in [unit]; null when
 *   unbounded.
 * @property maxUnits Maximum total the selection may reach, in [unit]; null when
 *   unbounded.
 * @property isOptional Esse3 `opzFlg` — an optional rule is satisfied with zero picks.
 * @property courses The activities the rule offers.
 * @property preNote Free-text note Esse3 renders before the rule.
 * @property postNote Free-text note Esse3 renders after the rule.
 */
data class EditableRule(
    val choiceId: Long,
    val orderNumber: Int,
    val description: String,
    val courseYear: Int,
    val typeDescription: String,
    val isMandatoryRule: Boolean,
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

    /** The selection total in the rule's own unit of measure. */
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

    /**
     * Whether the rule's constraint is met: mandatory rules always are, optional rules
     * accept an empty selection, and the rest compare [selectedUnits] against the
     * min/max bounds.
     */
    val isSatisfied: Boolean
        get() {
            if (isMandatoryRule) return true
            if (isOptional && courses.none { it.isSelected }) return true
            val selected = selectedUnits
            val minOk = minUnits == null || selected >= minUnits
            val maxOk = maxUnits == null || selected <= maxUnits
            return minOk && maxOk
        }

    /**
     * Whether the toggle for [course] should be enabled: mandatory courses are always
     * checked and untoggleable, deselecting is always allowed, and selecting is allowed
     * only while the rule's cap has room for the course's weight.
     */
    fun isCourseSelectable(course: EditableCourse): Boolean {
        if (course.isMandatory) return false
        if (course.isSelected) return true
        val max = maxUnits ?: return true
        val weight = when (unit) {
            ChoiceConstraintUnit.Credits -> course.credits
            ChoiceConstraintUnit.Activities -> 1f
        }
        return selectedUnits + weight <= max
    }
}
