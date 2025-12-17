package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class QuizAttempt(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("quiz") val quiz: Int? = null,
    @SerializedName("userid") val userId: Int? = null,
    @SerializedName("attempt") val attempt: Int? = null,
    @SerializedName("uniqueid") val uniqueId: Int? = null,
    @SerializedName("layout") val layout: String? = null,
    @SerializedName("currentpage") val currentPage: Int? = null,
    @SerializedName("preview") val preview: Int? = null,
    @SerializedName("state") val state: QuizAttempt.QuizAttemptState? = null,
    @SerializedName("timestart") val timeStart: Int? = null,
    @SerializedName("timefinish") val timeFinish: Int? = null,
    @SerializedName("timemodified") val timeModified: Int? = null,
    @SerializedName("timemodifiedoffline") val timeModifiedOffline: Int? = null,
    @SerializedName("timecheckstate") val timeCheckState: Int? = null,
    @SerializedName("sumgrades") val sumGrades: BigDecimal? = null,
    @SerializedName("gradeitemmarks") val gradeItemMarks: List<AttemptGradeItem>? = null
) {
    enum class QuizAttemptState(val value: String) {
        @SerializedName(value = "inprogress") INPROGRESS("inprogress"),
        @SerializedName(value = "overdue") OVERDUE("overdue"),
        @SerializedName(value = "finished") FINISHED("finished"),
        @SerializedName(value = "abandoned") ABANDONED("abandoned")
    }
}
