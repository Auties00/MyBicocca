package it.attendance100.mybicocca.data.dto.elearning

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ElearningQuiz(
    @SerialName("id")
    val id: Int,
    @SerialName("course")
    val course: Int,
    @SerialName("coursemodule")
    val courseModule: Int,
    @SerialName("name")
    val name: String,
    @SerialName("intro")
    val intro: String? = null,
    @SerialName("introformat")
    val introFormat: Int? = null,
    @SerialName("introfiles")
    val introFiles: List<ElearningFile>? = null,
    @SerialName("timeopen")
    val timeOpen: Long? = null,
    @SerialName("timeclose")
    val timeClose: Long? = null,
    @SerialName("timelimit")
    val timeLimit: Int? = null,
    @SerialName("overduehandling")
    val overdueHandling: String? = null,
    @SerialName("graceperiod")
    val gracePeriod: Int? = null,
    @SerialName("preferredbehaviour")
    val preferredBehaviour: String? = null,
    @SerialName("canredoquestions")
    val canRedoQuestions: Int? = null,
    @SerialName("attempts")
    val attempts: Int? = null,
    @SerialName("attemptonlast")
    val attemptOnLast: Int? = null,
    @SerialName("grademethod")
    val gradeMethod: Int? = null,
    @SerialName("decimalpoints")
    val decimalPoints: Int? = null,
    @SerialName("questiondecimalpoints")
    val questionDecimalPoints: Int? = null,
    @SerialName("reviewattempt")
    val reviewAttempt: Int? = null,
    @SerialName("reviewcorrectness")
    val reviewCorrectness: Int? = null,
    @SerialName("reviewmaxmarks")
    val reviewMaxMarks: Int? = null,
    @SerialName("reviewmarks")
    val reviewMarks: Int? = null,
    @SerialName("reviewspecificfeedback")
    val reviewSpecificFeedback: Int? = null,
    @SerialName("reviewgeneralfeedback")
    val reviewGeneralFeedback: Int? = null,
    @SerialName("reviewrightanswer")
    val reviewRightAnswer: Int? = null,
    @SerialName("reviewoverallfeedback")
    val reviewOverallFeedback: Int? = null,
    @SerialName("questionsperpage")
    val questionsPerPage: Int? = null,
    @SerialName("navmethod")
    val navMethod: String? = null,
    @SerialName("shuffleanswers")
    val shuffleAnswers: Int? = null,
    @SerialName("sumgrades")
    val sumGrades: Double? = null,
    @SerialName("grade")
    val grade: Double? = null,
    @SerialName("hasquestions")
    val hasQuestions: Int? = null,
    @SerialName("hasfeedback")
    val hasFeedback: Int? = null,
    @SerialName("section")
    val section: Int? = null,
    @SerialName("visible")
    val visible: Boolean? = null,
    @SerialName("groupmode")
    val groupMode: Int? = null,
    @SerialName("groupingid")
    val groupingId: Int? = null,
    @SerialName("autosaveperiod")
    val autosavePeriod: Int? = null,
    @SerialName("allowofflineattempts")
    val allowOfflineAttempts: Int? = null
) {
    companion object {
        // Grade methods
        const val GRADE_METHOD_HIGHEST = 1
        const val GRADE_METHOD_AVERAGE = 2
        const val GRADE_METHOD_FIRST = 3
        const val GRADE_METHOD_LAST = 4
    }
}

@Serializable
data class ElearningGetQuizzesResponse(
    @SerialName("quizzes")
    val quizzes: List<ElearningQuiz>,
    @SerialName("warnings")
    val warnings: List<ElearningWarning>? = null
) : ElearningResponse
