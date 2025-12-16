package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class GetUserBestGradeResponse(
    @SerializedName("hasgrade") val hasGrade: Boolean? = null,
    @SerializedName("grade") val grade: BigDecimal? = null,
    @SerializedName("gradetopass") val gradeToPass: BigDecimal? = null,
    @SerializedName("warnings") val warnings: List<Warning>? = null
)
