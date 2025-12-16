package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class Quiz(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("course") val course: Int? = null,
    @SerializedName("coursemodule") val courseModule: Int? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("intro") val intro: String? = null,
    @SerializedName("introformat") val introFormat: Int? = null,
    @SerializedName("introfiles") val introFiles: List<MoodleFile>? = null,
    @SerializedName("section") val section: Int? = null,
    @SerializedName("visible") val visible: Boolean? = null,
    @SerializedName("groupmode") val groupMode: Int? = null,
    @SerializedName("groupingid") val groupingId: Int? = null,
    @SerializedName("lang") val lang: String? = null,
    @SerializedName("timeopen") val timeOpen: Int? = null,
    @SerializedName("timeclose") val timeClose: Int? = null,
    @SerializedName("timelimit") val timeLimit: Int? = null,
    @SerializedName("overduehandling") val overdueHandling: Quiz.QuizOverdueHandling? = null,
    @SerializedName("graceperiod") val gracePeriod: Int? = null,
    @SerializedName("preferredbehaviour") val preferredBehaviour: String? = null,
    @SerializedName("canredoquestions") val canRedoQuestions: Int? = null,
    @SerializedName("attempts") val attempts: Int? = null,
    @SerializedName("attemptonlast") val attemptOnLast: Int? = null,
    @SerializedName("grademethod") val gradeMethod: Int? = null,
    @SerializedName("decimalpoints") val decimalPoints: Int? = null,
    @SerializedName("questiondecimalpoints") val questionDecimalPoints: Int? = null,
    @SerializedName("reviewattempt") val reviewAttempt: Int? = null,
    @SerializedName("reviewcorrectness") val reviewCorrectness: Int? = null,
    @SerializedName("reviewmarks") val reviewMarks: Int? = null,
    @SerializedName("reviewspecificfeedback") val reviewSpecificFeedback: Int? = null,
    @SerializedName("reviewgeneralfeedback") val reviewGeneralFeedback: Int? = null,
    @SerializedName("reviewrightanswer") val reviewRightAnswer: Int? = null,
    @SerializedName("reviewoverallfeedback") val reviewOverallFeedback: Int? = null,
    @SerializedName("questionsperpage") val questionsPerPage: Int? = null,
    @SerializedName("navmethod") val navMethod: Quiz.QuizNavMethod? = null,
    @SerializedName("shuffleanswers") val shuffleAnswers: Int? = null,
    @SerializedName("sumgrades") val sumGrades: BigDecimal? = null,
    @SerializedName("grade") val grade: BigDecimal? = null,
    @SerializedName("timecreated") val timeCreated: Int? = null,
    @SerializedName("timemodified") val timeModified: Int? = null,
    @SerializedName("password") val password: String? = null,
    @SerializedName("subnet") val subnet: String? = null,
    @SerializedName("browsersecurity") val browserSecurity: String? = null,
    @SerializedName("delay1") val delay1: Int? = null,
    @SerializedName("delay2") val delay2: Int? = null,
    @SerializedName("showuserpicture") val showUserPicture: Int? = null,
    @SerializedName("showblocks") val showBlocks: Int? = null,
    @SerializedName("completionattemptsexhausted") val completionAttemptsExhausted: Int? = null,
    @SerializedName("completionpass") val completionPass: Int? = null,
    @SerializedName("allowofflineattempts") val allowOfflineAttempts: Int? = null,
    @SerializedName("autosaveperiod") val autoSavePeriod: Int? = null,
    @SerializedName("hasfeedback") val hasFeedback: Int? = null,
    @SerializedName("hasquestions") val hasQuestions: Int? = null
) {
    enum class QuizOverdueHandling(val value: String) {
        @SerializedName(value = "autosubmit") AUTOSUBMIT("autosubmit"),
        @SerializedName(value = "graceperiod") GRACEPERIOD("graceperiod"),
        @SerializedName(value = "autoabandon") AUTOABANDON("autoabandon")
    }
    enum class QuizNavMethod(val value: String) {
        @SerializedName(value = "free") FREE("free"),
        @SerializedName(value = "seq") SEQ("seq")
    }
}
