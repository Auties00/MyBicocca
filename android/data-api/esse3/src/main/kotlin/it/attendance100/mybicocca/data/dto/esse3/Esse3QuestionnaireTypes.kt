package it.attendance100.mybicocca.data.dto.esse3

/**
 * A questionnaire to be filled by the student.
 */
data class Esse3Questionnaire(
    val id: Long,
    val compositionId: Long,
    val title: String,
    val status: Esse3QuestionnaireStatus,
    val required: Boolean = false,
    val courseCode: String? = null,
    val courseName: String? = null,
    val professor: String? = null
)

/**
 * Status of a questionnaire.
 */
enum class Esse3QuestionnaireStatus {
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED;

    companion object {
        fun fromString(value: String): Esse3QuestionnaireStatus {
            return when (value.lowercase()) {
                "completato", "completed" -> COMPLETED
                "in corso", "in progress" -> IN_PROGRESS
                else -> NOT_STARTED
            }
        }
    }
}

/**
 * A page within a questionnaire.
 */
data class Esse3QuestionnairePage(
    val id: Long,
    val pageNumber: Int,
    val title: String?,
    val questions: List<Esse3Question>
)

/**
 * A question within a questionnaire page.
 */
sealed class Esse3Question {
    abstract val id: Long
    abstract val text: String
    abstract val required: Boolean
    abstract val fieldName: String

    /**
     * Free text question.
     */
    data class FreeText(
        override val id: Long,
        override val text: String,
        override val required: Boolean,
        override val fieldName: String,
        val maxLength: Int? = null,
        val currentValue: String? = null
    ) : Esse3Question()

    /**
     * Single choice question.
     */
    data class SingleChoice(
        override val id: Long,
        override val text: String,
        override val required: Boolean,
        override val fieldName: String,
        val options: List<Esse3QuestionOption>,
        val selectedValue: String? = null
    ) : Esse3Question()

    /**
     * Multiple choice question.
     */
    data class MultipleChoice(
        override val id: Long,
        override val text: String,
        override val required: Boolean,
        override val fieldName: String,
        val options: List<Esse3QuestionOption>,
        val selectedValues: List<String> = emptyList(),
        val minSelections: Int = 0,
        val maxSelections: Int = Int.MAX_VALUE
    ) : Esse3Question()

    /**
     * Rating scale question.
     */
    data class Rating(
        override val id: Long,
        override val text: String,
        override val required: Boolean,
        override val fieldName: String,
        val minValue: Int,
        val maxValue: Int,
        val selectedValue: Int? = null
    ) : Esse3Question()
}

/**
 * An option for a choice question.
 */
data class Esse3QuestionOption(
    val id: Long?,
    val value: String,
    val text: String
)

/**
 * Questionnaire navigation info.
 */
data class Esse3QuestionnaireNavigation(
    val questId: Long,
    val questCompId: Long,
    val pageId: Long,
    val userCompId: String?,
    val questConfigId: String?,
    val redirectUrl: String?,
    val eventoCompCod: String?,
    val adsceId: Long?
) {
    fun toFormFields(): Map<String, String> = buildMap {
        put("p_quest_id", questId.toString())
        put("p_quest_comp_id", questCompId.toString())
        put("p_pagina_id", pageId.toString())
        userCompId?.let { put("p_user_comp_id", it) }
        questConfigId?.let { put("p_quest_config_id", it) }
        redirectUrl?.let { put("page_redirect", it) }
        eventoCompCod?.let { put("p_evento_comp_cod", it) }
        adsceId?.let { put("adsce_id", it.toString()) }
        put("noEscapeFlg", "0")
    }

    fun toQueryString(): String = toFormFields().entries.joinToString("&") { "${it.key}=${it.value}" }
}

/**
 * Course evaluation questionnaire entry from libretto.
 *
 * Data comes from the ValDid table with columns:
 * Anno di corso | Attività Didattiche | Peso in crediti | Stato | AA Freq. | Ric. | Q.Val.
 */
data class Esse3CourseEvaluation(
    val courseCode: String,
    val courseName: String,
    val courseYear: Int,
    val credits: Int,
    val academicYear: String?,
    val status: String?,
    val isRecognized: Boolean,
    val questionnaireUrl: String?
)
