package it.attendance100.mybicocca.data.remote.esse3.scraper.dto

/**
 * A course entry in the teaching evaluation list (ValDid).
 *
 * Represents a course from the student's academic record that can be evaluated.
 * The questionnaire status indicates whether evaluation is available, pending, or completed.
 */
data class Esse3EvaluationCourse(
    val courseCode: String,
    val courseName: String,
    val year: Int,
    val credits: Int,
    val academicYear: String,
    val status: Esse3EvaluationCourseStatus
)

/**
 * Status of a course evaluation questionnaire.
 */
sealed interface Esse3EvaluationCourseStatus {
    /**
     * No questionnaire available for this course.
     */
    data object NotAvailable : Esse3EvaluationCourseStatus

    /**
     * Questionnaire is available and pending completion.
     */
    data class Pending(val activityId: Long) : Esse3EvaluationCourseStatus

    /**
     * Questionnaire has been completed.
     */
    data class Completed(val activityId: Long) : Esse3EvaluationCourseStatus
}

/**
 * A partition (teacher/class section) within a course that can be evaluated separately.
 *
 * Each partition represents a specific combination of didactic unit, teacher, and class section
 * that requires a separate evaluation questionnaire.
 */
data class Esse3EvaluationPartition(
    val unitName: String,
    val teacher: String,
    val activityType: String,
    val partition: String,
    val status: Esse3EvaluationPartitionStatus
)

/**
 * Status of a course evaluation section.
 */
sealed interface Esse3EvaluationPartitionStatus {
    /**
     * No questionnaire available for this section.
     */
    data object NotAvailable : Esse3EvaluationPartitionStatus

    /**
     * Questionnaire is available and pending completion.
     */
    data class Pending(val questionnaireUrl: String) : Esse3EvaluationPartitionStatus

    /**
     * Questionnaire has been completed.
     */
    data object Completed : Esse3EvaluationPartitionStatus
}


/**
 * Navigation state for questionnaire pages.
 *
 * Contains all the identifiers and parameters needed to navigate between
 * questionnaire pages and submit answers.
 */
data class Esse3QuestionnaireNavigation(
    val questId: Long,
    val questCompId: Long,
    val pageId: Long,
    val userCompId: String,
    val questConfigId: String,
    val redirectUrl: String,
    val eventoCompCod: String,
    val adsceId: Long
) {
    /**
     * Converts this navigation state to form fields for submission.
     */
    fun toFormFields(): Map<String, String> = buildMap {
        put("p_quest_id", questId.toString())
        put("p_quest_comp_id", questCompId.toString())
        put("p_pagina_id", pageId.toString())
        put("p_user_comp_id", userCompId)
        put("p_quest_config_id", questConfigId)
        put("page_redirect", redirectUrl)
        put("p_evento_comp_cod", eventoCompCod)
        put("adsce_id", adsceId.toString())
        put("noEscapeFlg", "1")
    }
}

/**
 * A page within a questionnaire containing questions.
 */
data class Esse3QuestionnairePage(
    val navigation: Esse3QuestionnaireNavigation,
    val title: String?,
    val sectionTitle: String?,
    val questions: List<Esse3Question>,
    val requiredQuestionIds: List<Long>,
    val canGoBack: Boolean,
    val canGoForward: Boolean
)

/**
 * A question within a questionnaire page.
 */
sealed interface Esse3Question {
    val id: Long
    val fieldName: String
    val text: String
    val required: Boolean

    /**
     * Free text question with optional character limit.
     */
    data class FreeText(
        override val id: Long,
        override val fieldName: String,
        override val text: String,
        override val required: Boolean,
        val maxLength: Int?,
        val currentValue: String?
    ) : Esse3Question

    /**
     * Single choice question with radio buttons or dropdown.
     */
    data class SingleChoice(
        override val id: Long,
        override val fieldName: String,
        override val text: String,
        override val required: Boolean,
        val options: List<Esse3QuestionOption>,
        val selectedValue: String?
    ) : Esse3Question

    /**
     * Multiple choice question with checkboxes.
     */
    data class MultipleChoice(
        override val id: Long,
        override val fieldName: String,
        override val text: String,
        override val required: Boolean,
        val options: List<Esse3QuestionOption>,
        val selectedValues: List<String>
    ) : Esse3Question

    /**
     * Rating scale question (1-5, 1-10, etc.).
     */
    data class Rating(
        override val id: Long,
        override val fieldName: String,
        override val text: String,
        override val required: Boolean,
        val minValue: Int,
        val maxValue: Int,
        val selectedValue: Int?
    ) : Esse3Question
}

/**
 * An option for a choice question.
 */
data class Esse3QuestionOption(
    val value: String,
    val text: String
)

/**
 * Result of submitting a questionnaire page.
 */
sealed interface Esse3QuestionnaireSubmitResult {
    /**
     * Submission successful, next page returned.
     */
    data class NextPage(val page: Esse3QuestionnairePage) : Esse3QuestionnaireSubmitResult

    /**
     * Submission successful, questionnaire completed.
     */
    data object Completed : Esse3QuestionnaireSubmitResult

    /**
     * Submission failed with validation errors.
     */
    data class ValidationError(val message: String) : Esse3QuestionnaireSubmitResult
}
